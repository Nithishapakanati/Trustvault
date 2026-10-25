package com.trustvault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class DocumentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private String ownerUsername;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private String fileName;
    private String contentType;
    private Long fileSize;

    @Lob
    @JsonIgnore
    private byte[] fileData;

    public DocumentRecord() {
    }

    public DocumentRecord(
            String title, String content, String ownerUsername) {
        this.title = title;
        this.content = content;
        this.ownerUsername = ownerUsername;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    @JsonIgnore
    public byte[] getFileData() {
        return fileData;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
}