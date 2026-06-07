package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminStudentRequests;
import com.org.careerbuilder.dto.response.AdminStudentCertificateDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Certificate;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.repository.CertificateRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.service.AdminStudentCertificateService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import com.org.careerbuilder.service.support.AdminFileStorageHelper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class AdminStudentCertificateServiceImpl implements AdminStudentCertificateService {

    private static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "jpg", "jpeg", "png");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;
    private final AdminFileStorageHelper fileStorageHelper;
    private final AdminActivityLogger activityLogger;

    private final Map<String, BulkCertSession> bulkPreviewCache = new ConcurrentHashMap<>();

    @Override
    @Transactional(readOnly = true)
    public AdminStudentCertificateDtos.CertificatesListResponse listCertificates(
            Long schoolId, Long studentId, String search, String category) {
        requireStudent(schoolId, studentId);
        List<Certificate> all = certificateRepository.findByStudentFiltered(studentId, null, search);
        List<Certificate> filtered = all;
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            Certificate.CertificateCategory cat = parseCategory(category);
            filtered = all.stream().filter(c -> c.getCategory() == cat).toList();
        }
        List<AdminStudentCertificateDtos.CertificateListRow> rows = filtered.stream()
                .map(this::toListRow)
                .toList();
        List<String> categories = Arrays.stream(Certificate.CertificateCategory.values())
                .map(Enum::name)
                .toList();
        return new AdminStudentCertificateDtos.CertificatesListResponse(rows, categories, rows.size());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudentCertificateDtos.CertificateDetailResponse getCertificateDetail(
            Long schoolId, Long studentId, Long certificateId) {
        Certificate c = requireCertificate(schoolId, studentId, certificateId);
        Student st = c.getStudent();
        return new AdminStudentCertificateDtos.CertificateDetailResponse(
                c.getId(),
                c.getName(),
                c.getCategory().name(),
                c.getIssuedOn() != null ? c.getIssuedOn().format(DATE_FMT) : "",
                formatFileSize(c.getFileSizeBytes()),
                c.getFileName() != null ? c.getFileName() : Paths.get(c.getFilePath()).getFileName().toString(),
                c.getFilePath(),
                st.getFirstName() + " " + st.getLastName(),
                c.getUploadedBy() != null ? c.getUploadedBy() : "Admin",
                c.getStatus().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadCertificate(Long schoolId, Long studentId, Long certificateId) {
        Certificate c = requireCertificate(schoolId, studentId, certificateId);
        try {
            return Files.readAllBytes(Paths.get(c.getFilePath()));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read certificate file", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentCertificateDtos.CertificateUploadResponse uploadCertificate(
            Long schoolId, Long studentId, MultipartFile file, AdminStudentRequests.UploadCertificateRequest meta) {
        Student st = requireStudent(schoolId, studentId);
        validateFile(file);
        String storedPath = fileStorageHelper.store(schoolId, "certificates", file);
        long size = file.getSize();
        Certificate cert = Certificate.builder()
                .student(st)
                .name(meta.getCertificateName().trim())
                .category(parseCategory(meta.getCategory()))
                .status(Certificate.CertificateStatus.AVAILABLE)
                .issuedOn(parseIssueDate(meta.getIssueDate()))
                .academicYear(meta.getAcademicYear())
                .filePath(storedPath)
                .fileName(file.getOriginalFilename())
                .fileSizeBytes(size)
                .uploadedBy(meta.getUploadedBy() != null ? meta.getUploadedBy() : "Admin")
                .build();
        cert = certificateRepository.save(cert);
        activityLogger.log(schoolId, AdminActivityType.CERTIFICATE_UPLOADED,
                "Certificate Uploaded",
                cert.getName() + " (" + cert.getCategory().name() + ")",
                "STUDENT", studentId, meta.getUploadedBy());
        return new AdminStudentCertificateDtos.CertificateUploadResponse(cert.getId(), "Certificate uploaded successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCertificate(Long schoolId, Long studentId, Long certificateId, String performedBy) {
        Certificate c = requireCertificate(schoolId, studentId, certificateId);
        certificateRepository.delete(c);
        activityLogger.log(schoolId, AdminActivityType.OTHER,
                "Certificate Deleted",
                c.getName(),
                "STUDENT", studentId, performedBy);
    }

    @Override
    public byte[] downloadBulkTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("certificates");
            Row header = sheet.createRow(0);
            String[] cols = {"Certificate Name", "Category", "Issue Date (YYYY-MM-DD)", "File Name"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Character Certificate");
            sample.createCell(1).setCellValue("ACADEMIC");
            sample.createCell(2).setCellValue(LocalDate.now().toString());
            sample.createCell(3).setCellValue("character_cert.pdf");
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate template", e);
        }
    }

    @Override
    public AdminStudentCertificateDtos.BulkCertificatePreviewResponse previewBulkUpload(
            Long schoolId, Long studentId, MultipartFile excelFile, MultipartFile filesArchive) {
        requireStudent(schoolId, studentId);
        if (excelFile == null || excelFile.isEmpty()) {
            throw new IllegalArgumentException("Excel file is required");
        }
        Map<String, byte[]> archiveFiles = extractArchive(filesArchive);
        List<BulkCertCandidate> candidates = new ArrayList<>();
        List<AdminStudentCertificateDtos.BulkCertificatePreviewRow> rows = new ArrayList<>();
        int valid = 0, errors = 0;

        Student st = studentRepository.findById(studentId).orElseThrow();
        String studentName = st.getFirstName() + " " + st.getLastName();

        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String certName = cell(row, 0);
                String category = cell(row, 1);
                String issueDate = cell(row, 2);
                String fileName = cell(row, 3);
                if (certName.isBlank() && fileName.isBlank()) {
                    continue;
                }
                String error = validateBulkRow(certName, category, fileName, archiveFiles);
                if (error != null) {
                    errors++;
                    rows.add(new AdminStudentCertificateDtos.BulkCertificatePreviewRow(
                            i + 1, studentName, certName, fileName, "ERROR", error));
                } else {
                    valid++;
                    byte[] fileBytes = archiveFiles.get(fileName.toLowerCase());
                    candidates.add(new BulkCertCandidate(certName, category, issueDate, fileName, fileBytes));
                    rows.add(new AdminStudentCertificateDtos.BulkCertificatePreviewRow(
                            i + 1, studentName, certName, fileName, "VALID", "Ready to register"));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Excel file", e);
        }

        String previewId = UUID.randomUUID().toString();
        bulkPreviewCache.put(previewId, new BulkCertSession(schoolId, studentId, candidates));
        return new AdminStudentCertificateDtos.BulkCertificatePreviewResponse(
                previewId, valid + errors, valid, errors, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStudentCertificateDtos.BulkCertificateCommitResponse commitBulkUpload(
            Long schoolId, Long studentId, String previewId, String performedBy) {
        BulkCertSession session = bulkPreviewCache.get(previewId);
        if (session == null || !session.studentId().equals(studentId) || !session.schoolId().equals(schoolId)) {
            throw new IllegalArgumentException("Preview session expired or not found");
        }
        Student st = requireStudent(schoolId, studentId);
        int registered = 0, skipped = 0;
        for (BulkCertCandidate c : session.candidates()) {
            try {
                String path = fileStorageHelper.store(schoolId, "certificates",
                        new ByteArrayMultipartFile(c.fileName(), c.fileBytes()));
                Certificate cert = Certificate.builder()
                        .student(st)
                        .name(c.certName())
                        .category(parseCategory(c.category()))
                        .status(Certificate.CertificateStatus.AVAILABLE)
                        .issuedOn(parseIssueDate(c.issueDate()))
                        .filePath(path)
                        .fileName(c.fileName())
                        .fileSizeBytes((long) c.fileBytes().length)
                        .uploadedBy(performedBy != null ? performedBy : "Admin")
                        .build();
                certificateRepository.save(cert);
                registered++;
            } catch (Exception e) {
                skipped++;
            }
        }
        bulkPreviewCache.remove(previewId);
        if (registered > 0) {
            activityLogger.log(schoolId, AdminActivityType.CERTIFICATE_UPLOADED,
                    "Bulk Certificates Uploaded",
                    registered + " certificate(s) added",
                    "STUDENT", studentId, performedBy);
        }
        return new AdminStudentCertificateDtos.BulkCertificateCommitResponse(
                previewId, registered, skipped,
                registered + " certificate(s) registered");
    }

    private AdminStudentCertificateDtos.CertificateListRow toListRow(Certificate c) {
        return new AdminStudentCertificateDtos.CertificateListRow(
                c.getId(),
                c.getName(),
                c.getCategory().name(),
                c.getIssuedOn() != null ? c.getIssuedOn().format(DATE_FMT) : "",
                formatFileSize(c.getFileSizeBytes()),
                c.getFileName(),
                c.getUploadedBy(),
                c.getStatus().name()
        );
    }

    private Certificate requireCertificate(Long schoolId, Long studentId, Long certificateId) {
        Certificate c = certificateRepository.findByIdAndStudentId(certificateId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
        if (!c.getStudent().getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Certificate not found");
        }
        return c;
    }

    private Student requireStudent(Long schoolId, Long studentId) {
        Student st = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!st.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Student not found in school");
        }
        return st;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }
        String ext = extension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Only PDF, JPG, and PNG files are allowed");
        }
    }

    private static String validateBulkRow(String certName, String category, String fileName, Map<String, byte[]> archive) {
        if (certName == null || certName.isBlank()) {
            return "Certificate name is required";
        }
        try {
            parseCategory(category);
        } catch (Exception e) {
            return "Invalid category";
        }
        if (fileName == null || fileName.isBlank()) {
            return "File name is required";
        }
        if (archive != null && !archive.isEmpty() && !archive.containsKey(fileName.toLowerCase())) {
            return "File not found in archive: " + fileName;
        }
        return null;
    }

    private static Certificate.CertificateCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return Certificate.CertificateCategory.ACADEMIC;
        }
        return Certificate.CertificateCategory.valueOf(category.trim().toUpperCase());
    }

    private static LocalDate parseIssueDate(String issueDate) {
        if (issueDate == null || issueDate.isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(issueDate.trim());
    }

    private static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "—";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    private static String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i + 1).toLowerCase() : "";
    }

    private static String cell(Row row, int idx) {
        if (row.getCell(idx) == null) {
            return "";
        }
        return switch (row.getCell(idx).getCellType()) {
            case STRING -> row.getCell(idx).getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) row.getCell(idx).getNumericCellValue());
            default -> "";
        };
    }

    private static Map<String, byte[]> extractArchive(MultipartFile archive) {
        if (archive == null || archive.isEmpty()) {
            return Map.of();
        }
        Map<String, byte[]> files = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(archive.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String name = Paths.get(entry.getName()).getFileName().toString().toLowerCase();
                    files.put(name, zis.readAllBytes());
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ZIP archive", e);
        }
        return files;
    }

    private record BulkCertSession(Long schoolId, Long studentId, List<BulkCertCandidate> candidates) {
    }

    private record BulkCertCandidate(String certName, String category, String issueDate, String fileName, byte[] fileBytes) {
    }

    /** Minimal MultipartFile wrapper for stored bytes. */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final String name;
        private final byte[] content;

        ByteArrayMultipartFile(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) {
            throw new UnsupportedOperationException();
        }
    }
}
