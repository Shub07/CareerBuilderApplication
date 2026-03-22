package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherMessageRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.models.StudyMaterial;
import com.org.careerbuilder.repository.StudyMaterialRepository;
import com.org.careerbuilder.service.StudentClassesService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/student/classes")
@RequiredArgsConstructor
public class StudentClassesController {

    private final StudentClassesService studentClassesService;
    private final StudyMaterialRepository studyMaterialRepository;

    /**
     * Get overview section for Student Classes Dashboard
     */
    @GetMapping("/overview")
    public StudentClassesOverviewResponse getOverview(
            @RequestParam Long studentId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return studentClassesService.getOverview(studentId, date);
    }

    @GetMapping("/materials/{materialId}/download")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long materialId) throws IOException {

        StudyMaterial material = studyMaterialRepository
                .findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        Path path = Paths.get(material.getFilePath());

        UrlResource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + path.getFileName())
                .body((Resource) resource);
    }

    /**
     * Get today's classes
     */
    @GetMapping("/today")
    public List<StudentClassCardResponse> getTodayClasses(
            @RequestParam Long studentId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return studentClassesService.getTodayClasses(studentId, date);
    }

    /**
     * Get all subjects of a student
     */
    @GetMapping("/subjects")
    public List<StudentClassCardResponse> getSubjects(
            @RequestParam Long studentId
    ) {
        return studentClassesService.getSubjects(studentId);
    }

    /**
     * Get calendar slots
     */
    @GetMapping("/calendar")
    public List<StudentCalendarSlotResponse> getCalendar(
            @RequestParam Long studentId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return studentClassesService.getCalendar(studentId, date);
    }

    /**
     * Get study materials for a subject
     */
    @GetMapping("/materials")
    public List<StudyMaterialCardResponse> getStudyMaterials(
            @RequestParam Long studentId,
            @RequestParam(required = false) Long subjectId
    ) {
        return studentClassesService.getStudyMaterials(studentId, subjectId);
    }

    /**
     * Get teachers of a subject
     */
    @GetMapping("/teachers")
    public List<TeacherCardResponse> getTeachers(
            @RequestParam Long studentId,
            @RequestParam(required = false) Long subjectId
    ) {
        return studentClassesService.getTeachers(studentId, subjectId);
    }

    /**
     * Send message to teacher
     */
    @PostMapping("/teachers/{teacherId}/message")
    public void sendTeacherMessage(
            @RequestParam Long studentId,
            @PathVariable Long teacherId,
            @RequestBody TeacherMessageRequest request
    ) {
        studentClassesService.sendTeacherMessage(studentId, teacherId, request);
    }
}