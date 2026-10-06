package com.faf.jiggly.pocket.adapter.out.storage;

import com.faf.jiggly.pocket.domain.model.Document;
import com.faf.jiggly.pocket.domain.port.DocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DocumentInMemoryRepositoryImpl implements DocumentRepository{

    private final DocumentConverter documentConverter;

    private Map<UUID, DocumentEntity> documentMap = new ConcurrentHashMap<>();

    public DocumentInMemoryRepositoryImpl(DocumentConverter documentConverter) {
        this.documentConverter = documentConverter;
    }

    @Override
    public Document save(Document document) {
        var documentEntity = documentConverter.fromDomain(document);
        documentMap.put(document.id(), documentEntity);
        return document;
    }

    @Override
    public Document findById(UUID id) {
        var documentEntity = documentMap.get(id);
        return documentConverter.toDomain(documentEntity);
    }
}
