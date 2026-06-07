package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminFeeRequests;
import com.org.careerbuilder.dto.response.AdminFeeDtos;
import com.org.careerbuilder.models.FeeJob;

public interface FeeJobService {

    /** Queues async materialisation of fee obligations for a structure's target students. */
    AdminFeeDtos.JobStatusResponse enqueueObligationGeneration(Long schoolId, Long structureId, String performedBy);

    /** Queues an async report export; poll status then download the artifact. */
    AdminFeeDtos.JobStatusResponse enqueueExport(AdminFeeRequests.ExportFeeReportRequest request);

    AdminFeeDtos.JobStatusResponse getStatus(Long schoolId, Long jobId);

    /** Returns a completed job whose artifact is ready for download. */
    FeeJob getDownloadable(Long schoolId, Long jobId);
}
