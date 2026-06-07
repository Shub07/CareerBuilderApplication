package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminClassRequests;
import com.org.careerbuilder.dto.response.AdminClassDtos;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminClassService {

    // List page
    AdminClassDtos.ClassStats getStats(Long schoolId);

    AdminClassDtos.AcademicYearsResponse getAcademicYears(Long schoolId);

    AdminClassDtos.ClassListResponse listClasses(
            Long schoolId, String q, String academicYear, String statsFilter, int page, int size);

    AdminClassDtos.CreateClassResponse createClass(AdminClassRequests.CreateClassRequest request);

    AdminClassDtos.ActionResponse updateClass(Long classId, AdminClassRequests.UpdateClassRequest request);

    AdminClassDtos.DeletePreviewResponse getDeletePreview(Long schoolId, Long classId);

    AdminClassDtos.ActionResponse deleteClass(Long schoolId, Long classId, String performedBy);

    // Detail header + form options
    AdminClassDtos.ClassDetailResponse getClassDetail(Long schoolId, Long classId);

    AdminClassDtos.FormOptionsResponse getFormOptions(Long schoolId, Long classId);

    // Sections tab
    AdminClassDtos.SectionsTabResponse getSections(Long schoolId, Long classId);

    AdminClassDtos.ActionResponse addSection(Long classId, AdminClassRequests.AddSectionRequest request);

    AdminClassDtos.ActionResponse updateSection(Long classId, Long sectionId, AdminClassRequests.UpdateSectionRequest request);

    AdminClassDtos.ActionResponse deleteSection(Long schoolId, Long classId, Long sectionId, String performedBy);

    // Subjects tab
    AdminClassDtos.SubjectsTabResponse getSubjects(Long schoolId, Long classId);

    AdminClassDtos.ActionResponse addSubject(Long classId, AdminClassRequests.AddSubjectRequest request);

    AdminClassDtos.ActionResponse updateSubject(Long classId, Long classSubjectId, AdminClassRequests.UpdateSubjectRequest request);

    AdminClassDtos.ActionResponse deleteSubject(Long schoolId, Long classId, Long classSubjectId, String performedBy);

    // Teachers tab
    AdminClassDtos.TeachersTabResponse getTeacherAllocations(Long schoolId, Long classId);

    AdminClassDtos.ActionResponse assignTeacher(Long classId, AdminClassRequests.AssignTeacherRequest request);

    AdminClassDtos.ActionResponse removeAllocation(Long schoolId, Long classId, Long allocationId, String performedBy);

    AdminClassDtos.ActionResponse assignClassTeacher(Long classId, AdminClassRequests.AssignClassTeacherRequest request);

    // Students tab
    AdminClassDtos.StudentsTabResponse getStudents(
            Long schoolId, Long classId, String section, int page, int size);

    List<AdminClassDtos.StudentSearchResult> searchAddableStudents(Long schoolId, Long classId, String q);

    AdminClassDtos.ActionResponse addStudent(Long classId, AdminClassRequests.AddStudentRequest request);

    AdminClassDtos.ActionResponse transferStudent(
            Long classId, Long studentId, AdminClassRequests.TransferStudentRequest request);

    AdminClassDtos.ActionResponse removeStudent(Long schoolId, Long classId, Long studentId, String performedBy);

    byte[] getBulkTemplate();

    AdminClassDtos.BulkPreviewResponse previewBulkUpload(Long schoolId, Long classId, MultipartFile file);

    AdminClassDtos.BulkUploadResult bulkUpload(Long schoolId, Long classId, String performedBy, MultipartFile file);

    // Activity log tab
    AdminClassDtos.ActivityLogTabResponse getActivityLog(Long schoolId, Long classId, int page, int size);
}
