package com.faf.jiggly.pocket.domain.port;

import com.faf.jiggly.pocket.domain.model.Document;

import java.util.UUID;

public interface DocumentRepository {
    public Document save(Document document);
    public Document findById(UUID id);
}
