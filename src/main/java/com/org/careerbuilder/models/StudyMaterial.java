package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.MaterialType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "study_materials",
        indexes = {
                @Index(name = "idx_material_subject_uploaded", columnList = "subject_id, uploaded_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 20)
    private MaterialType materialType;

    @Positive
    @Column(name = "pages")
    private Integer pages;

    @NotBlank
    @Column(name = "uploaded_by", nullable = false, length = 100)
    private String uploadedBy;

    @NotNull
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @NotBlank
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    // Explicit getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public MaterialType getMaterialType() { return materialType; }
    public void setMaterialType(MaterialType materialType) { this.materialType = materialType; }
    
    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }
    
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}