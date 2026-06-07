package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.models.Faculty;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.repository.FacultyRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.service.AdminSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminSearchServiceImpl implements AdminSearchService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminOperationResponses.GlobalSearchResponse search(Long schoolId, String query, int limit) {
        if (query == null || query.trim().length() < 2) {
            return new AdminOperationResponses.GlobalSearchResponse(List.of());
        }
        String q = query.trim();
        int max = Math.min(Math.max(limit, 1), 30);
        List<AdminOperationResponses.SearchHit> hits = new ArrayList<>();

        for (Student s : studentRepository.searchBySchoolText(schoolId, q)) {
            if (hits.size() >= max) {
                break;
            }
            hits.add(new AdminOperationResponses.SearchHit(
                    "STUDENT",
                    s.getId(),
                    s.getFirstName() + " " + s.getLastName(),
                    "Class " + s.getClassName() + " " + s.getSection() + " · Roll " + s.getRollNo(),
                    "/students/" + s.getId()
            ));
        }

        if (hits.size() < max) {
            for (Faculty f : facultyRepository.findAll()) {
                if (!f.getSchool().getId().equals(schoolId)) {
                    continue;
                }
                String hay = (f.getFirstName() + " " + f.getLastName() + " " + f.getEmail() + " " + f.getFacultyId())
                        .toLowerCase(Locale.ROOT);
                if (!hay.contains(q.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                hits.add(new AdminOperationResponses.SearchHit(
                        "TEACHER",
                        f.getId(),
                        f.getFirstName() + " " + f.getLastName(),
                        f.getSubject() != null ? f.getSubject().getName() : "Faculty",
                        "/teachers/" + f.getId()
                ));
                if (hits.size() >= max) {
                    break;
                }
            }
        }

        for (Object[] row : studentRepository.findDistinctClassSections(schoolId)) {
            if (hits.size() >= max) {
                break;
            }
            String cn = (String) row[0];
            String sec = (String) row[1];
            String label = "Class " + cn + " " + sec;
            if (label.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))
                    || cn.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))) {
                hits.add(new AdminOperationResponses.SearchHit(
                        "CLASS",
                        null,
                        label,
                        "Class section",
                        "/classes?class=" + cn + "&section=" + sec
                ));
            }
        }

        return new AdminOperationResponses.GlobalSearchResponse(hits);
    }
}
