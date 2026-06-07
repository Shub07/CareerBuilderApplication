package com.org.careerbuilder.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class AdminDashboardDtos {

    private AdminDashboardDtos() {
    }

    public record StatCard(
            String key,
            String label,
            String value,
            String subLabel,
            String trendText
    ) {
    }

    public record GreetingHeader(
            String greeting,
            String summaryLine,
            LocalDate today,
            String academicYear,
            int pendingApprovalsCount,
            int newNoticesCount
    ) {
    }

    public record QuickAction(
            String key,
            String label,
            String routeHint
    ) {
    }

    public record PendingApprovalRow(
            String id,
            String kind,
            String requesterName,
            String requestType,
            String detail,
            Instant requestedAt,
            boolean canApprove
    ) {
    }

    public record ActivityRow(
            Long id,
            String activityType,
            String title,
            String description,
            Instant occurredAt
    ) {
    }

    public record NoticeBoardItem(
            Long id,
            String title,
            String category,
            String categoryLabel,
            LocalDate eventDate,
            String description
    ) {
    }

    public record AdminDashboardResponse(
            GreetingHeader header,
            List<StatCard> stats,
            List<QuickAction> quickActions,
            List<PendingApprovalRow> pendingApprovals,
            List<ActivityRow> recentActivities,
            List<NoticeBoardItem> noticeBoard
    ) {
    }
}
