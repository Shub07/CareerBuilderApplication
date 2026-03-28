package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AssignmentStatus;
import com.org.careerbuilder.repository.AssignmentRepository;
import com.org.careerbuilder.repository.AssignmentSubmissionRepository;
import com.org.careerbuilder.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentSubmissionRepository repo;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;

    private final String uploadDir = "uploads/assignments/";

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentCardResponse> getAssignments(Long studentId) {
        studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Map<Long, AssignmentSubmission> submissionByAssignmentId = repo.findByStudent_Id(studentId)
            .stream()
            .filter(s -> s.getAssignment() != null && s.getAssignment().getId() != null)
            .collect(Collectors.toMap(
                s -> s.getAssignment().getId(),
                s -> s,
                (first, second) -> first
            ));

        return assignmentRepository.findAll()
            .stream()
            .map(assignment -> {
                AssignmentSubmission submission = submissionByAssignmentId.get(assignment.getId());

                String status;
                if (submission != null) {
                status = "Submitted";
                } else if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDateTime.now().toLocalDate())) {
                status = "Overdue";
                } else {
                status = "Pending";
                }

                String teacherName = "Not assigned";
                try {
                    if (assignment.getTeacher() != null) {
                        String firstName = assignment.getTeacher().getFirstName() == null ? "" : assignment.getTeacher().getFirstName();
                        String lastName = assignment.getTeacher().getLastName() == null ? "" : assignment.getTeacher().getLastName();
                        String fullName = (firstName + " " + lastName).trim();
                        teacherName = fullName.isEmpty() ? "Not assigned" : fullName;
                    }
                } catch (Exception ignored) {
                    teacherName = "Not assigned";
                }

                String subjectName = "Unknown Subject";
                try {
                    if (assignment.getSubject() != null && assignment.getSubject().getName() != null) {
                        subjectName = assignment.getSubject().getName();
                    }
                } catch (Exception ignored) {
                    subjectName = "Unknown Subject";
                }

                return new AssignmentCardResponse(
                    assignment.getId(),
                    assignment.getTitle(),
                    subjectName,
                    teacherName,
                    assignment.getDueDate(),
                    status
                );
            })
            .toList();
    }

    @Override
    public AssignmentSubmissionResponse submit(
            Long assignmentId,
            Long studentId,
            MultipartFile file,
            String comments,
            boolean allowLate
    ) {

        try {
            Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

            boolean isLate = assignment.getDueDate() != null &&
                LocalDateTime.now().toLocalDate().isAfter(assignment.getDueDate());

            if (isLate && !allowLate) {
            throw new IllegalArgumentException("This assignment is overdue. Confirm late submission to continue.");
            }

            if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Assignment file is required.");
            }

            String originalFileName = Objects.requireNonNullElse(file.getOriginalFilename(), "assignment_file");
            String fileName = studentId + "_" + assignmentId + "_" + System.currentTimeMillis() + "_" + originalFileName;
            Path path = Paths.get(uploadDir + fileName);

            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            AssignmentSubmission s =
                    repo.findByAssignment_IdAndStudent_Id(assignmentId, studentId)
                            .orElse(AssignmentSubmission.builder()
                        .assignment(assignment)
                        .student(student)
                                    .build());

            s.setFilePath(path.toString());
            s.setComments(comments);
            s.setSubmittedAt(LocalDateTime.now());

            s.setStatus(isLate ? AssignmentStatus.LATE : AssignmentStatus.SUBMITTED);

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
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for assignment " + assignmentId));

        return new AssignmentSubmissionResponse(
                s.getId(),
                s.getAssignment().getId(),
                s.getFilePath(),
                s.getComments(),
                s.getSubmittedAt(),
                s.getStatus().name()
        );
    }

    @Override
    public Resource downloadSubmissionFile(Long assignmentId, Long studentId) {
        AssignmentSubmission submission = repo.findByAssignment_IdAndStudent_Id(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for assignment " + assignmentId));

        if (submission.getFilePath() == null || submission.getFilePath().isBlank()) {
            throw new ResourceNotFoundException("No file found for assignment submission " + assignmentId);
        }

        Path path = Paths.get(submission.getFilePath());
        Resource resource = new PathResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Submission file does not exist on server.");
        }

        return resource;
    }
}