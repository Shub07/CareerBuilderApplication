package com.org.careerbuilder.dto.response;

import java.util.List;

public record DashboardResponse(
        Header header,
        DashboardCards cards,
        RecentExamScore recentExamScore,
        TodaySchedule todaySchedule,
        NoticePanel noticePanel
) {

    public record Header(String greetingText, String dateText) {}

    public record DashboardCards(
            int attendancePercent,
            String attendanceDeltaText,
            int upcomingExamsCount,
            String nextExamText,
            int upcomingLeaveCount,
            String nextLeaveText,
            int recentNoticesCount,
            int recentNoticesUnreadCount,
            int todaysClassesCount,
            int todaysClassesCompletedCount,
            int assignmentsCount,
            String assignmentsDueText
    ) {}

    public record RecentExamScore(
            String totalMarksText,
            List<ExamSubjectScore> subjects
    ) {}

    public record ExamSubjectScore(
            String subjectName,
            String gradeText,
            String deltaText,
            String scoreText,
            String examDateText,
            int percent
    ) {}

    public record TodaySchedule(
            String title,
            List<ScheduleItem> items
    ) {}

    public record ScheduleItem(
            int slotNo,
            String subjectName,
            String teacherName,
            String timeText
    ) {}

    public record NoticePanel(
            String title,
            List<NoticeItem> items
    ) {}

    public record NoticeItem(
            String title,
            String dateText,
            boolean unread
    ) {}
}
