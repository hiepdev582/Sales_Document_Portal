package com.salesportal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String contentType;

    private long fileSize;

    @Column(nullable = false)
    private String encryptedFilePath;

    @Column(nullable = false, length = 64)
    private String ivHex; // 12 bytes = 24 hex chars

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Classification classification = Classification.INTERNAL;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DocumentEntity() {}

    public DocumentEntity(String title, String originalFilename, String contentType, long fileSize,
                          String encryptedFilePath, String ivHex, Department department,
                          Classification classification, User owner) {
        this.title = title;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.encryptedFilePath = encryptedFilePath;
        this.ivHex = ivHex;
        this.department = department;
        this.classification = classification;
        this.owner = owner;
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

    public String getEncryptedFilePath() { return encryptedFilePath; }
    public void setEncryptedFilePath(String encryptedFilePath) { this.encryptedFilePath = encryptedFilePath; }

    public String getIvHex() { return ivHex; }
    public void setIvHex(String ivHex) { this.ivHex = ivHex; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Classification getClassification() { return classification; }
    public void setClassification(Classification classification) { this.classification = classification; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
