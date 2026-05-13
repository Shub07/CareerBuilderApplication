package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherClassAnnouncementRequest;
import com.org.careerbuilder.dto.request.TeacherStudentAlertRequest;
import com.org.careerbuilder.dto.response.TeacherNoticeDtos;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeacherNoticeService {
    TeacherNoticeDtos.TeacherNoticeFiltersResponse getFilters(Long facultyId, String studentQuery);

    List<TeacherNoticeDtos.TeacherNoticeResponse> listNotices(Long facultyId, String tab, String query);

    TeacherNoticeDtos.TeacherNoticeResponse getNotice(Long facultyId, Long teacherNoticeId);

    TeacherNoticeDtos.AttachmentUploadResponse uploadAttachment(Long facultyId, MultipartFile file);

    TeacherNoticeDtos.TeacherNoticeResponse createClassAnnouncement(Long facultyId, TeacherClassAnnouncementRequest request);

    TeacherNoticeDtos.TeacherNoticeResponse createStudentAlert(Long facultyId, TeacherStudentAlertRequest request);

    TeacherNoticeDtos.TeacherNoticeResponse updateClassAnnouncement(Long facultyId, Long teacherNoticeId, TeacherClassAnnouncementRequest request);

    TeacherNoticeDtos.TeacherNoticeResponse updateStudentAlert(Long facultyId, Long teacherNoticeId, TeacherStudentAlertRequest request);

    TeacherNoticeDtos.TeacherNoticeResponse publish(Long facultyId, Long teacherNoticeId);

    void deleteNotice(Long facultyId, Long teacherNoticeId);

    Resource downloadAttachment(Long facultyId, Long teacherNoticeId);
}
