package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AttendanceRequest;
import com.org.careerbuilder.dto.request.BulkAttendanceRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.*;
import com.org.careerbuilder.models.enums.AttendanceStatus;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AttendanceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceBatchUploadRepository batchUploadRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public AttendanceRecordResponse markAttendance(AttendanceRequest request) {
        log.info("Marking attendance for student: {} on date: {}", request.getStudentId(), request.getDate());

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        AttendanceRecord existing = attendanceRecordRepository.findByDateAndStudentId(request.getDate(), request.getStudentId());
        if (existing != null) {
            existing.setStatus(request.getStatus());
            existing.setRemarks(request.getRemarks());
        } else {
            existing = AttendanceRecord.builder()
                    .student(student)
                    .date(request.getDate())
                    .status(request.getStatus())
                    .className(request.getClassName() != null ? request.getClassName() : student.getClassName())
                    .remarks(request.getRemarks())
                    .build();
        }

        AttendanceRecord saved = attendanceRecordRepository.save(existing);
        return convertToResponse(saved, student);
    }

    @Override
    public AttendanceRecordResponse updateAttendance(Long attendanceId, AttendanceRequest request) {
        log.info("Updating attendance record: {}", attendanceId);

        AttendanceRecord attendance = attendanceRecordRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + attendanceId));

        Student student = attendance.getStudent();
        
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }
        if (request.getRemarks() != null) {
            attendance.setRemarks(request.getRemarks());
        }

        AttendanceRecord updated = attendanceRecordRepository.save(attendance);
        return convertToResponse(updated, student);
    }

    @Override
    public AttendanceRecordResponse getAttendanceById(Long attendanceId) {
        log.info("Fetching attendance record: {}", attendanceId);

        AttendanceRecord attendance = attendanceRecordRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + attendanceId));

        return convertToResponse(attendance, attendance.getStudent());
    }

    @Override
    public void deleteAttendance(Long attendanceId) {
        log.info("Deleting attendance record: {}", attendanceId);

        if (!attendanceRecordRepository.existsById(attendanceId)) {
            throw new ResourceNotFoundException("Attendance record not found with ID: " + attendanceId);
        }
        attendanceRecordRepository.deleteById(attendanceId);
    }

    @Override
    public BulkAttendanceResponse bulkMarkAttendance(BulkAttendanceRequest request) {
        log.info("Processing bulk attendance marking for class: {}, section: {}", request.getClassName(), request.getSection());

        int successful = 0;
        int failed = 0;
        List<AttendanceRecord> recordsToSave = new ArrayList<>();

        for (BulkAttendanceRequest.AttendanceEntry entry : request.getRecords()) {
            try {
                Student student = studentRepository.findById(entry.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + entry.getStudentId()));

                recordsToSave.add(AttendanceRecord.builder()
                        .student(student)
                        .date(request.getDate())
                        .status(AttendanceStatus.valueOf(entry.getStatus()))
                        .className(request.getClassName())
                        .remarks(entry.getRemarks())
                        .build());
                successful++;
            } catch (Exception e) {
                log.error("Failed to mark attendance for student: {}", entry.getStudentId(), e);
                failed++;
            }
        }

        if (!recordsToSave.isEmpty()) {
            attendanceRecordRepository.saveAll(recordsToSave);
        }

        AttendanceBatchUpload batch = AttendanceBatchUpload.builder()
                .schoolId(request.getSchoolId())
                .className(request.getClassName())
                .section(request.getSection())
                .uploadDate(request.getDate())
                .uploadedById(1L) // Will be set from context
                .totalRecords(request.getRecords().size())
                .successfulRecords(successful)
                .failedRecords(failed)
                .uploadStatus(failed == 0 ? "COMPLETED" : "PARTIAL")
                .build();

        AttendanceBatchUpload savedBatch = batchUploadRepository.save(batch);
        log.info("Bulk attendance processing completed. Successful: {}, Failed: {}", successful, failed);

        return BulkAttendanceResponse.builder()
                .batchId(savedBatch.getId())
                .className(savedBatch.getClassName())
                .section(savedBatch.getSection())
                .totalRecords(savedBatch.getTotalRecords())
                .successfulRecords(savedBatch.getSuccessfulRecords())
                .failedRecords(savedBatch.getFailedRecords())
                .uploadStatus(savedBatch.getUploadStatus())
                .uploadedAt(savedBatch.getCreatedAt().toString())
                .build();
    }

    @Override
    public Page<AttendanceRecordResponse> getStudentAttendance(Long studentId, LocalDate from, LocalDate to, Pageable pageable) {
        log.info("Fetching attendance for student: {} from {} to {}", studentId, from, to);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        Page<AttendanceRecord> records = attendanceRecordRepository.findByStudent_IdAndDateBetween(studentId, from, to, pageable);

        return records.map(record -> convertToResponse(record, student));
    }

    @Override
    public AttendanceSummaryResponse getStudentAttendanceSummary(Long studentId, LocalDate from, LocalDate to) {
        log.info("Generating attendance summary for student: {} from {} to {}", studentId, from, to);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        long totalExpected = countWorkingDays(from, to);
        long totalPresent = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.PRESENT, from, to);
        long totalAbsent = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.ABSENT, from, to);
        long totalLeave = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LEAVE, from, to);

        int percentage = totalExpected > 0 ? (int) ((totalPresent * 100) / totalExpected) : 0;
        String status = percentage >= 75 ? "GOOD" : percentage >= 50 ? "WARNING" : "CRITICAL";

        return AttendanceSummaryResponse.builder()
                .studentId(studentId)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .className(student.getClassName())
                .section(student.getSection())
                .attendancePercentage(percentage)
                .totalDaysExpected(totalExpected)
                .totalDaysPresent(totalPresent)
                .totalDaysAbsent(totalAbsent)
                .totalDaysLeave(totalLeave)
                .periodStartDate(from)
                .periodEndDate(to)
                .status(status)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<AttendanceRecordResponse> getClassAttendance(String className, String section, LocalDate from, LocalDate to, Pageable pageable) {
        log.info("Fetching attendance for class: {} section: {}", className, section);

        Page<AttendanceRecord> records = attendanceRecordRepository.findByClassNameAndSectionAndDateBetween(className, section, from, to, pageable);

        return records.map(record -> convertToResponse(record, record.getStudent()));
    }

    @Override
    public ClassAttendanceReportResponse getClassAttendanceReport(String className, String section, LocalDate date) {
        log.info("Generating attendance report for class: {} section: {} on date: {}", className, section, date);

        List<AttendanceRecord> records = attendanceRecordRepository.findByDateAndClassNameAndSection(date, className, section);

        long presentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long leaveCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.LEAVE).count();
        long totalStudents = records.size();

        double percentage = totalStudents > 0 ? (presentCount * 100.0) / totalStudents : 0;

        return ClassAttendanceReportResponse.builder()
                .className(className)
                .section(section)
                .reportDate(date)
                .totalStudents((int) totalStudents)
                .presentCount((int) presentCount)
                .absentCount((int) absentCount)
                .leaveCount((int) leaveCount)
                .attendancePercentage(percentage)
                .generatedAt(LocalDate.now())
                .build();
    }

    @Override
    public List<ClassAttendanceReportResponse> getSchoolAttendanceReport(Long schoolId, LocalDate date) {
        log.info("Generating school-wide attendance report for date: {}", date);

        List<AttendanceRecord> allRecords = attendanceRecordRepository.findByDateAndStatus(date, AttendanceStatus.PRESENT);
        
        // Group by class and section
        return allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getClassName() + "-" + r.getSection()))
                .values().stream()
                .map(groupRecords -> {
                    AttendanceRecord sample = groupRecords.get(0);
                    long presentCount = groupRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
                    long absentCount = groupRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
                    long leaveCount = groupRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.LEAVE).count();
                    double percentage = (presentCount * 100.0) / groupRecords.size();

                    return ClassAttendanceReportResponse.builder()
                            .className(sample.getClassName())
                            .section(sample.getSection())
                            .reportDate(date)
                            .totalStudents(groupRecords.size())
                            .presentCount((int) presentCount)
                            .absentCount((int) absentCount)
                            .leaveCount((int) leaveCount)
                            .attendancePercentage(percentage)
                            .generatedAt(LocalDate.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public int getAttendancePercentage(Long studentId, LocalDate from, LocalDate to) {
        long total = attendanceRecordRepository.countByStudent_IdAndDateBetween(studentId, from, to);
        if (total == 0) return 0;
        long present = attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.PRESENT, from, to);
        return (int) ((present * 100) / total);
    }

    @Override
    public long getTotalAbsent(Long studentId, LocalDate from, LocalDate to) {
        return attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.ABSENT, from, to);
    }

    @Override
    public long getTotalPresent(Long studentId, LocalDate from, LocalDate to) {
        return attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.PRESENT, from, to);
    }

    @Override
    public long getTotalLeave(Long studentId, LocalDate from, LocalDate to) {
        return attendanceRecordRepository.countByStudent_IdAndStatusAndDateBetween(
                studentId, AttendanceStatus.LEAVE, from, to);
    }

    // Helper methods
    private AttendanceRecordResponse convertToResponse(AttendanceRecord record, Student student) {
        return AttendanceRecordResponse.builder()
                .attendanceId(record.getId())
                .studentId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .rollNo(student.getRollNo() != null ? student.getRollNo().toString() : "")
                .date(record.getDate())
                .status(record.getStatus())
                .className(record.getClassName())
                .remarks(record.getRemarks())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private long countWorkingDays(LocalDate from, LocalDate to) {
        long count = 0;
        LocalDate current = from;
        while (!current.isAfter(to)) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (dayOfWeek < 6) { // Monday to Friday
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }
}

