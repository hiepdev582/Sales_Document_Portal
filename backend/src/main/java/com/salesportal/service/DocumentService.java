package com.salesportal.service;

import com.salesportal.dto.DocumentDto;
import com.salesportal.entity.Classification;
import com.salesportal.entity.Department;
import com.salesportal.entity.DocumentEntity;
import com.salesportal.entity.User;
import com.salesportal.repository.DocumentRepository;
import com.salesportal.security.AbacEvaluator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CryptoService cryptoService;
    private final AbacEvaluator abacEvaluator;
    private final String storageDir;

    public DocumentService(DocumentRepository documentRepository,
            CryptoService cryptoService,
            AbacEvaluator abacEvaluator,
            @Value("${app.crypto.storage-dir:./encrypted-documents}") String storageDir) {
        this.documentRepository = documentRepository;
        this.cryptoService = cryptoService;
        this.abacEvaluator = abacEvaluator;
        this.storageDir = storageDir;

        // Ensure encrypted documents directory exists
        File dir = new File(storageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public DocumentDto uploadDocument(MultipartFile file, String title, Department department,
            Classification classification, User currentUser) throws IOException {
        byte[] originalBytes = file.getBytes();
        byte[] iv = cryptoService.generateIv();
        byte[] encryptedBytes = cryptoService.encrypt(originalBytes, iv);

        String fileNameOnDisk = UUID.randomUUID() + ".enc";
        Path targetPath = Paths.get(storageDir, fileNameOnDisk);
        Files.write(targetPath, encryptedBytes);

        String ivHex = HexFormat.of().formatHex(iv);

        DocumentEntity document = new DocumentEntity(
                title != null && !title.isBlank() ? title : file.getOriginalFilename(),
                file.getOriginalFilename(),
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                file.getSize(),
                targetPath.toString(),
                ivHex,
                department != null ? department : currentUser.getDepartment(),
                classification != null ? classification : Classification.INTERNAL,
                currentUser);

        DocumentEntity saved = documentRepository.save(document);
        return new DocumentDto(saved);
    }

    public List<DocumentDto> getAllAccessibleDocuments(User currentUser) {
        List<DocumentEntity> allDocs = documentRepository.findAll();
        return allDocs.stream()
                .filter(doc -> abacEvaluator.canAccessDocument(currentUser, doc))
                .map(DocumentDto::new)
                .collect(Collectors.toList());
    }

    public DocumentDto getDocumentDetails(Long id, User currentUser) {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));

        if (!abacEvaluator.canAccessDocument(currentUser, doc)) {
            throw new AccessDeniedException("ABAC Access Denied: You do not have permission to view document #" + id);
        }

        return new DocumentDto(doc);
    }

    public byte[] downloadDecryptedDocument(Long id, User currentUser) throws IOException {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));

        if (!abacEvaluator.canAccessDocument(currentUser, doc)) {
            throw new AccessDeniedException("ABAC Access Denied: You do not have permission to access document #" + id);
        }

        byte[] encryptedBytes = Files.readAllBytes(Paths.get(doc.getEncryptedFilePath()));
        byte[] iv = HexFormat.of().parseHex(doc.getIvHex());

        return cryptoService.decrypt(encryptedBytes, iv);
    }

    public void deleteDocument(Long id, User currentUser) throws IOException {
        DocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));

        if (!abacEvaluator.canDeleteDocument(currentUser, doc)) {
            throw new AccessDeniedException("BFLA/ABAC Access Denied: Only ADMIN or Owner can delete document #" + id);
        }

        Path path = Paths.get(doc.getEncryptedFilePath());
        if (Files.exists(path)) {
            Files.delete(path);
        }

        documentRepository.delete(doc);
    }
}
