package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.FeeTransaction;
import com.org.careerbuilder.models.enums.FeeTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeeTransactionRepository extends JpaRepository<FeeTransaction, Long> {

    @Query("""
            SELECT t FROM FeeTransaction t
            WHERE t.student.id = :studentId
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    List<FeeTransaction> findByStudent(@Param("studentId") Long studentId);

    @Query("""
            SELECT t FROM FeeTransaction t
            WHERE t.student.id = :studentId AND t.type = :type
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    List<FeeTransaction> findByStudentAndType(@Param("studentId") Long studentId,
                                              @Param("type") FeeTransactionType type);

    Optional<FeeTransaction> findByIdAndSchool_Id(Long id, Long schoolId);

    Optional<FeeTransaction> findBySchool_IdAndIdempotencyKey(Long schoolId, String idempotencyKey);

    Optional<FeeTransaction> findByReceiptNumber(String receiptNumber);

    boolean existsByReceiptNumber(String receiptNumber);

    long countByReceiptNumberStartingWith(String prefix);

    /** Net collection (collections minus refunds) within a date window. */
    @Query("""
            SELECT COALESCE(SUM(CASE WHEN t.type = com.org.careerbuilder.models.enums.FeeTransactionType.COLLECTION
                                     THEN t.amount ELSE -t.amount END), 0)
            FROM FeeTransaction t
            WHERE t.school.id = :schoolId
              AND t.transactionDate >= :from AND t.transactionDate <= :to
            """)
    BigDecimal sumNetCollectedBetween(@Param("schoolId") Long schoolId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM FeeTransaction t
            WHERE t.school.id = :schoolId AND t.type = :type
            """)
    BigDecimal sumBySchoolAndType(@Param("schoolId") Long schoolId,
                                  @Param("type") FeeTransactionType type);

    @Query("""
            SELECT t FROM FeeTransaction t
            WHERE t.school.id = :schoolId
              AND (:from IS NULL OR t.transactionDate >= :from)
              AND (:to IS NULL OR t.transactionDate <= :to)
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    List<FeeTransaction> findForExport(@Param("schoolId") Long schoolId,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);
}
