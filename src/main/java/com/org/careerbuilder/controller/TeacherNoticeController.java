package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherClassAnnouncementRequest;
import com.org.careerbuilder.dto.request.TeacherStudentAlertRequest;
import com.org.careerbuilder.dto.response.TeacherNoticeDtos;
import com.org.careerbuilder.service.TeacherNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/notices")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherNoticeController {

    private final TeacherNoticeService teacherNoticeService;

    @GetMapping("/{facultyId}/filters")
    public ResponseEntity<TeacherNoticeDtos.TeacherNoticeFiltersResponse> filters(
            @PathVariable Long facultyId,
            @RequestParam(required = false) String studentQuery) {
        return ResponseEntity.ok(teacherNoticeService.getFilters(facultyId, studentQuery));
    }

    @GetMapping("/{facultyId}")
    public ResponseEntity<List<TeacherNoticeDtos.TeacherNoticeResponse>> list(
            @PathVariable Long facultyId,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(teacherNoticeService.listNotices(facultyId, tab, query));
    }

    @GetMapping("/{facultyId}/{teacherNoticeId}")
    public ResponseEntity<TeacherNoticeDtos.TeacherNoticeResponse> get(
            @PathVariable Long facultyId,
            @PathVariable Long teacherNoticeId) {
        return ResponseEntity.ok(teacherNoticeService.getNotice(facultyId, teacherNoticeId));
    }

    @PostMapping("/{facultyId}/attachments")
    public ResponseEntity<TeacherNoticeDtos.AttachmentUploadResponse> upload(
            @PathVariable Long facultyId,
            @RequestParam MultipartFile file) {
        return ResponseEntity.ok(teacherNoticeService.uploadAttachment(facultyId, file));
    }

    @PostMapping("/{facultyId}/class-announcements")
    public ResponseEntity<TeacherNoticeDtos.TeacherNoticeResponse> createClassAnnouncement(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherClassAnnouncementRequest request) {
        return ResponseEntity.ok(teacherNoticeService.createClassAnnouncement(facultyId, request));
    }

    @PostMapping("/{facultyId}/student-alerts")
    public ResponseEntity<TeacherNoticeDtos.TeacherNoticeResponse> createStudentAlert(
            @PathVariable Long facultyId,
            @Valid @RequestBody TeacherStudentAlertRequest request) {
        return ResponseEntity.ok(teacherNoticeService.createStudentAlert(facultyId, request));
    }

    @PutMapping("/{facultyId}/{teacherNoticeId}/class-announcement")
    public ResponseEntity<TeacherNoticeDtos.TeacherNoticeResponse> updateClassAnnouncement(
            @PathVariable Long facultyId,
            @PathVariable Long teacherNoticeId,
            @Valid @RequestBody TeacherClassAnnouncementRequest request) {
        return ResponseEntity.ok(teacherNoticeService.updateClassAnnouncement(facultyId, teacherNoticeId, request));
    }

    @PutMapping("/{facultyId}/{teacherNoticeId}/student-alert")
    public ResponseEntity<TeacherNoticeDtos.TeacherNoticeResponse> updateStudentAlert(
            @PathVariable Long facultyId,
            @PathVariable Long teacherNoticeId,
            @Valid @RequestBody TeacherStudentAlertRequest request) {
        return ResponseEntity.ok(teacherNoticeService.updateStudentAlert(facultyId, teacherNoticeId, request));
    }

    @PatchMapping("/{facultyId}/{teacherNoticeId}/publish")
    public ResponseEntity<TeacherNoticeDtos.TeacherNoticeResponse> publish(
            @PathVariable Long facultyId,
            @PathVariable Long teacherNoticeId) {
        return ResponseEntity.ok(teacherNoticeService.publish(facultyId, teacherNoticeId));
    }

    @DeleteMapping("/{facultyId}/{teacherNoticeId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long facultyId,
            @PathVariable Long teacherNoticeId) {
        teacherNoticeService.deleteNotice(facultyId, teacherNoticeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{facultyId}/{teacherNoticeId}/attachment/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long facultyId,
            @PathVariable Long teacherNoticeId) {
        Resource resource = teacherNoticeService.downloadAttachment(facultyId, teacherNoticeId);
        String filename = resource.getFilename() != null ? resource.getFilename() : "notice_attachment";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
