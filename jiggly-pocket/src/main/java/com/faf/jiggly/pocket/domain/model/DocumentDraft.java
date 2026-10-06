package com.faf.jiggly.pocket.domain.model;

import lombok.Builder;

import java.util.Objects;
import java.util.Optional;

@Builder
public record DocumentDraft(UserId userId, String title, Optional<String> description) {
    public DocumentDraft {
        Objects.requireNonNull(userId, "userId is null");
        Objects.requireNonNull(title, "title is null");
        Objects.requireNonNull(description, "description is null");
    }
}
