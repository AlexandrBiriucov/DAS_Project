package com.faf.jiggly.pocket.adapter.in.http;

import com.faf.jiggly.pocket.adapter.out.storage.EncryptedFileStore;
import com.faf.jiggly.pocket.domain.model.DocumentDraft;
import com.faf.jiggly.pocket.domain.model.UserId;
import com.faf.jiggly.pocket.domain.usecase.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


@RestController
@RequestMapping("/files")
public class FileController {
    private final DocumentService documentService;
    private final EncryptedFileStore fileStore;

    public FileController(DocumentService documentService, EncryptedFileStore fileStore) {
        this.documentService = documentService;
        this.fileStore = fileStore;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "description", required = false) String description,
                                    HttpServletRequest request) throws IOException {
        var session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        var userId = UserId.fromString((String) session.getAttribute("userId"));
        var title = cleanFilename(file.getOriginalFilename());

        var storedName = fileStore.store(file.getBytes());   // encrypted before it touches the disk

        var draft = DocumentDraft.builder()
                .userId(userId)
                .title(title)
                .description(Optional.ofNullable(description))
                .build();
        var document = documentService.saveDocument(draft, storedName);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", document.id().toString()));
    }

    // @GetMapping("getFile")
    // public ResponseEntity<?> download(@RequestParam("file")){



    // }


@GetMapping("/{id}")
    public ResponseEntity<?> download(@PathVariable String id, HttpServletRequest request) {
    var session = request.getSession(false);
    if (session == null || session.getAttribute("userId") == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
    }
    var userId = UserId.fromString((String) session.getAttribute("userId"));

    UUID documentId;
    try {
        documentId = UUID.fromString(id);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
    }

    var document = documentService.findById(documentId);
    if (document.isEmpty() || !document.get().userId().equals(userId)) {
        // same answer for "doesn't exist" and "not yours", so nobody can probe for other users' ids
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
    }

    var bytes = fileStore.read(document.get().path());   // decrypted here, only for the owner

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(document.get().title(), StandardCharsets.UTF_8).build().toString())
            .body(bytes);
}


    private static String cleanFilename(String original) {
        if (original == null || original.isBlank()) {
            return "unnamed";
        }
        // keep only the last part, in case the client sent a path
        var name = original.substring(Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\')) + 1);
        return name.isBlank() ? "unnamed" : name;
    }
}