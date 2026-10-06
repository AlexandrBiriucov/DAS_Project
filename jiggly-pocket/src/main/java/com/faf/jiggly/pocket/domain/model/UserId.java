package com.faf.jiggly.pocket.domain.model;

import java.util.Objects;
import java.util.UUID;


public record UserId(UUID id) {
    public UserId {
        Objects.requireNonNull(id);
    }

    public static UserId fromString(String id) {
        return new UserId(UUID.fromString(id));
    }

    public static UserId of(UUID id) {
        return new UserId(id);
    }

    public String asString(){
        return id.toString();
    }
}
