package com.org.careerbuilder.service.support;

import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.ClassSubjectTeacher;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.repository.ClassSubjectTeacherRepository;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LessonPlanSharedHelper {

    private static final DateTimeFormatter TIME_DISPLAY = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;

    public Faculty loadFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with id: " + facultyId));
    }

    public Student loadStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
    }

    public void assertTeaches(Long facultyId, Long schoolId, String className, String section, Long subjectId) {
        List<ClassSubjectTeacher> list = classSubjectTeacherRepository.findBySchoolIdAndClassNameAndSectionAndSubject_IdAndActiveTrue(
                schoolId, className, section, subjectId);
        boolean ok = list.stream().anyMatch(c -> c.getFaculty().getId().equals(facultyId));
        if (!ok) {
            throw new IllegalArgumentException("Teacher is not assigned to this class and subject");
        }
    }

    public boolean studentMatchesClass(Student st, Long schoolId, String className, String section) {
        return st.getSchool().getId().equals(schoolId)
                && st.getClassName().equalsIgnoreCase(className.trim())
                && st.getSection().equalsIgnoreCase(section.trim());
    }

    public String classLabel(String className, String section) {
        if (className == null || section == null) {
            return "—";
        }
        return "Grade " + className + " " + section;
    }

    public String formatSessionTime(LocalTime time) {
        if (time == null) {
            return "—";
        }
        return TIME_DISPLAY.format(time);
    }

    public String storeUploadedFile(Long facultyId, String subFolder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = Paths.get("uploads", subFolder, String.valueOf(facultyId));
            Files.createDirectories(dir);
            String orig = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String safe = orig.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = dir.resolve(UUID.randomUUID() + "_" + safe);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace("\\", "/");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    public void deleteStoredFile(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception ignored) {
            // best-effort cleanup
        }
    }
}
