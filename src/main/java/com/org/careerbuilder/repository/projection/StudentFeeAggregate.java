package com.org.careerbuilder.repository.projection;

import java.math.BigDecimal;

/**
 * Per-student fee roll-up used by the admin Student Fees list page.
 */
public interface StudentFeeAggregate {
    Long getStudentId();

    String getFirstName();

    String getLastName();

    String getClassName();

    String getSection();

    BigDecimal getTotalFees();

    BigDecimal getPaidFees();

    BigDecimal getOverdueFees();
}
