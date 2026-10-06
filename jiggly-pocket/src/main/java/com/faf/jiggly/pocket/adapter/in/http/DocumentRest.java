package com.faf.jiggly.pocket.adapter.in.http;

import java.util.Objects;
import java.util.Optional;

public record DocumentRest(String title, Optional<String> description) {
    public DocumentRest{
        Objects.requireNonNull(title, "title is null");
        Objects.requireNonNull(description, "description is null");
    }
}
