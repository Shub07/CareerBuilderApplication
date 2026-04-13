package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.CareerSaveProgressRequest;
import com.org.careerbuilder.dto.request.CareerSubmitRequest;
import com.org.careerbuilder.dto.response.*;

import java.util.List;

public interface CareerBuilderService {

    /** Summary stats + in-progress/completed lists */
    CareerBuilderSummaryResponse getSummary(Long studentId, Long schoolId);

    /** All assessments for school, annotated with student status */
    List<CareerAssessmentResponse> getAssessments(Long studentId, Long schoolId);

    /** Questions for a specific assessment + student's saved answers */
    CareerQuestionsResponse getQuestions(Long assessmentId, Long studentId);

    /** Start a fresh attempt or return existing progress id */
    CareerStudentProgressIdResponse startOrResume(Long assessmentId, Long studentId);

    /** Save one answer and update currentQuestionIndex */
    void saveProgress(Long assessmentId, Long studentId, CareerSaveProgressRequest request);

    /** Complete the assessment – compute + persist result */
    CareerResultResponse submit(Long assessmentId, Long studentId, CareerSubmitRequest request);

    /** Get the computed result for a completed assessment */
    CareerResultResponse getResult(Long assessmentId, Long studentId);

    /** All reports available for download */
    List<CareerReportResponse> getReports(Long studentId);

    /** Download a generated report by result id */
    CareerReportDownloadPayload downloadReport(Long resultId, Long studentId);
}
