package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherQuizReviewSaveRequest;
import com.org.careerbuilder.dto.request.TeacherQuizUpsertRequest;
import com.org.careerbuilder.dto.response.TeacherQuizDtos;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TeacherQuizService {

    TeacherQuizDtos.QuizFiltersResponse getFilters(Long facultyId);

    TeacherQuizDtos.QuizListPage listQuizzes(
            Long facultyId,
            String view,
            String className,
            String section,
            Long subjectId,
            String lifecycleStatus,
            LocalDate dateFrom,
            LocalDate dateTo,
            String search,
            Pageable pageable);

    TeacherQuizDtos.QuizDetailResponse getQuizDetail(Long facultyId, Long quizId);

    Long createQuiz(Long facultyId, TeacherQuizUpsertRequest request);

    void updateQuiz(Long facultyId, Long quizId, TeacherQuizUpsertRequest request);

    void deleteQuiz(Long facultyId, Long quizId);

    TeacherQuizDtos.DuplicateQuizResponse duplicateQuiz(Long facultyId, Long quizId);

    void publishQuiz(Long facultyId, Long quizId);

    void markConducted(Long facultyId, Long quizId);

    TeacherQuizDtos.QuizResultsPage getResults(Long facultyId, Long quizId, String search, Pageable pageable);

    TeacherQuizDtos.QuizSubmissionReviewResponse getSubmissionReview(Long facultyId, Long quizId, Long submissionId);

    void saveSubmissionReview(Long facultyId, Long quizId, Long submissionId, TeacherQuizReviewSaveRequest request);

    Resource exportResultsCsv(Long facultyId, Long quizId);
}
