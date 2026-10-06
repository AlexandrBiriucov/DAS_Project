package com.faf.jiggly.pocket.domain.model;

import lombok.Builder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Builder
public record Document(UUID id, UserId userId, String title, String path, Optional<String> description) {
    public Document{
        Objects.requireNonNull(id, "id is null");
        Objects.requireNonNull(userId, "userId is null");
        Objects.requireNonNull(title, "title is null");
        Objects.requireNonNull(path, "path is null");
        Objects.requireNonNull(description, "description is null");
    }
}
