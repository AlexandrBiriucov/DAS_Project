package com.faf.jiggly.pocket.domain.usecase;

import com.faf.jiggly.pocket.domain.model.Document;
import com.faf.jiggly.pocket.domain.model.DocumentDraft;
import com.faf.jiggly.pocket.domain.port.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document saveDocument(DocumentDraft document) {
        var path = "/documents/" + UUID.randomUUID();

        return documentRepository.save(toDocument(document, path));
    }

    

    private Document toDocument(DocumentDraft document, String path) {
        return Document.builder()
                .id(UUID.randomUUID())
                .userId(document.userId())
                .title(document.title())
                .description(document.description())
                .path(path)
                .build();

    }
}
