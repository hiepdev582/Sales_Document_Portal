package com.salesportal.controller;

import com.salesportal.dto.DocumentDto;
import com.salesportal.entity.Classification;
import com.salesportal.entity.Department;
import com.salesportal.entity.User;
import com.salesportal.security.CustomUserDetails;
import com.salesportal.service.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> listDocuments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(documentService.getAllAccessibleDocuments(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocumentDetails(@PathVariable Long id,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(documentService.getDocumentDetails(id, userDetails.getUser()));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "department", required = false) Department department,
            @RequestParam(value = "classification", required = false) Classification classification,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        DocumentDto doc = documentService.uploadDocument(
                file, title, department, classification, userDetails.getUser()
        );
        return ResponseEntity.ok(doc);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        User user = userDetails.getUser();
        DocumentDto details = documentService.getDocumentDetails(id, user);
        byte[] decryptedData = documentService.downloadDecryptedDocument(id, user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + details.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(details.getContentType()))
                .body(decryptedData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        documentService.deleteDocument(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
