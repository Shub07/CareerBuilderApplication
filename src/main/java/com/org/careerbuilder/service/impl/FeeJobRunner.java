package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminFeeRequests;
import com.org.careerbuilder.models.Fee;
import com.org.careerbuilder.models.FeeJob;
import com.org.careerbuilder.models.FeeStructure;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.enums.FeeJobStatus;
import com.org.careerbuilder.repository.FeeJobRepository;
import com.org.careerbuilder.repository.FeeRepository;
import com.org.careerbuilder.repository.FeeStructureRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.service.AdminFeeService;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Executes fee background jobs off the request thread. Lives in its own bean so
 * the {@code @Async} proxy is honoured when invoked from {@link FeeJobService}.
 */
@Component
public class FeeJobRunner {

    private static final int BATCH = 200;

    private final FeeJobRepository jobRepository;
    private final FeeStructureRepository structureRepository;
    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;
    private final AdminFeeService adminFeeService;

    public FeeJobRunner(FeeJobRepository jobRepository,
                        FeeStructureRepository structureRepository,
                        FeeRepository feeRepository,
                        StudentRepository studentRepository,
                        @Lazy AdminFeeService adminFeeService) {
        this.jobRepository = jobRepository;
        this.structureRepository = structureRepository;
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
        this.adminFeeService = adminFeeService;
    }

    @Async("feeJobExecutor")
    @Transactional
    public void runObligationGeneration(Long jobId, Long structureId) {
        FeeJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }
        try {
            job.setStatus(FeeJobStatus.RUNNING);
            jobRepository.save(job);

            FeeStructure structure = structureRepository.findById(structureId)
                    .orElseThrow(() -> new IllegalStateException("Fee structure not found: " + structureId));
            List<Student> targets = targetStudents(structure);
            job.setTotalItems(targets.size());

            int created = 0;
            int index = 0;
            for (Student s : targets) {
                Optional<Fee> existing = feeRepository.findByStudentIdAndFeeTypeAndAcademicYearAndTerm(
                        s.getId(), structure.getFeeType(), structure.getAcademicYear(),
                        structure.getFrequency().name());
                if (existing.isEmpty()) {
                    feeRepository.save(Fee.builder()
                            .student(s)
                            .feeType(structure.getFeeType())
                            .amount(structure.getAmount())
                            .paidAmount(BigDecimal.ZERO)
                            .dueDate(resolveDueDate(structure))
                            .status(Fee.FeeStatus.PENDING)
                            .academicYear(structure.getAcademicYear())
                            .term(structure.getFrequency().name())
                            .description(structure.getFeeName())
                            .build());
                    created++;
                }
                if (++index % BATCH == 0) {
                    job.setProcessedItems(index);
                    jobRepository.save(job);
                }
            }
            job.setProcessedItems(targets.size());
            job.setStatus(FeeJobStatus.DONE);
            job.setMessage(created + " fee obligation(s) generated");
            jobRepository.save(job);
        } catch (Exception e) {
            markFailed(jobId, e);
        }
    }

    @Async("feeJobExecutor")
    public void runExport(Long jobId, AdminFeeRequests.ExportFeeReportRequest request) {
        try {
            FeeJob job = startJob(jobId);
            if (job == null) {
                return;
            }
            byte[] data = adminFeeService.exportReport(request);
            finishExport(jobId, data,
                    adminFeeService.exportFilename(request.getFormat()),
                    adminFeeService.exportContentType(request.getFormat()));
        } catch (Exception e) {
            markFailed(jobId, e);
        }
    }

    @Transactional
    protected FeeJob startJob(Long jobId) {
        FeeJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setStatus(FeeJobStatus.RUNNING);
            jobRepository.save(job);
        }
        return job;
    }

    @Transactional
    protected void finishExport(Long jobId, byte[] data, String filename, String contentType) {
        FeeJob job = jobRepository.findById(jobId).orElseThrow();
        job.setResultData(data);
        job.setResultFilename(filename);
        job.setResultContentType(contentType);
        job.setStatus(FeeJobStatus.DONE);
        job.setMessage("Export ready");
        jobRepository.save(job);
    }

    @Transactional
    protected void markFailed(Long jobId, Exception e) {
        jobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(FeeJobStatus.FAILED);
            String msg = e.getMessage();
            job.setMessage(msg != null && msg.length() > 480 ? msg.substring(0, 480) : msg);
            jobRepository.save(job);
        });
    }

    private List<Student> targetStudents(FeeStructure structure) {
        Long schoolId = structure.getSchool().getId();
        String className = structure.getTargetClassName();
        String section = structure.getTargetSection();
        if (className == null || className.equalsIgnoreCase("ALL")) {
            return studentRepository.findAllBySchool_IdOrderByClass(schoolId);
        }
        if (section != null && !section.isBlank() && !section.equalsIgnoreCase("ALL")) {
            return studentRepository.findBySchool_IdAndClassNameAndSection(schoolId, className, section);
        }
        return studentRepository.findBySchool_IdAndClassName(schoolId, className);
    }

    private LocalDate resolveDueDate(FeeStructure structure) {
        if (structure.getDueDate() != null) {
            return structure.getDueDate();
        }
        if (structure.getDueDayOfMonth() != null) {
            LocalDate now = LocalDate.now();
            int day = Math.min(structure.getDueDayOfMonth(), now.lengthOfMonth());
            return now.withDayOfMonth(day);
        }
        return LocalDate.now().plusDays(30);
    }
}
