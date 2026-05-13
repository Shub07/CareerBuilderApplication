package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;

public record LearningMaterialVisibilityRequest(@NotNull Boolean visibleToStudents) {
}
