package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.*;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface AssignmentService {

    List<AssignmentCardResponse> getAssignments(Long studentId);

    AssignmentSubmissionResponse submit(
            Long assignmentId,
            Long studentId,
            MultipartFile file,
            String comments,
            boolean allowLate
    );

    AssignmentSubmissionResponse getSubmission(
            Long assignmentId,
            Long studentId
    );

    Resource downloadSubmissionFile(
            Long assignmentId,
            Long studentId
    );


}