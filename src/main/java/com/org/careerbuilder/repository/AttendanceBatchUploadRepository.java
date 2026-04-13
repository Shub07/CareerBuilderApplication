package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AttendanceBatchUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceBatchUploadRepository extends JpaRepository<AttendanceBatchUpload, Long> {

    Page<AttendanceBatchUpload> findBySchoolId(Long schoolId, Pageable pageable);

    Page<AttendanceBatchUpload> findByClassNameAndSection(String className, String section, Pageable pageable);

    List<AttendanceBatchUpload> findByUploadDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT a FROM AttendanceBatchUpload a WHERE a.uploadStatus = 'PENDING' ORDER BY a.createdAt ASC")
    Page<AttendanceBatchUpload> findPendingUploads(Pageable pageable);

    long countByUploadStatus(String status);
}

