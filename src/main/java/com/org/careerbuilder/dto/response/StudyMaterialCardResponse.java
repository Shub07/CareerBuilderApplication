package com.org.careerbuilder.dto.response;

public record StudyMaterialCardResponse(
        Long materialId,
        Long subjectId,
        String subjectName,
        String title,
        String fileType,
        Integer pages,
        String uploadedBy,
        String uploadedAtText
) {}