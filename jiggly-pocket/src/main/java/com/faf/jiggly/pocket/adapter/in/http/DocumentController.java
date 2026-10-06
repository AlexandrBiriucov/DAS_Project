package com.faf.jiggly.pocket.adapter.in.http;

import com.faf.jiggly.pocket.domain.model.UserId;
import com.faf.jiggly.pocket.domain.usecase.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService documentService;
    private final DocumentRestConverter converter;

    private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);

    public DocumentController(DocumentService documentService, DocumentRestConverter converter) {
        this.documentService = documentService;
        this.converter = converter;
    }

    @PostMapping
    public ResponseEntity<UUID> saveDocument(@RequestBody DocumentRest document) {
        LOG.info("saveDocument called, payload length={}", document.title());
        var userId = UserId.of(UUID.randomUUID()); //TODO get userId from authentication context once implemented
        var docMetadata = documentService.saveDocument(converter.toDomain(document, userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(docMetadata.id());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> getDocument(@PathVariable String id) {
        LOG.info("getDocument called, id={}", id);
        return ResponseEntity.ok().build();
    }
}
