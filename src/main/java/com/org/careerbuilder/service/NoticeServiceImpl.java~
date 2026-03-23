package com.org.careerbuilder.service;

import com.org.careerbuilder.models.Notice;
import com.org.careerbuilder.repository.NoticeRepository;
import com.org.careerbuilder.repository.StudentNoticeReadRepository;
import com.org.careerbuilder.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final StudentNoticeReadRepository studentNoticeReadRepository;

    @Override
    public List<Notice> getRecentNotices(String schoolId) {
        return noticeRepository.findTop10BySchoolIdOrderByCreatedAtDesc(schoolId);
    }

    @Override
    public int getTotalNoticeCount(String schoolId) {
        return (int) noticeRepository.countBySchoolId(schoolId);
    }

    @Override
    public int getUnreadNoticeCount(Long studentId, List<Notice> recent) {
        // unread = recent - read
        int unread = 0;
        for (Notice n : recent) {
            boolean read = studentNoticeReadRepository.existsByStudent_IdAndNotice_Id(studentId, n.getId());
            if (!read) unread++;
        }
        return unread;
    }
}
