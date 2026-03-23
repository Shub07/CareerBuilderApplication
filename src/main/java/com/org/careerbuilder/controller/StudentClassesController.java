package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.TeacherMessageRequest;
import com.org.careerbuilder.dto.response.*;
import com.org.careerbuilder.models.StudyMaterial;
import com.org.careerbuilder.repository.StudyMaterialRepository;
import com.org.careerbuilder.service.StudentClassesService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
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
     * 📊 Get overview section for Student Classes Dashboard
     * Returns summary metrics for today's classes, attendance, materials, etc.
     * 
     * Usage:
     * GET /api/student/classes/overview?studentId=1&date=2026-03-22
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

    /**
     * 📥 Download Study Material
     * Allows students to download uploaded study materials (PDFs, documents, etc.)
     * 
     * Usage:
     * GET /api/student/classes/materials/5/download
     */
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
                .body(resource);
    }

    /**
     * 📅 Get Today's Classes
     * Fetches all class sessions scheduled for the student on a specific date
     * Returns class name, subject, teacher, time slot, and status
     * 
     * Usage:
     * GET /api/student/classes/today?studentId=1&date=2026-03-22
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
     * 📚 Get All Subjects
     * Fetches all subjects that the student is enrolled in across different classes
     * 
     * Usage:
     * GET /api/student/classes/subjects?studentId=1
     */
    @GetMapping("/subjects")
    public List<StudentClassCardResponse> getSubjects(
            @RequestParam Long studentId
    ) {
        return studentClassesService.getSubjects(studentId);
    }

    /**
     * 📆 Get Calendar Slots
     * Fetches class schedule slots for a month/week view
     * Used to display the calendar grid on the Classes dashboard
     * 
     * Usage:
     * GET /api/student/classes/calendar?studentId=1&date=2026-03-22
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
     * 📄 Get Study Materials
     * Fetches educational resources (PDFs, notes, videos, etc.) uploaded by teachers
     * Can be filtered by subject
     * 
     * Usage:
     * GET /api/student/classes/materials?studentId=1&subjectId=5
     * GET /api/student/classes/materials?studentId=1 (all materials)
     */
    @GetMapping("/materials")
    public List<StudyMaterialCardResponse> getStudyMaterials(
            @RequestParam Long studentId,
            @RequestParam(required = false) Long subjectId
    ) {
        return studentClassesService.getStudyMaterials(studentId, subjectId);
    }

    /**
     * 👨‍🏫 Get Teachers List - MY CLASSES TAB
     * Fetches all teachers assigned to the student's classes with details:
     * - Teacher name, subject, experience, qualification
     * - Classes taught to this student
     * - Availability status (Available to query, Busy, etc.)
     * - Profile photo URL
     * 
     * This is the main endpoint for the "Teachers" section in the UI
     * 
     * Usage:
     * GET /api/student/classes/teachers?studentId=1
     * GET /api/student/classes/teachers?studentId=1&subjectId=5 (filter by subject)
     * 
     * Response:
     * [
     *   {
     *     "teacherId": 1,
     *     "teacherName": "Mrs. Sarja Kumar",
     *     "subjectName": "Mathematics",
     *     "experienceYears": 8,
     *     "qualification": "M.Sc Mathematics, B.Ed",
     *     "availabilityTag": "Available to query",
     *     "classesText": "Class 10 A, Class 10 B"
     *   },
     *   ...
     * ]
     */
    @GetMapping("/teachers")
    public List<TeacherCardResponse> getTeachers(
            @RequestParam Long studentId,
            @RequestParam(required = false) Long subjectId
    ) {
        return studentClassesService.getTeachers(studentId, subjectId);
    }

    /**
     * 👤 Get Teacher Profile
     * Fetches detailed profile information for a specific teacher
     * Shown when student clicks "View Profile" on a teacher card
     * 
     * Returns:
     * - Teacher name, subject, profile photo
     * - About/Bio section
     * - Qualification and experience
     * - Classes taught
     * - Office hours
     * - Contact information
     * - Availability status
     * 
     * Usage:
     * GET /api/student/classes/teachers/1/profile
     * 
     * Response:
     * {
     *   "teacherId": 1,
     *   "teacherName": "Mrs. Sarja Kumar",
     *   "subjectName": "Mathematics",
     *   "profilePhotoUrl": "https://...",
     *   "qualification": "M.Sc Mathematics, B.Ed",
     *   "experienceYears": 8,
     *   "about": "Dedicated mathematics educator with a passion for making complex calculus concepts accessible to all students. Mrs. Kumar has been with the Mathematics Olympiad team...",
     *   "email": "sarja@school.edu",
     *   "phone": "+91-9876543210",
     *   "classesTaught": ["Class 10 A", "Class 10 B"],
     *   "officeHours": {
     *     "day": "Mon - Fri",
     *     "startTime": "3:30 PM",
     *     "endTime": "4:30 PM"
     *   },
     *   "availabilityStatus": "Available to query",
     *   "messageEnabled": true
     * }
     */
    @GetMapping("/teachers/{teacherId}/profile")
    public TeacherProfileResponse getTeacherProfile(
            @PathVariable Long teacherId
    ) {
        // This endpoint will be implemented in StudentClassesService
        return studentClassesService.getTeacherProfile(teacherId);
    }

    /**
     * 💬 Send Message to Teacher
     * Allows student to send a message/query to a teacher
     * Message includes subject and body text
     * Teacher receives notification and can reply
     * 
     * Usage:
     * POST /api/student/classes/teachers/1/message
     * 
     * Request Body:
     * {
     *   "subject": "Question about Calculus assignment",
     *   "message": "Hi Ms. Aruna, I have a question about the integration problem in..."
     * }
     * 
     * Response:
     * {
     *   "messageId": 123,
     *   "subject": "Question about Calculus assignment",
     *   "message": "Hi Ms. Aruna, I have a question about the integration problem in...",
     *   "sentAt": "2026-03-22T15:45:30",
     *   "status": "SENT"
     * }
     */
    @PostMapping("/teachers/{teacherId}/message")
    public TeacherMessageResponse sendTeacherMessage(
            @RequestParam Long studentId,
            @PathVariable Long teacherId,
            @RequestBody TeacherMessageRequest request
    ) {
        return studentClassesService.sendTeacherMessage(studentId, teacherId, request);
    }
}