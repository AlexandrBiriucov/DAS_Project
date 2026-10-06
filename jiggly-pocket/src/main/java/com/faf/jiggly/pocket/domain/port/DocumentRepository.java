package com.faf.jiggly.pocket.domain.port;

import com.faf.jiggly.pocket.domain.model.Document;

import java.util.UUID;
import java.util.Optional;

public interface DocumentRepository {
    public Document save(Document document);
    Optional<Document> findById(UUID id);
}
