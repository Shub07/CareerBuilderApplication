package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.TeacherSelfAttendanceDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.TeacherAttendanceEntry;
import com.org.careerbuilder.models.TeacherLeaveBalance;
import com.org.careerbuilder.models.TeacherLeaveRequest;
import com.org.careerbuilder.models.enums.TeacherAttendanceStatus;
import com.org.careerbuilder.models.enums.TeacherLeaveStatus;
import com.org.careerbuilder.models.enums.TeacherLeaveType;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.repository.TeacherAttendanceEntryRepository;
import com.org.careerbuilder.repository.TeacherLeaveBalanceRepository;
import com.org.careerbuilder.repository.TeacherLeaveRequestRepository;
import com.org.careerbuilder.service.TeacherSelfAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TeacherSelfAttendanceServiceImpl implements TeacherSelfAttendanceService {

    private static final LocalTime LATE_AFTER = LocalTime.of(9, 0);
    private static final int HALF_DAY_MAX_MINUTES = 4 * 60;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private final FacultyRepository facultyRepository;
    private final TeacherAttendanceEntryRepository teacherAttendanceEntryRepository;
    private final TeacherLeaveRequestRepository teacherLeaveRequestRepository;
    private final TeacherLeaveBalanceRepository teacherLeaveBalanceRepository;

    private Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherSelfAttendanceDtos.DashboardResponse getDashboard(Long facultyId) {
        Faculty faculty = loadFaculty(facultyId);
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
        LocalDate sunday = monday.plusDays(6);

        Map<LocalDate, TeacherAttendanceEntry> entriesByDate = new HashMap<>();
        for (TeacherAttendanceEntry e : teacherAttendanceEntryRepository
                .findByFaculty_IdAndWorkDateBetweenOrderByWorkDateAsc(facultyId, monday, sunday)) {
            entriesByDate.put(e.getWorkDate(), e);
        }

        TeacherSelfAttendanceDtos.TodayStatusCard todayCard = toTodayCard(entriesByDate.get(today), hasLeaveForDate(facultyId, today));

        List<TeacherSelfAttendanceDtos.WeekRow> weekRows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LocalDate day = monday.plusDays(i);
            TeacherAttendanceEntry entry = entriesByDate.get(day);
            String status = entry != null ? entry.getStatus().name() : (hasLeaveForDate(facultyId, day) ? "LEAVE" : "--");
            weekRows.add(new TeacherSelfAttendanceDtos.WeekRow(
                    day,
                    day.getDayOfWeek().name().substring(0, 1) + day.getDayOfWeek().name().substring(1, 3).toLowerCase(Locale.ROOT),
                    entry != null ? fmt(entry.getCheckInTime()) : "--",
                    entry != null ? fmt(entry.getCheckOutTime()) : "--",
                    entry != null ? minutesLabel(entry.getWorkedMinutes()) : "--",
                    status
            ));
        }

        TeacherSelfAttendanceDtos.LeaveBalanceCard leaveBalance = buildLeaveBalanceCard(faculty);
        return new TeacherSelfAttendanceDtos.DashboardResponse(todayCard, weekRows, leaveBalance);
    }

    @Override
    @Transactional
    public TeacherSelfAttendanceDtos.TodayStatusCard checkIn(Long facultyId, TeacherSelfAttendanceDtos.CheckInRequest request) {
        Faculty faculty = loadFaculty(facultyId);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        TeacherAttendanceEntry entry = teacherAttendanceEntryRepository.findByFaculty_IdAndWorkDate(facultyId, today)
                .orElse(TeacherAttendanceEntry.builder()
                        .faculty(faculty)
                        .schoolId(faculty.getSchool().getId())
                        .workDate(today)
                        .build());

        if (entry.getCheckInTime() != null) {
            throw new IllegalStateException("Already checked in for today");
        }
        entry.setCheckInTime(now);
        entry.setLateNote(request != null ? request.lateNote() : null);
        entry.setStatus(now.isAfter(LATE_AFTER) ? TeacherAttendanceStatus.LATE : TeacherAttendanceStatus.PRESENT);
        teacherAttendanceEntryRepository.save(entry);
        return toTodayCard(entry, false);
    }

    @Override
    @Transactional
    public TeacherSelfAttendanceDtos.TodayStatusCard checkOut(Long facultyId, TeacherSelfAttendanceDtos.CheckOutRequest request) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        TeacherAttendanceEntry entry = teacherAttendanceEntryRepository.findByFaculty_IdAndWorkDate(facultyId, today)
                .orElseThrow(() -> new IllegalStateException("Check-in required before check-out"));
        if (entry.getCheckInTime() == null) {
            throw new IllegalStateException("Check-in required before check-out");
        }
        if (entry.getCheckOutTime() != null) {
            throw new IllegalStateException("Already checked out for today");
        }
        entry.setCheckOutTime(now);
        entry.setWorkSummary(request != null ? request.workSummary() : null);
        int worked = (int) Duration.between(entry.getCheckInTime(), now).toMinutes();
        entry.setWorkedMinutes(Math.max(worked, 0));
        if (entry.getWorkedMinutes() <= HALF_DAY_MAX_MINUTES) {
            entry.setStatus(TeacherAttendanceStatus.HALF_DAY);
        } else if (entry.getCheckInTime().isAfter(LATE_AFTER)) {
            entry.setStatus(TeacherAttendanceStatus.LATE);
        } else {
            entry.setStatus(TeacherAttendanceStatus.PRESENT);
        }
        teacherAttendanceEntryRepository.save(entry);
        return toTodayCard(entry, false);
    }

    @Override
    @Transactional
    public void applyLeave(Long facultyId, TeacherSelfAttendanceDtos.LeaveApplyRequest request, MultipartFile document) {
        Faculty faculty = loadFaculty(facultyId);
        if (request.toDate().isBefore(request.fromDate())) {
            throw new IllegalArgumentException("toDate must be on or after fromDate");
        }
        Faculty substitute = null;
        if (request.substituteFacultyId() != null) {
            substitute = facultyRepository.findByIdAndSchool_IdWithDetails(
                            request.substituteFacultyId(), faculty.getSchool().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Substitute teacher not found"));
            if (substitute.getId().equals(facultyId)) {
                throw new IllegalArgumentException("Cannot select yourself as substitute");
            }
        }
        TeacherLeaveRequest leave = TeacherLeaveRequest.builder()
                .faculty(faculty)
                .schoolId(faculty.getSchool().getId())
                .leaveType(request.leaveType())
                .fromDate(request.fromDate())
                .toDate(request.toDate())
                .reason(request.reason().trim())
                .status(TeacherLeaveStatus.APPLIED)
                .substituteFaculty(substitute)
                .build();
        if (document != null && !document.isEmpty()) {
            leave.setDocumentPath(storeLeaveDocument(facultyId, document));
            leave.setDocumentOriginalName(document.getOriginalFilename());
            leave.setDocumentContentType(document.getContentType());
        }
        teacherLeaveRequestRepository.save(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSelfAttendanceDtos.SubstituteTeacherOption> listSubstituteTeachers(Long facultyId) {
        Faculty faculty = loadFaculty(facultyId);
        return facultyRepository.findColleaguesBySchoolExcluding(faculty.getSchool().getId(), facultyId).stream()
                .map(f -> new TeacherSelfAttendanceDtos.SubstituteTeacherOption(
                        f.getId(),
                        (f.getFirstName() + " " + f.getLastName()).trim()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadLeaveDocument(Long facultyId, Long leaveId) {
        TeacherLeaveRequest leave = teacherLeaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher leave request not found"));
        if (!leave.getFaculty().getId().equals(facultyId)) {
            throw new ResourceNotFoundException("Teacher leave request not found");
        }
        if (leave.getDocumentPath() == null || leave.getDocumentPath().isBlank()) {
            throw new ResourceNotFoundException("No document attached to this leave request");
        }
        Path path = Paths.get(leave.getDocumentPath());
        PathResource resource = new PathResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Document file not found on server");
        }
        String filename = leave.getDocumentOriginalName() != null ? leave.getDocumentOriginalName() : "leave_document";
        return new PathResource(path) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private String storeLeaveDocument(Long facultyId, MultipartFile file) {
        try {
            Path dir = Paths.get("uploads", "teacher-leave-documents", String.valueOf(facultyId));
            Files.createDirectories(dir);
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
            String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(UUID.randomUUID() + "_" + safe);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store leave document", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSelfAttendanceDtos.AttendanceHistoryRow> getAttendanceHistory(
            Long facultyId, LocalDate fromDate, LocalDate toDate, String statusFilter) {
        loadFaculty(facultyId);
        LocalDate to = toDate != null ? toDate : LocalDate.now();
        LocalDate from = fromDate != null ? fromDate : to.minusDays(60);
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("fromDate must be on or before toDate");
        }

        Map<LocalDate, TeacherAttendanceEntry> entriesByDate = new HashMap<>();
        for (TeacherAttendanceEntry e : teacherAttendanceEntryRepository
                .findByFaculty_IdAndWorkDateBetweenOrderByWorkDateAsc(facultyId, from, to)) {
            entriesByDate.put(e.getWorkDate(), e);
        }

        String normalizedStatus = statusFilter == null || statusFilter.isBlank()
                ? null
                : statusFilter.trim().toUpperCase(Locale.ROOT);

        List<TeacherSelfAttendanceDtos.AttendanceHistoryRow> rows = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            TeacherAttendanceEntry entry = entriesByDate.get(day);
            String status;
            if (entry != null) {
                status = entry.getStatus().name();
            } else if (hasLeaveForDate(facultyId, day)) {
                status = "LEAVE";
            } else {
                status = "--";
            }
            if (normalizedStatus != null && !normalizedStatus.equals("ALL") && !status.equals(normalizedStatus)) {
                continue;
            }
            String dayLabel = day.getDayOfWeek().name().substring(0, 1)
                    + day.getDayOfWeek().name().substring(1, 3).toLowerCase(Locale.ROOT)
                    + ", " + day.getDayOfMonth() + " "
                    + day.getMonth().name().substring(0, 1)
                    + day.getMonth().name().substring(1, 3).toLowerCase(Locale.ROOT);
            rows.add(new TeacherSelfAttendanceDtos.AttendanceHistoryRow(
                    day,
                    dayLabel,
                    entry != null ? fmt(entry.getCheckInTime()) : "--",
                    entry != null ? fmt(entry.getCheckOutTime()) : "--",
                    entry != null ? minutesLabel(entry.getWorkedMinutes()) : "--",
                    status
            ));
        }
        rows.sort(Comparator.comparing(TeacherSelfAttendanceDtos.AttendanceHistoryRow::date).reversed());
        return rows;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSelfAttendanceDtos.LeaveHistoryItem> getLeaveHistory(Long facultyId, TeacherLeaveStatus status) {
        loadFaculty(facultyId);
        List<TeacherLeaveRequest> leaves = status == null
                ? teacherLeaveRequestRepository.findByFaculty_IdOrderByCreatedAtDesc(facultyId)
                : teacherLeaveRequestRepository.findByFaculty_IdAndStatusOrderByCreatedAtDesc(facultyId, status);
        return leaves.stream().map(this::toLeaveHistoryItem).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSelfAttendanceDtos.AdminLeaveItem> getAdminLeaveRequests(Long schoolId, TeacherLeaveStatus status) {
        List<TeacherLeaveRequest> leaves = status == null
                ? teacherLeaveRequestRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId)
                : teacherLeaveRequestRepository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, status);
        return leaves.stream().map(this::toAdminLeaveItem).toList();
    }

    @Override
    @Transactional
    public TeacherSelfAttendanceDtos.AdminLeaveItem updateLeaveStatus(Long leaveId, TeacherSelfAttendanceDtos.LeaveActionRequest request) {
        TeacherLeaveRequest leave = teacherLeaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher leave request not found"));
        if (request.status() == TeacherLeaveStatus.REJECTED &&
                (request.rejectionReason() == null || request.rejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting leave");
        }
        if (request.status() == TeacherLeaveStatus.APPLIED) {
            throw new IllegalArgumentException("Cannot move leave request back to APPLIED");
        }
        leave.setStatus(request.status());
        if (request.status() == TeacherLeaveStatus.REJECTED) {
            leave.setRejectionReason(request.rejectionReason().trim());
        } else {
            leave.setRejectionReason(null);
        }
        teacherLeaveRequestRepository.save(leave);
        return toAdminLeaveItem(leave);
    }

    private boolean hasLeaveForDate(Long facultyId, LocalDate date) {
        return !teacherLeaveRequestRepository.findByFaculty_IdAndStatusInAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                facultyId,
                List.of(TeacherLeaveStatus.APPLIED, TeacherLeaveStatus.APPROVED),
                date,
                date
        ).isEmpty();
    }

    private TeacherSelfAttendanceDtos.TodayStatusCard toTodayCard(TeacherAttendanceEntry entry, boolean onLeave) {
        if (onLeave && entry == null) {
            return new TeacherSelfAttendanceDtos.TodayStatusCard("On Leave", "LEAVE", "--", "--", "--");
        }
        if (entry == null || entry.getCheckInTime() == null) {
            return new TeacherSelfAttendanceDtos.TodayStatusCard("Not Checked-in", "NOT_CHECKED_IN", "--", "--", "--");
        }
        if (entry.getCheckOutTime() == null) {
            return new TeacherSelfAttendanceDtos.TodayStatusCard("Checked-in", "CHECKED_IN",
                    fmt(entry.getCheckInTime()), "--", "Calculating...");
        }
        return new TeacherSelfAttendanceDtos.TodayStatusCard("Checked-out", "CHECKED_OUT",
                fmt(entry.getCheckInTime()), fmt(entry.getCheckOutTime()), minutesLabel(entry.getWorkedMinutes()));
    }

    private TeacherSelfAttendanceDtos.LeaveBalanceCard buildLeaveBalanceCard(Faculty faculty) {
        TeacherLeaveBalance balance = teacherLeaveBalanceRepository.findByFaculty_Id(faculty.getId())
                .orElseGet(() -> teacherLeaveBalanceRepository.save(TeacherLeaveBalance.builder()
                        .faculty(faculty)
                        .casualTotal(12)
                        .medicalTotal(10)
                        .halfDayTotal(6)
                        .build()));

        List<TeacherLeaveStatus> activeStatuses = List.of(TeacherLeaveStatus.APPLIED, TeacherLeaveStatus.APPROVED);
        int usedCasual = (int) teacherLeaveRequestRepository.countByFaculty_IdAndLeaveTypeAndStatusIn(
                faculty.getId(), TeacherLeaveType.CASUAL, activeStatuses);
        int usedMedical = (int) teacherLeaveRequestRepository.countByFaculty_IdAndLeaveTypeAndStatusIn(
                faculty.getId(), TeacherLeaveType.MEDICAL, activeStatuses);
        int usedHalfDay = (int) teacherLeaveRequestRepository.countByFaculty_IdAndLeaveTypeAndStatusIn(
                faculty.getId(), TeacherLeaveType.HALF_DAY, activeStatuses);

        return new TeacherSelfAttendanceDtos.LeaveBalanceCard(
                new TeacherSelfAttendanceDtos.Bucket(Math.max(balance.getCasualTotal() - usedCasual, 0), balance.getCasualTotal()),
                new TeacherSelfAttendanceDtos.Bucket(Math.max(balance.getMedicalTotal() - usedMedical, 0), balance.getMedicalTotal()),
                new TeacherSelfAttendanceDtos.Bucket(Math.max(balance.getHalfDayTotal() - usedHalfDay, 0), balance.getHalfDayTotal())
        );
    }

    private String fmt(LocalTime t) {
        return t == null ? "--" : TIME_FMT.format(t);
    }

    private String minutesLabel(Integer minutes) {
        if (minutes == null) return "--";
        int h = minutes / 60;
        int m = minutes % 60;
        return h + "h " + m + "m";
    }

    private TeacherSelfAttendanceDtos.LeaveHistoryItem toLeaveHistoryItem(TeacherLeaveRequest leave) {
        int totalDays = (int) ChronoUnit.DAYS.between(leave.getFromDate(), leave.getToDate()) + 1;
        boolean hasDocument = leave.getDocumentPath() != null && !leave.getDocumentPath().isBlank();
        Long documentSizeBytes = null;
        if (hasDocument) {
            try {
                documentSizeBytes = Files.size(Paths.get(leave.getDocumentPath()));
            } catch (IOException ignored) {
                // size unavailable if file missing
            }
        }
        String substituteName = null;
        if (leave.getSubstituteFaculty() != null) {
            Faculty sub = leave.getSubstituteFaculty();
            substituteName = (sub.getFirstName() + " " + sub.getLastName()).trim();
        }
        return new TeacherSelfAttendanceDtos.LeaveHistoryItem(
                leave.getId(),
                leave.getLeaveType().name(),
                leave.getFromDate(),
                leave.getToDate(),
                Math.max(totalDays, 1),
                leave.getReason(),
                leave.getRejectionReason(),
                leave.getStatus().name(),
                leave.getCreatedAt(),
                leave.getUpdatedAt(),
                hasDocument,
                leave.getDocumentOriginalName(),
                leave.getDocumentContentType(),
                documentSizeBytes,
                substituteName
        );
    }

    private TeacherSelfAttendanceDtos.AdminLeaveItem toAdminLeaveItem(TeacherLeaveRequest leave) {
        Faculty f = leave.getFaculty();
        String facultyName = (f.getFirstName() + " " + f.getLastName()).trim();
        return new TeacherSelfAttendanceDtos.AdminLeaveItem(
                leave.getId(),
                f.getId(),
                facultyName,
                leave.getLeaveType().name(),
                leave.getFromDate(),
                leave.getToDate(),
                leave.getReason(),
                leave.getRejectionReason(),
                leave.getStatus().name(),
                leave.getCreatedAt()
        );
    }
}
