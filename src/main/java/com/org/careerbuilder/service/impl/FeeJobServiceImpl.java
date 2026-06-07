package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminFeeRequests;
import com.org.careerbuilder.dto.response.AdminFeeDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.FeeJob;
import com.org.careerbuilder.models.enums.FeeJobStatus;
import com.org.careerbuilder.models.enums.FeeJobType;
import com.org.careerbuilder.repository.FeeJobRepository;
import com.org.careerbuilder.service.FeeJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class FeeJobServiceImpl implements FeeJobService {

    private final FeeJobRepository jobRepository;
    private final FeeJobRunner jobRunner;

    @Override
    public AdminFeeDtos.JobStatusResponse enqueueObligationGeneration(
            Long schoolId, Long structureId, String performedBy) {
        // Persisted in its own (save) transaction so the worker thread sees a committed row.
        FeeJob job = jobRepository.save(FeeJob.builder()
                .schoolId(schoolId)
                .jobType(FeeJobType.OBLIGATION_GENERATION)
                .status(FeeJobStatus.QUEUED)
                .processedItems(0)
                .createdBy(performedBy)
                .build());
        Long jobId = job.getId();
        triggerAfterCommit(() -> jobRunner.runObligationGeneration(jobId, structureId));
        return toStatus(job);
    }

    @Override
    public AdminFeeDtos.JobStatusResponse enqueueExport(AdminFeeRequests.ExportFeeReportRequest request) {
        FeeJob job = jobRepository.save(FeeJob.builder()
                .schoolId(request.getSchoolId())
                .jobType(FeeJobType.EXPORT)
                .status(FeeJobStatus.QUEUED)
                .processedItems(0)
                .build());
        Long jobId = job.getId();
        triggerAfterCommit(() -> jobRunner.runExport(jobId, request));
        return toStatus(job);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFeeDtos.JobStatusResponse getStatus(Long schoolId, Long jobId) {
        return toStatus(requireJob(schoolId, jobId));
    }

    @Override
    @Transactional(readOnly = true)
    public FeeJob getDownloadable(Long schoolId, Long jobId) {
        FeeJob job = requireJob(schoolId, jobId);
        if (job.getStatus() != FeeJobStatus.DONE || job.getResultData() == null) {
            throw new IllegalStateException("Export is not ready for download (status: " + job.getStatus() + ")");
        }
        return job;
    }

    /** Fire the worker only once the enclosing transaction commits (so it sees the row). */
    private void triggerAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private FeeJob requireJob(Long schoolId, Long jobId) {
        return jobRepository.findByIdAndSchoolId(jobId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private AdminFeeDtos.JobStatusResponse toStatus(FeeJob job) {
        boolean downloadReady = job.getStatus() == FeeJobStatus.DONE
                && job.getJobType() == FeeJobType.EXPORT
                && job.getResultData() != null;
        return new AdminFeeDtos.JobStatusResponse(
                job.getId(),
                job.getJobType().name(),
                job.getStatus().name(),
                job.getTotalItems(),
                job.getProcessedItems(),
                job.getMessage(),
                downloadReady);
    }
}
