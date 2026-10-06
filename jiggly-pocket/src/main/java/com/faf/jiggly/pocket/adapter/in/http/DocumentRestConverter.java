package com.faf.jiggly.pocket.adapter.in.http;

import com.faf.jiggly.pocket.domain.model.DocumentDraft;
import com.faf.jiggly.pocket.domain.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class DocumentRestConverter {
    public DocumentDraft toDomain(DocumentRest rest, UserId userId) {
        return DocumentDraft.builder()
                .userId(userId)
                .description(rest.description())
                .title(rest.title())
                .build();
    }
}
