package com.trustvault.controller;

import com.trustvault.model.*;
import com.trustvault.service.TrustVaultService;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TrustVaultController {

    private final TrustVaultService service;

    public TrustVaultController(TrustVaultService service) {
        this.service = service;
    }

    public record CreateDocumentRequest(
            @NotBlank String title,
            @NotBlank String content) {}

    public record ShareRequest(
            @NotBlank String recipientUsername) {}

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentRecord create(
            @RequestBody CreateDocumentRequest request,
            Authentication auth) {
        return service.create(
                request.title(), request.content(), auth.getName());
    }

    @GetMapping("/documents")
    public List<DocumentRecord> list(Authentication auth) {
        return service.listVisible(auth.getName());
    }

    @GetMapping("/documents/{id}")
    public DocumentRecord read(
            @PathVariable Long id, Authentication auth) {
        try {
            return service.read(id, auth.getName());
        } catch (SecurityException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // Upload a file from the user's computer
    @PostMapping(
            value = "/documents/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentRecord upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            Authentication auth) {

        try {
            return service.createUploadedFile(
                    title, file, auth.getName());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not upload the file.");
        }
    }

    // Download a file only if the current user has permission
    @GetMapping("/documents/{id}/download")
    public ResponseEntity<byte[]> download(
            @PathVariable Long id, Authentication auth) {

        try {
            DocumentRecord doc =
                    service.readAttachment(id, auth.getName());

            String type = doc.getContentType() == null
                    ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                    : doc.getContentType();

            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(type);
            } catch (IllegalArgumentException e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            String fileName = doc.getFileName() == null
                    ? "download" : doc.getFileName();

            ContentDisposition disposition =
                    ContentDisposition.attachment()
                            .filename(fileName, StandardCharsets.UTF_8)
                            .build();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            disposition.toString())
                    .body(doc.getFileData());

        } catch (SecurityException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/documents/{id}/share")
    @ResponseStatus(HttpStatus.CREATED)
    public SharePermission share(
            @PathVariable Long id,
            @RequestBody ShareRequest request,
            Authentication auth) {

        try {
            return service.share(
                    id, auth.getName(), request.recipientUsername());
        } catch (SecurityException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/documents/{id}/share/{recipient}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @PathVariable Long id,
            @PathVariable String recipient,
            Authentication auth) {

        try {
            service.revoke(id, auth.getName(), recipient);
        } catch (SecurityException e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/audit")
    public List<AuditLog> audit() {
        return service.auditLogs();
    }
}