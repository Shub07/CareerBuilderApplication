package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherAttendanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TeacherAttendanceEntryRepository extends JpaRepository<TeacherAttendanceEntry, Long> {
    Optional<TeacherAttendanceEntry> findByFaculty_IdAndWorkDate(Long facultyId, LocalDate workDate);

    List<TeacherAttendanceEntry> findByFaculty_IdAndWorkDateBetweenOrderByWorkDateAsc(Long facultyId, LocalDate from, LocalDate to);
}
