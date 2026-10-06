package com.faf.jiggly.pocket.adapter.out.storage;

import com.faf.jiggly.pocket.domain.model.Document;
import com.faf.jiggly.pocket.domain.model.UserId;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentConverter {
    public DocumentEntity fromDomain(Document document) {
        return new DocumentEntity(
                document.id().toString(),
                document.userId().asString(),
                document.title(),
                document.path(),
                document.description(),
                java.time.Instant.now()
        );
    }
    public Document toDomain(DocumentEntity documentEntity) {
        return new Document(
                UUID.fromString(documentEntity.id()),
                UserId.fromString(documentEntity.userId()),
                documentEntity.title(),
                documentEntity.path(),
                documentEntity.description()
        );
    }
}
