package com.org.careerbuilder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 👨‍👩‍👧 Parent Child Dashboard DTO - Child's Info for Parent (READ-ONLY)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentChildDashboardDTO {
    
    // Parent Info
    private String parentId;
    private String parentEmail;
    private String parentName;
    
    // Child Info (READ-ONLY)
    private Long childId;
    private String childName;
    private String childEmail;
    private String childClass;
    private String section;
    private Integer rollNo;
    private String schoolName;
    private Integer age;
    
    // Quick Metrics
    private Double overallAttendance;
    private Integer pendingFeesCount;
    private String pendingFeesAmount;
    private Integer upcomingAssignments;
}

