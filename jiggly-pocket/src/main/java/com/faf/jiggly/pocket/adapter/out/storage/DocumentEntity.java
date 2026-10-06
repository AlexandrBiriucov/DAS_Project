package com.faf.jiggly.pocket.adapter.out.storage;

import java.time.Instant;
import java.util.Optional;

public record DocumentEntity(String id, String userId, String title, String path, Optional<String> description, Instant creationTime) {
}
