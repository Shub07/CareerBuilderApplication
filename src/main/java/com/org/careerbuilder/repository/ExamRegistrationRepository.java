package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ExamRegistration;
import com.org.careerbuilder.models.enums.ExamRegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ExamRegistrationRepository extends JpaRepository<ExamRegistration, Long> {

    Page<ExamRegistration> findByExam_IdAndSchoolIdOrderByStudent_RollNoAsc(
            Long examId, Long schoolId, Pageable pageable);

    long countByExam_IdAndSchoolId(Long examId, Long schoolId);

    @Query("SELECT r.student.id FROM ExamRegistration r WHERE r.exam.id = :examId")
    Set<Long> findStudentIdsByExamId(@Param("examId") Long examId);

    boolean existsByExam_IdAndStudent_Id(Long examId, Long studentId);

    List<ExamRegistration> findByExam_IdAndSchoolId(Long examId, Long schoolId);

    long countByExam_IdAndSchoolIdAndPrimaryVenue(Long examId, Long schoolId, String primaryVenue);

    long countByExam_IdAndSchoolIdAndStatus(Long examId, Long schoolId, ExamRegistrationStatus status);

    @Query("""
            SELECT COUNT(r) FROM ExamRegistration r
            WHERE r.exam.id = :examId AND r.schoolId = :schoolId
            AND r.primaryVenue = :venueName
            """)
    long countByVenueName(@Param("examId") Long examId,
                          @Param("schoolId") Long schoolId,
                          @Param("venueName") String venueName);

    void deleteByExam_IdAndStudent_IdIn(Long examId, Collection<Long> studentIds);

    Page<ExamRegistration> findByExam_IdAndSchoolIdAndHallTicketGeneratedTrueOrderByStudent_RollNoAsc(
            Long examId, Long schoolId, Pageable pageable);

    List<ExamRegistration> findByExam_IdAndSchoolIdAndHallTicketGeneratedFalseOrderByStudent_RollNoAsc(
            Long examId, Long schoolId);

    long countByExam_IdAndSchoolIdAndHallTicketGeneratedFalse(Long examId, Long schoolId);

    long countByExam_IdAndSchoolIdAndVenue_Id(Long examId, Long schoolId, Long venueId);

    List<ExamRegistration> findByExam_IdAndSchoolIdAndVenue_IdIsNullOrderByStudent_RollNoAsc(
            Long examId, Long schoolId);

    Optional<ExamRegistration> findByIdAndExam_IdAndSchoolId(Long id, Long examId, Long schoolId);
}
