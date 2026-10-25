package com.trustvault.service;

import com.trustvault.model.*;
import com.trustvault.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class TrustVaultService {

    private final DocumentRepository documents;
    private final SharePermissionRepository shares;
    private final AuditLogRepository audit;

    public TrustVaultService(
            DocumentRepository documents,
            SharePermissionRepository shares,
            AuditLogRepository audit) {
        this.documents = documents;
        this.shares = shares;
        this.audit = audit;
    }

    @Transactional
    public DocumentRecord create(String title, String content, String owner) {
        DocumentRecord doc =
                documents.save(new DocumentRecord(title, content, owner));

        log(owner, "DOCUMENT_CREATED", doc.getId(),
                "Created document: " + title);
        return doc;
    }

    @Transactional
    public DocumentRecord createUploadedFile(
            String title, MultipartFile file, String owner)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please choose a file first.");
        }

        if (file.getSize() > 10L * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Maximum file size is 10 MB.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            fileName = "uploaded-file";
        }

        fileName = fileName.replace('\\', '/');
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        if (fileName.isBlank()) {
            fileName = "uploaded-file";
        }

        if (fileName.length() > 240) {
            fileName = fileName.substring(fileName.length() - 240);
        }

        String documentTitle =
                title == null || title.isBlank()
                        ? fileName : title.trim();

        if (documentTitle.length() > 240) {
            documentTitle = documentTitle.substring(0, 240);
        }

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        DocumentRecord doc = new DocumentRecord(
                documentTitle, "Uploaded file: " + fileName, owner);

        doc.setFileName(fileName);
        doc.setContentType(contentType);
        doc.setFileSize(file.getSize());
        doc.setFileData(file.getBytes());

        doc = documents.save(doc);

        log(owner, "FILE_UPLOADED", doc.getId(),
                "Uploaded file: " + fileName);

        return doc;
    }

    @Transactional
    public DocumentRecord readAttachment(Long id, String username) {
        DocumentRecord doc = read(id, username);

        if (doc.getFileData() == null || doc.getFileName() == null) {
            throw new IllegalArgumentException(
                    "This document has no uploaded file.");
        }

        log(username, "FILE_DOWNLOADED", id,
                "Downloaded file: " + doc.getFileName());

        return doc;
    }

    public List<DocumentRecord> listVisible(String username) {
        List<DocumentRecord> result =
                new java.util.ArrayList<>(
                        documents.findByOwnerUsername(username));

        shares.findByRecipientUsernameAndActiveTrue(username)
                .forEach(s -> documents.findById(s.getDocumentId())
                        .ifPresent(d -> {
                            if (result.stream().noneMatch(
                                    x -> x.getId().equals(d.getId()))) {
                                result.add(d);
                            }
                        }));

        return result;
    }

    public DocumentRecord read(Long id, String username) {
        DocumentRecord d = documents.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Document not found"));

        boolean isOwner = d.getOwnerUsername().equals(username);

        boolean isShared = shares
                .findByDocumentIdAndRecipientUsername(id, username)
                .filter(SharePermission::isActive)
                .isPresent();

        if (!isOwner && !isShared) {
            log(username, "ACCESS_DENIED", id,
                    "Attempted to access document without active permission");

            throw new SecurityException(
                    "You do not have permission to access this document.");
        }

        log(username, "DOCUMENT_VIEWED", id,
                "Viewed document: " + d.getTitle());

        return d;
    }

    @Transactional
    public SharePermission share(
            Long id, String owner, String recipient) {

        DocumentRecord d = documents.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Document not found"));

        if (!d.getOwnerUsername().equals(owner)) {
            throw new SecurityException(
                    "Only the document owner can share it.");
        }

        if (owner.equals(recipient)) {
            throw new IllegalArgumentException(
                    "You cannot share a document with yourself.");
        }

        SharePermission permission = shares
                .findByDocumentIdAndRecipientUsername(id, recipient)
                .orElseGet(() ->
                        new SharePermission(id, recipient, "READ"));

        permission.setActive(true);
        shares.save(permission);

        log(owner, "PERMISSION_GRANTED", id,
                "Granted READ access to " + recipient);

        return permission;
    }

    @Transactional
    public void revoke(Long id, String owner, String recipient) {
        DocumentRecord d = documents.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Document not found"));

        if (!d.getOwnerUsername().equals(owner)) {
            throw new SecurityException(
                    "Only the document owner can revoke access.");
        }

        SharePermission permission = shares
                .findByDocumentIdAndRecipientUsername(id, recipient)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Share permission not found"));

        permission.setActive(false);
        shares.save(permission);

        log(owner, "PERMISSION_REVOKED", id,
                "Revoked access for " + recipient);
    }

    public List<AuditLog> auditLogs() {
        return audit.findAllByOrderByEventTimeDesc();
    }

    private void log(
            String actor, String action, Long id, String details) {
        audit.save(new AuditLog(actor, action, id, details));
    }
}
