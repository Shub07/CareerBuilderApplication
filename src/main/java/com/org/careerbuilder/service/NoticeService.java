package com.org.careerbuilder.service;

import com.org.careerbuilder.models.Notice;

import java.util.List;

public interface NoticeService {
    List<Notice> getRecentNotices(Long schoolId);
    int getTotalNoticeCount(Long schoolId);
    int getUnreadNoticeCount(Long studentId, List<Notice> recent);
}
