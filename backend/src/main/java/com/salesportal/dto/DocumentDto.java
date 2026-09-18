package com.salesportal.dto;

import com.salesportal.entity.Classification;
import com.salesportal.entity.Department;
import com.salesportal.entity.DocumentEntity;

import java.time.LocalDateTime;

public class DocumentDto {
    private Long id;
    private String title;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private Department department;
    private Classification classification;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;

    public DocumentDto() {}

    public DocumentDto(DocumentEntity entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.originalFilename = entity.getOriginalFilename();
        this.contentType = entity.getContentType();
        this.fileSize = entity.getFileSize();
        this.department = entity.getDepartment();
        this.classification = entity.getClassification();
        this.ownerId = entity.getOwner().getId();
        this.ownerName = entity.getOwner().getFullName();
        this.createdAt = entity.getCreatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Classification getClassification() { return classification; }
    public void setClassification(Classification classification) { this.classification = classification; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
