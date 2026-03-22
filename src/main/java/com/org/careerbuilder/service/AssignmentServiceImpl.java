package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AssignmentStatus;
import com.org.careerbuilder.repository.AssignmentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentSubmissionRepository repo;

    private final String uploadDir = "uploads/assignments/";

    @Override
    public List<AssignmentCardResponse> getAssignments(Long studentId) {

        return repo.findByStudent_Id(studentId)
                .stream()
                .map(s -> new AssignmentCardResponse(
                        s.getAssignment().getId(),
                        s.getAssignment().getTitle(),
                        s.getAssignment().getSubject().getName(),
                        s.getAssignment().getTeacher().getFirstName(),
                        s.getAssignment().getDueDate(),
                        s.getStatus().name()
                ))
                .toList();
    }

    @Override
    public AssignmentSubmissionResponse submit(
            Long assignmentId,
            Long studentId,
            MultipartFile file,
            String comments
    ) {

        try {
            String fileName = studentId + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);

            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            AssignmentSubmission s =
                    repo.findByAssignment_IdAndStudent_Id(assignmentId, studentId)
                            .orElse(AssignmentSubmission.builder()
                                    .assignment(Assignment.builder().id(assignmentId).build())
                                    .student(Student.builder().id(studentId).build())
                                    .build());

            s.setFilePath(path.toString());
            s.setComments(comments);
            s.setSubmittedAt(LocalDateTime.now());

            if (LocalDateTime.now().toLocalDate()
                    .isAfter(s.getAssignment().getDueDate())) {
                s.setStatus(AssignmentStatus.LATE);
            } else {
                s.setStatus(AssignmentStatus.SUBMITTED);
            }

            repo.save(s);

            return new AssignmentSubmissionResponse(
                    s.getId(),
                    assignmentId,
                    s.getFilePath(),
                    s.getComments(),
                    s.getSubmittedAt(),
                    s.getStatus().name()
            );

        } catch (Exception e) {
            throw new RuntimeException("Upload failed");
        }
    }

    @Override
    public AssignmentSubmissionResponse getSubmission(Long assignmentId, Long studentId) {

        AssignmentSubmission s =
                repo.findByAssignment_IdAndStudent_Id(assignmentId, studentId)
                        .orElseThrow();

        return new AssignmentSubmissionResponse(
                s.getId(),
                s.getAssignment().getId(),
                s.getFilePath(),
                s.getComments(),
                s.getSubmittedAt(),
                s.getStatus().name()
        );
    }
}