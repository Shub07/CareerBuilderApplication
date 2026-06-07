package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminFeeRequests;
import com.org.careerbuilder.dto.response.AdminFeeDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Fee;
import com.org.careerbuilder.models.FeeStructure;
import com.org.careerbuilder.models.FeeTransaction;
import com.org.careerbuilder.models.School;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.StudentProfile;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.FeeFrequency;
import com.org.careerbuilder.models.enums.FeePaymentMode;
import com.org.careerbuilder.models.enums.FeeTransactionType;
import com.org.careerbuilder.models.NotificationOutbox;
import com.org.careerbuilder.models.enums.NotificationChannel;
import com.org.careerbuilder.models.enums.NotificationStatus;
import com.org.careerbuilder.repository.FeeRepository;
import com.org.careerbuilder.repository.FeeStructureRepository;
import com.org.careerbuilder.repository.FeeTransactionRepository;
import com.org.careerbuilder.repository.NotificationOutboxRepository;
import com.org.careerbuilder.repository.ReceiptCounterRepository;
import com.org.careerbuilder.repository.SchoolRepository;
import com.org.careerbuilder.repository.StudentProfileRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.repository.projection.StudentFeeAggregate;
import com.org.careerbuilder.service.AdminFeeService;
import com.org.careerbuilder.service.FeeJobService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import com.org.careerbuilder.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminFeeServiceImpl implements AdminFeeService {

    private static final Locale INDIA = new Locale("en", "IN");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final FeeRepository feeRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeeTransactionRepository feeTransactionRepository;
    private final ReceiptCounterRepository receiptCounterRepository;
    private final StudentRepository studentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SchoolRepository schoolRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final AdminActivityLogger activityLogger;
    private final FeeJobService feeJobService;

    // ─── List page ──────────────────────────────────────────────────────────────

    @Override
    @Cacheable(cacheNames = CacheConfig.FEE_STATS_CACHE, key = "#schoolId")
    @Transactional(readOnly = true)
    public AdminFeeDtos.FeeStatsResponse getStats(Long schoolId) {
        LocalDate today = LocalDate.now();
        BigDecimal collected = nz(feeRepository.sumCollectedBySchool(schoolId));
        BigDecimal pending = nz(feeRepository.sumPendingAmountBySchool(schoolId));
        BigDecimal overdue = nz(feeRepository.sumOverdueBySchool(schoolId, today));
        BigDecimal thisMonth = nz(feeTransactionRepository.sumNetCollectedBetween(
                schoolId, today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth())));
        return new AdminFeeDtos.FeeStatsResponse(
                collected, pending, overdue, thisMonth,
                inr(collected), inr(pending), inr(overdue), inr(thisMonth));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFeeDtos.StudentFeeListResponse listStudentFees(
            Long schoolId, String q, String className, String status, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normStatus = normalizeStatus(status);
        LocalDate today = LocalDate.now();

        List<StudentFeeAggregate> rows = feeRepository.aggregateStudentFees(
                schoolId, blankToNull(q), blankToNull(className), normStatus, today,
                PageRequest.of(safePage, safeSize));
        long total = feeRepository.countStudentFees(schoolId, blankToNull(q), blankToNull(className), normStatus, today);

        List<AdminFeeDtos.StudentFeeRow> mapped = rows.stream().map(a -> {
            BigDecimal totalFees = nz(a.getTotalFees());
            BigDecimal paid = nz(a.getPaidFees());
            BigDecimal overdue = nz(a.getOverdueFees());
            BigDecimal pending = totalFees.subtract(paid).max(BigDecimal.ZERO);
            String st = deriveStatus(totalFees, paid, overdue);
            return new AdminFeeDtos.StudentFeeRow(
                    a.getStudentId(),
                    a.getFirstName() + " " + a.getLastName(),
                    classLabel(a.getClassName(), a.getSection()),
                    totalFees, paid, pending,
                    st, Fee.FeeStatus.valueOf(st).getLabel(),
                    pending.signum() > 0, paid.signum() > 0, overdue.signum() > 0);
        }).toList();

        int totalPages = (int) Math.ceil((double) total / safeSize);
        String showing = showingLabel(total, safePage, safeSize, mapped.size());
        return new AdminFeeDtos.StudentFeeListResponse(
                mapped, safePage, safeSize, total, totalPages, showing);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFeeDtos.FeeFilterOptionsResponse getFilterOptions(Long schoolId) {
        List<String> classes = feeRepository.findDistinctClassNames(schoolId);
        List<AdminFeeDtos.FeeOption> statuses = List.of(
                new AdminFeeDtos.FeeOption("ALL", "All"),
                new AdminFeeDtos.FeeOption("PAID", "Paid"),
                new AdminFeeDtos.FeeOption("PARTIAL", "Partially Paid"),
                new AdminFeeDtos.FeeOption("OVERDUE", "Overdue"),
                new AdminFeeDtos.FeeOption("PENDING", "Pending"));
        List<AdminFeeDtos.FeeOption> feeTypes = feeStructureRepository.findActiveBySchool(schoolId).stream()
                .map(FeeStructure::getFeeType).distinct()
                .map(t -> new AdminFeeDtos.FeeOption(t, t)).toList();
        List<AdminFeeDtos.FeeOption> modes = new ArrayList<>();
        for (FeePaymentMode m : FeePaymentMode.values()) {
            modes.add(new AdminFeeDtos.FeeOption(m.name(), m.getLabel()));
        }
        return new AdminFeeDtos.FeeFilterOptionsResponse(classes, statuses, feeTypes, modes);
    }

    // ─── Fee structures ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminFeeDtos.FeeStructureListResponse getStructures(Long schoolId) {
        List<FeeStructure> structures = feeStructureRepository.findActiveBySchool(schoolId);
        long active = feeStructureRepository.countActiveBySchool(schoolId);
        BigDecimal avg = nz(feeStructureRepository.averageAmountBySchool(schoolId)).setScale(0, RoundingMode.HALF_UP);
        long totalStudents = studentRepository.countBySchool_Id(schoolId);
        AdminFeeDtos.FeeStructureStatsResponse stats = new AdminFeeDtos.FeeStructureStatsResponse(
                active, avg, totalStudents, inr(avg));
        List<AdminFeeDtos.FeeStructureRow> rows = structures.stream().map(this::toStructureRow).toList();
        return new AdminFeeDtos.FeeStructureListResponse(stats, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminFeeDtos.FeeStructureRow createStructure(AdminFeeRequests.CreateFeeStructureRequest request) {
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        FeeFrequency frequency = parseFrequency(request.getFrequency());
        String academicYear = request.getAcademicYear() != null ? request.getAcademicYear() : currentAcademicYear();

        FeeStructure structure = FeeStructure.builder()
                .school(school)
                .feeType(request.getFeeType())
                .feeName(blankToNull(request.getFeeName()) != null ? request.getFeeName() : request.getFeeType())
                .targetClassName(request.getTargetClassName())
                .targetSection(blankToNull(request.getTargetSection()))
                .academicYear(academicYear)
                .financialYear(request.getFinancialYear())
                .amount(request.getAmount())
                .frequency(frequency)
                .dueDate(request.getDueDate())
                .dueDayOfMonth(request.getDueDayOfMonth())
                .lateFeeEnabled(request.isLateFeeEnabled())
                .notes(request.getNotes())
                .active(true)
                .deleted(false)
                .createdBy(request.getPerformedBy())
                .build();
        structure = feeStructureRepository.save(structure);

        if (request.isGenerateForStudents()) {
            // Materialising obligations for a whole class can be large; run it as a
            // background job that fires once this transaction commits.
            feeJobService.enqueueObligationGeneration(
                    school.getId(), structure.getId(), request.getPerformedBy());
        }

        activityLogger.log(school.getId(), AdminActivityType.FEE_STRUCTURE_CREATED,
                "Fee structure created: " + structure.getFeeName(),
                structure.getTargetClassName() + " • " + inr(structure.getAmount())
                        + (request.isGenerateForStudents() ? " • generating obligations…" : ""),
                "FEE_STRUCTURE", structure.getId(), request.getPerformedBy());
        return toStructureRow(structure);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminFeeDtos.FeeStructureRow updateStructure(
            Long structureId, AdminFeeRequests.UpdateFeeStructureRequest request) {
        FeeStructure structure = feeStructureRepository
                .findByIdAndSchool_IdAndDeletedFalse(structureId, request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found"));
        if (blankToNull(request.getFeeName()) != null) {
            structure.setFeeName(request.getFeeName());
        }
        if (request.getAmount() != null) {
            structure.setAmount(request.getAmount());
        }
        if (blankToNull(request.getFrequency()) != null) {
            structure.setFrequency(parseFrequency(request.getFrequency()));
        }
        if (request.getDueDate() != null) {
            structure.setDueDate(request.getDueDate());
        }
        if (request.getDueDayOfMonth() != null) {
            structure.setDueDayOfMonth(request.getDueDayOfMonth());
        }
        if (request.getLateFeeEnabled() != null) {
            structure.setLateFeeEnabled(request.getLateFeeEnabled());
        }
        if (request.getActive() != null) {
            structure.setActive(request.getActive());
        }
        if (request.getNotes() != null) {
            structure.setNotes(request.getNotes());
        }
        feeStructureRepository.save(structure);
        activityLogger.log(request.getSchoolId(), AdminActivityType.FEE_STRUCTURE_UPDATED,
                "Fee structure updated: " + structure.getFeeName(), null,
                "FEE_STRUCTURE", structure.getId(), request.getPerformedBy());
        return toStructureRow(structure);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminFeeDtos.ActionResponse deleteStructure(Long schoolId, Long structureId, String performedBy) {
        FeeStructure structure = feeStructureRepository
                .findByIdAndSchool_IdAndDeletedFalse(structureId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found"));
        structure.setDeleted(true);
        structure.setActive(false);
        feeStructureRepository.save(structure);
        activityLogger.log(schoolId, AdminActivityType.FEE_STRUCTURE_DELETED,
                "Fee structure deleted: " + structure.getFeeName(), null,
                "FEE_STRUCTURE", structure.getId(), performedBy);
        return new AdminFeeDtos.ActionResponse(true, "Fee structure deleted");
    }

    // ─── Collect / refund ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AdminFeeDtos.StudentSearchOption> searchStudents(Long schoolId, String q) {
        if (blankToNull(q) == null) {
            return List.of();
        }
        List<Student> students = studentRepository.searchBySchoolText(schoolId, q.trim());
        List<Student> limited = students.size() > 20 ? students.subList(0, 20) : students;
        Map<Long, StudentProfile> profiles = profilesFor(limited);
        return limited.stream().map(s -> {
            StudentProfile p = profiles.get(s.getId());
            return new AdminFeeDtos.StudentSearchOption(
                    s.getId(), s.getFirstName() + " " + s.getLastName(),
                    p != null ? p.getAdmissionNumber() : null,
                    classLabel(s.getClassName(), s.getSection()));
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminFeeDtos.PaidFeeOption> getPaidFees(Long schoolId, Long studentId) {
        requireStudent(schoolId, studentId);
        return feeTransactionRepository.findByStudentAndType(studentId, FeeTransactionType.COLLECTION).stream()
                .map(t -> new AdminFeeDtos.PaidFeeOption(
                        t.getId(),
                        t.getFee() != null ? t.getFee().getId() : null,
                        t.getFeeType() + " — " + inr(t.getAmount()) + " (" + t.getTransactionDate().format(DATE_FMT) + ")",
                        t.getFeeType(), t.getAmount(), t.getTransactionDate()))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConfig.FEE_STATS_CACHE, key = "#request.schoolId")
    public AdminFeeDtos.CollectFeeResponse collectFee(
            AdminFeeRequests.CollectFeeRequest request, String idempotencyKey) {
        String idemKey = blankToNull(idempotencyKey);
        if (idemKey != null) {
            Optional<FeeTransaction> replay = feeTransactionRepository
                    .findBySchool_IdAndIdempotencyKey(request.getSchoolId(), idemKey);
            if (replay.isPresent()) {
                FeeTransaction t = replay.get();
                return new AdminFeeDtos.CollectFeeResponse(true,
                        "Fee already collected (idempotent replay)", buildReceipt(t, t.getStudent()));
            }
        }
        Student student = requireStudent(request.getSchoolId(), request.getStudentId());
        School school = student.getSchool();
        FeePaymentMode mode = parseMode(request.getPaymentMethod());
        LocalDate date = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now();
        BigDecimal amount = request.getAmount();

        Fee fee = resolveCollectibleFee(student, request);
        BigDecimal feeAmount = fee.getAmountAsDecimal();
        // Cap paid at the obligation amount to avoid overpayment artefacts.
        BigDecimal newPaid = nz(fee.getPaidAmount()).add(amount);
        if (newPaid.compareTo(feeAmount) > 0) {
            newPaid = feeAmount;
        }
        fee.setPaidAmount(newPaid);
        fee.setStatus(computeStatus(feeAmount, newPaid, fee.getDueDate()));
        feeRepository.save(fee);

        FeeTransaction txn = FeeTransaction.builder()
                .school(school)
                .student(student)
                .fee(fee)
                .feeType(request.getFeeType())
                .type(FeeTransactionType.COLLECTION)
                .amount(amount)
                .mode(mode)
                .transactionDate(date)
                .referenceNumber(blankToNull(request.getReferenceNumber()))
                .notes(blankToNull(request.getNotes()))
                .receiptNumber(generateReceiptNumber())
                .collectedBy(request.getPerformedBy())
                .idempotencyKey(idemKey)
                .build();
        txn = feeTransactionRepository.save(txn);

        activityLogger.log(school.getId(), AdminActivityType.FEE_COLLECTED,
                "Fee collected: " + student.getFirstName() + " " + student.getLastName(),
                request.getFeeType() + " • " + inr(amount) + " • " + mode.getLabel(),
                "STUDENT", student.getId(), request.getPerformedBy());

        return new AdminFeeDtos.CollectFeeResponse(true,
                "Fee collected successfully", buildReceipt(txn, student));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConfig.FEE_STATS_CACHE, key = "#request.schoolId")
    public AdminFeeDtos.RefundResponse refundFee(
            AdminFeeRequests.RefundFeeRequest request, String idempotencyKey) {
        String idemKey = blankToNull(idempotencyKey);
        if (idemKey != null) {
            Optional<FeeTransaction> replay = feeTransactionRepository
                    .findBySchool_IdAndIdempotencyKey(request.getSchoolId(), idemKey);
            if (replay.isPresent()) {
                return new AdminFeeDtos.RefundResponse(true,
                        "Refund already processed (idempotent replay)", replay.get().getReceiptNumber());
            }
        }
        Student student = requireStudent(request.getSchoolId(), request.getStudentId());
        School school = student.getSchool();
        FeePaymentMode mode = parseMode(request.getRefundMethod());
        BigDecimal refund = request.getRefundAmount();

        Fee fee = null;
        String feeType = null;
        if (request.getSourceTransactionId() != null) {
            FeeTransaction source = feeTransactionRepository
                    .findByIdAndSchool_Id(request.getSourceTransactionId(), request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Original transaction not found"));
            if (!source.getStudent().getId().equals(student.getId())) {
                throw new IllegalArgumentException("Transaction does not belong to this student");
            }
            fee = source.getFee();
            feeType = source.getFeeType();
        } else if (request.getFeeId() != null) {
            fee = feeRepository.findById(request.getFeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fee not found"));
            feeType = fee.getFeeType();
        }

        if (fee != null) {
            // Re-load under a write lock so concurrent refunds/collections can't lose updates.
            fee = feeRepository.lockById(fee.getId()).orElse(fee);
            if (refund.compareTo(nz(fee.getPaidAmount())) > 0) {
                throw new IllegalArgumentException("Refund exceeds the paid amount for this fee");
            }
            BigDecimal newPaid = nz(fee.getPaidAmount()).subtract(refund).max(BigDecimal.ZERO);
            fee.setPaidAmount(newPaid);
            fee.setStatus(newPaid.signum() == 0
                    ? Fee.FeeStatus.REFUNDED
                    : computeStatus(fee.getAmountAsDecimal(), newPaid, fee.getDueDate()));
            feeRepository.save(fee);
        }
        if (feeType == null) {
            feeType = "Refund";
        }

        FeeTransaction txn = FeeTransaction.builder()
                .school(school)
                .student(student)
                .fee(fee)
                .feeType(feeType)
                .type(FeeTransactionType.REFUND)
                .amount(refund)
                .mode(mode)
                .transactionDate(LocalDate.now())
                .refundReason(request.getRefundReason())
                .notes(blankToNull(request.getNotes()))
                .receiptNumber(generateReceiptNumber())
                .collectedBy(request.getPerformedBy())
                .idempotencyKey(idemKey)
                .build();
        txn = feeTransactionRepository.save(txn);

        activityLogger.log(school.getId(), AdminActivityType.FEE_REFUNDED,
                "Fee refunded: " + student.getFirstName() + " " + student.getLastName(),
                feeType + " • " + inr(refund) + " • " + request.getRefundReason(),
                "STUDENT", student.getId(), request.getPerformedBy());

        return new AdminFeeDtos.RefundResponse(true, "Refund processed successfully", txn.getReceiptNumber());
    }

    // ─── Student detail + receipt ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminFeeDtos.StudentFeeDetailResponse getStudentDetail(Long schoolId, Long studentId) {
        Student student = requireStudent(schoolId, studentId);
        StudentProfile profile = studentProfileRepository.findByStudent_Id(studentId).orElse(null);
        LocalDate today = LocalDate.now();

        List<Fee> fees = feeRepository.findAllByStudentId(studentId);
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal overdue = BigDecimal.ZERO;
        List<AdminFeeDtos.FeeBreakdownRow> breakdown = new ArrayList<>();
        for (Fee f : fees) {
            BigDecimal amt = f.getAmountAsDecimal();
            BigDecimal fPaid = nz(f.getPaidAmount());
            BigDecimal fPending = amt.subtract(fPaid).max(BigDecimal.ZERO);
            total = total.add(amt);
            paid = paid.add(fPaid);
            boolean isOverdue = f.getStatus() != Fee.FeeStatus.PAID
                    && f.getStatus() != Fee.FeeStatus.REFUNDED
                    && f.getDueDate() != null && f.getDueDate().isBefore(today)
                    && fPending.signum() > 0;
            if (isOverdue) {
                overdue = overdue.add(fPending);
            }
            String st = isOverdue ? Fee.FeeStatus.OVERDUE.name() : f.getStatus().name();
            breakdown.add(new AdminFeeDtos.FeeBreakdownRow(
                    f.getId(), f.getFeeType(), amt, fPaid, fPending, f.getDueDate(),
                    st, Fee.FeeStatus.valueOf(st).getLabel()));
        }
        BigDecimal pending = total.subtract(paid).max(BigDecimal.ZERO);

        List<AdminFeeDtos.PaymentHistoryRow> history = feeTransactionRepository.findByStudent(studentId).stream()
                .map(t -> new AdminFeeDtos.PaymentHistoryRow(
                        t.getId(), t.getTransactionDate(), t.getFeeType(), t.getAmount(),
                        t.getMode().name(), t.getMode().getLabel(), t.getReferenceNumber(),
                        t.getCollectedBy(), t.getReceiptNumber(), t.getType().name()))
                .toList();

        return new AdminFeeDtos.StudentFeeDetailResponse(
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                classLabel(student.getClassName(), student.getSection()),
                profile != null ? profile.getAdmissionNumber() : null,
                total, paid, pending, overdue, breakdown, history);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFeeDtos.ReceiptResponse getReceipt(Long schoolId, Long transactionId) {
        FeeTransaction txn = feeTransactionRepository.findByIdAndSchool_Id(transactionId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return buildReceipt(txn, txn.getStudent());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getReceiptPdf(Long schoolId, Long transactionId) {
        return AdminFeeReceiptPdfWriter.write(getReceipt(schoolId, transactionId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminFeeDtos.ActionResponse remind(Long schoolId, Long studentId, String performedBy) {
        Student student = requireStudent(schoolId, studentId);
        BigDecimal pending = nz(feeRepository.calculateTotalPendingAmount(studentId));
        String name = student.getFirstName() + " " + student.getLastName();
        String body = "Dear " + student.getParentName() + ", this is a reminder that "
                + inr(pending) + " in fees is pending for " + name
                + " (" + classLabel(student.getClassName(), student.getSection())
                + "). Please clear the dues at your earliest convenience.";

        // Durable outbox row, committed atomically with the audit log; delivered async.
        notificationOutboxRepository.save(NotificationOutbox.builder()
                .schoolId(schoolId)
                .channel(NotificationChannel.EMAIL)
                .recipient(student.getEmail())
                .template("FEE_REMINDER")
                .subject("Fee payment reminder")
                .body(body)
                .status(NotificationStatus.PENDING)
                .attempts(0)
                .relatedStudentId(studentId)
                .build());

        activityLogger.log(schoolId, AdminActivityType.FEE_REMINDER_SENT,
                "Fee reminder queued: " + name, inr(pending) + " pending",
                "STUDENT", studentId, performedBy);
        return new AdminFeeDtos.ActionResponse(true, "Reminder queued for " + student.getFirstName());
    }

    // ─── Export ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] exportReport(AdminFeeRequests.ExportFeeReportRequest request) {
        String reportType = (request.getReportTypes() == null || request.getReportTypes().isEmpty())
                ? "SUMMARY" : request.getReportTypes().get(0).toUpperCase();
        boolean transactional = switch (reportType) {
            case "FEES_PAID_REPORT", "FEES_PAID", "REFUND_REPORT", "REFUND",
                 "LATE_PAYMENT_REPORT", "PENALTY_COLLECTED_REPORT" -> true;
            default -> false;
        };
        String title = "Fee Report - " + reportType.replace('_', ' ');
        List<String> headers;
        List<Map<String, String>> rows = new ArrayList<>();

        if (transactional) {
            headers = List.of("Receipt No", "Date", "Student", "Class", "Fee Type", "Type",
                    "Amount", "Mode", "Reference", "Collected By");
            List<FeeTransaction> txns = feeTransactionRepository.findForExport(
                    request.getSchoolId(), request.getStartDate(), request.getEndDate());
            boolean refundReport = reportType.startsWith("REFUND");
            for (FeeTransaction t : txns) {
                if (refundReport && t.getType() != FeeTransactionType.REFUND) {
                    continue;
                }
                if (!refundReport && !request.isIncludeRefunds() && t.getType() == FeeTransactionType.REFUND) {
                    continue;
                }
                if (blankToNull(request.getClassName()) != null
                        && !request.getClassName().equalsIgnoreCase(t.getStudent().getClassName())) {
                    continue;
                }
                if (blankToNull(request.getFeeCategory()) != null
                        && !request.getFeeCategory().equalsIgnoreCase(t.getFeeType())) {
                    continue;
                }
                Map<String, String> row = new LinkedHashMap<>();
                row.put("Receipt No", t.getReceiptNumber());
                row.put("Date", t.getTransactionDate().format(DATE_FMT));
                row.put("Student", t.getStudent().getFirstName() + " " + t.getStudent().getLastName());
                row.put("Class", classLabel(t.getStudent().getClassName(), t.getStudent().getSection()));
                row.put("Fee Type", t.getFeeType());
                row.put("Type", t.getType().name());
                row.put("Amount", t.getAmount().toPlainString());
                row.put("Mode", t.getMode().getLabel());
                row.put("Reference", t.getReferenceNumber() != null ? t.getReferenceNumber() : "");
                row.put("Collected By", t.getCollectedBy() != null ? t.getCollectedBy() : "");
                rows.add(row);
            }
        } else {
            headers = List.of("Student", "Class", "Total Fees", "Paid", "Pending", "Overdue", "Status");
            String statusFilter = (request.getPaymentStatuses() != null && !request.getPaymentStatuses().isEmpty())
                    ? request.getPaymentStatuses().get(0).toUpperCase() : statusForReport(reportType);
            LocalDate today = LocalDate.now();
            List<StudentFeeAggregate> aggregates = feeRepository.aggregateStudentFees(
                    request.getSchoolId(), null, blankToNull(request.getClassName()),
                    normalizeStatus(statusFilter), today, PageRequest.of(0, 5000));
            for (StudentFeeAggregate a : aggregates) {
                BigDecimal totalFees = nz(a.getTotalFees());
                BigDecimal paid = nz(a.getPaidFees());
                BigDecimal overdue = nz(a.getOverdueFees());
                BigDecimal pending = totalFees.subtract(paid).max(BigDecimal.ZERO);
                String st = deriveStatus(totalFees, paid, overdue);
                Map<String, String> row = new LinkedHashMap<>();
                row.put("Student", a.getFirstName() + " " + a.getLastName());
                row.put("Class", classLabel(a.getClassName(), a.getSection()));
                row.put("Total Fees", totalFees.toPlainString());
                row.put("Paid", paid.toPlainString());
                row.put("Pending", pending.toPlainString());
                row.put("Overdue", overdue.toPlainString());
                row.put("Status", Fee.FeeStatus.valueOf(st).getLabel());
                rows.add(row);
            }
        }
        return AdminFeeExportWriter.write(request.getFormat(), title, headers, rows);
    }

    @Override
    public String exportContentType(String format) {
        return switch (format == null ? "EXCEL" : format.toUpperCase()) {
            case "CSV" -> "text/csv";
            case "PDF" -> "application/pdf";
            default -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        };
    }

    @Override
    public String exportFilename(String format) {
        String ext = switch (format == null ? "EXCEL" : format.toUpperCase()) {
            case "CSV" -> "csv";
            case "PDF" -> "pdf";
            default -> "xlsx";
        };
        return "fee_report_" + LocalDate.now() + "." + ext;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Fee resolveCollectibleFee(Student student, AdminFeeRequests.CollectFeeRequest request) {
        if (request.getFeeId() != null) {
            Fee fee = feeRepository.findById(request.getFeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fee not found"));
            if (!fee.getStudent().getId().equals(student.getId())) {
                throw new IllegalArgumentException("Fee does not belong to this student");
            }
            return fee;
        }
        List<Fee> collectible = feeRepository.lockCollectibleForStudent(student.getId(), request.getFeeType());
        if (!collectible.isEmpty()) {
            return collectible.get(0);
        }
        Fee fee = Fee.builder()
                .student(student)
                .feeType(request.getFeeType())
                .amount(request.getAmount())
                .paidAmount(BigDecimal.ZERO)
                .dueDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .status(Fee.FeeStatus.PENDING)
                .academicYear(currentAcademicYear())
                .description(request.getFeeType())
                .build();
        return feeRepository.save(fee);
    }

    private AdminFeeDtos.ReceiptResponse buildReceipt(FeeTransaction txn, Student student) {
        StudentProfile profile = studentProfileRepository.findByStudent_Id(student.getId()).orElse(null);
        String desc = txn.getFeeType()
                + (txn.getType() == FeeTransactionType.REFUND ? " (Refund)" : "");
        return new AdminFeeDtos.ReceiptResponse(
                txn.getReceiptNumber(),
                student.getSchool() != null ? student.getSchool().getSchoolName() : null,
                student.getFirstName() + " " + student.getLastName(),
                classLabel(student.getClassName(), student.getSection()),
                profile != null ? profile.getAdmissionNumber() : null,
                txn.getMode().getLabel(),
                txn.getTransactionDate(),
                txn.getReferenceNumber(),
                List.of(new AdminFeeDtos.ReceiptItem(desc, txn.getAmount())),
                txn.getAmount(),
                inr(txn.getAmount()));
    }

    private AdminFeeDtos.FeeStructureRow toStructureRow(FeeStructure s) {
        return new AdminFeeDtos.FeeStructureRow(
                s.getId(),
                s.getFeeName() != null ? s.getFeeName() : s.getFeeType(),
                s.getFeeType(),
                s.getTargetClassName(),
                s.getAmount(),
                inr(s.getAmount()),
                s.getFrequency().name(),
                s.getFrequency().getLabel(),
                dueDateSchedule(s),
                s.getAcademicYear(),
                s.isActive());
    }

    private String dueDateSchedule(FeeStructure s) {
        if (s.getFrequency() == FeeFrequency.MONTHLY && s.getDueDayOfMonth() != null) {
            return "Every " + ordinal(s.getDueDayOfMonth());
        }
        if (s.getDueDate() != null) {
            return s.getDueDate().toString();
        }
        return "—";
    }

    private Student requireStudent(Long schoolId, Long studentId) {
        return studentRepository.findByIdAndSchool_Id(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private Map<Long, StudentProfile> profilesFor(List<Student> students) {
        Map<Long, StudentProfile> map = new LinkedHashMap<>();
        studentProfileRepository.findByStudent_IdIn(students.stream().map(Student::getId).toList())
                .forEach(p -> map.put(p.getStudent().getId(), p));
        return map;
    }

    private Fee.FeeStatus computeStatus(BigDecimal amount, BigDecimal paid, LocalDate dueDate) {
        amount = nz(amount);
        paid = nz(paid);
        if (amount.signum() > 0 && paid.compareTo(amount) >= 0) {
            return Fee.FeeStatus.PAID;
        }
        if (paid.signum() > 0) {
            return Fee.FeeStatus.PARTIAL;
        }
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            return Fee.FeeStatus.OVERDUE;
        }
        return Fee.FeeStatus.PENDING;
    }

    private static String deriveStatus(BigDecimal total, BigDecimal paid, BigDecimal overdue) {
        if (overdue.signum() > 0) {
            return Fee.FeeStatus.OVERDUE.name();
        }
        if (total.signum() > 0 && paid.compareTo(total) >= 0) {
            return Fee.FeeStatus.PAID.name();
        }
        if (paid.signum() > 0) {
            return Fee.FeeStatus.PARTIAL.name();
        }
        return Fee.FeeStatus.PENDING.name();
    }

    private String statusForReport(String reportType) {
        return switch (reportType) {
            case "PENDING_FEES_REPORT", "PENDING" -> "PENDING";
            case "OVERDUE_REPORT", "OVERDUE" -> "OVERDUE";
            default -> "ALL";
        };
    }

    /**
     * Generates a human-friendly receipt id of the form {@code INV-{year}-{seq}} using an
     * atomic per-year counter (INSERT ... ON CONFLICT ... RETURNING), so concurrent
     * collections can never receive the same number.
     */
    private String generateReceiptNumber() {
        int year = LocalDate.now().getYear();
        long seq = receiptCounterRepository.nextValue(year);
        return "INV-" + year + "-" + String.format("%04d", seq);
    }

    private static FeeFrequency parseFrequency(String value) {
        try {
            return FeeFrequency.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid frequency: " + value);
        }
    }

    private static FeePaymentMode parseMode(String value) {
        try {
            return FeePaymentMode.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid payment method: " + value);
        }
    }

    private static String normalizeStatus(String status) {
        String s = blankToNull(status);
        if (s == null) {
            return null;
        }
        s = s.toUpperCase();
        return s.equals("ALL") ? null : s;
    }

    private static String currentAcademicYear() {
        LocalDate now = LocalDate.now();
        int startYear = now.getMonthValue() >= 4 ? now.getYear() : now.getYear() - 1;
        return startYear + "-" + (startYear + 1);
    }

    private static String classLabel(String className, String section) {
        if (section == null || section.isBlank() || section.equalsIgnoreCase("NA")) {
            return className;
        }
        return className + " (" + section + ")";
    }

    private static String ordinal(int n) {
        if (n >= 11 && n <= 13) {
            return n + "th";
        }
        return switch (n % 10) {
            case 1 -> n + "st";
            case 2 -> n + "nd";
            case 3 -> n + "rd";
            default -> n + "th";
        };
    }

    private static String inr(BigDecimal value) {
        BigDecimal v = nz(value).setScale(0, RoundingMode.HALF_UP);
        NumberFormat nf = NumberFormat.getInstance(INDIA);
        nf.setMaximumFractionDigits(0);
        return "₹" + nf.format(v);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private String showingLabel(long total, int page, int size, int onPage) {
        if (total == 0) {
            return "Showing 0 of 0 students";
        }
        long from = (long) page * size + 1;
        long to = from + onPage - 1;
        return "Showing " + from + "-" + to + " of " + total + " students";
    }
}
