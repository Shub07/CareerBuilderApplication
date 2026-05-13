package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.TeacherMyClassesDtos;
import com.org.careerbuilder.service.TeacherMyClassesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/search")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class TeacherPortalSearchController {

    private final TeacherMyClassesService teacherMyClassesService;

    @GetMapping("/{facultyId}")
    public ResponseEntity<TeacherMyClassesDtos.TeacherPortalSearchResponse> search(
            @PathVariable Long facultyId,
            @RequestParam String q) {
        return ResponseEntity.ok(teacherMyClassesService.search(facultyId, q));
    }
}
