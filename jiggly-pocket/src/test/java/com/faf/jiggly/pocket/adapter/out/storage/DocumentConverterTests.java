package com.faf.jiggly.pocket.adapter.out.storage;

import com.faf.jiggly.pocket.domain.model.Document;
import com.faf.jiggly.pocket.domain.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DocumentConverterTests {

    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final UUID USER_UUID = UUID.randomUUID();

    private final DocumentConverter converter = new DocumentConverter();

    @Test
    void mapsEveryDomainFieldOntoTheEntity() {
        // GIVEN
        var document = new Document(
                DOCUMENT_ID,
                UserId.of(USER_UUID),
                "Tax return",
                "/documents/" + DOCUMENT_ID,
                Optional.of("2026 filing"));

        // WHEN
        var entity = converter.fromDomain(document);

        // THEN
        assertThat(entity.id()).isEqualTo(DOCUMENT_ID.toString());
        assertThat(entity.userId()).isEqualTo(USER_UUID.toString());
        assertThat(entity.title()).isEqualTo("Tax return");
        assertThat(entity.path()).isEqualTo("/documents/" + DOCUMENT_ID);
        assertThat(entity.description()).contains("2026 filing");
    }

    @Test
    void keepsAnEmptyDescriptionEmptyWhenConvertingToTheEntity() {
        // GIVEN
        var document = document(Optional.empty());

        // WHEN
        var entity = converter.fromDomain(document);

        // THEN
        assertThat(entity.description()).isEmpty();
    }

    @Test
    void stampsTheEntityWithACreationTime() {
        // GIVEN
        var document = document(Optional.empty());
        var before = Instant.now();

        // WHEN
        var entity = converter.fromDomain(document);

        // THEN
        assertThat(entity.creationTime()).isBetween(before, Instant.now());
    }

    @Test
    void mapsEveryEntityFieldOntoTheDomainObject() {
        // GIVEN
        var entity = new DocumentEntity(
                DOCUMENT_ID.toString(),
                USER_UUID.toString(),
                "Tax return",
                "/documents/" + DOCUMENT_ID,
                Optional.of("2026 filing"),
                Instant.now());

        // WHEN
        var document = converter.toDomain(entity);

        // THEN
        assertThat(document.id()).isEqualTo(DOCUMENT_ID);
        assertThat(document.userId()).isEqualTo(UserId.of(USER_UUID));
        assertThat(document.title()).isEqualTo("Tax return");
        assertThat(document.path()).isEqualTo("/documents/" + DOCUMENT_ID);
        assertThat(document.description()).contains("2026 filing");
    }

    @Test
    void keepsAnEmptyDescriptionEmptyWhenConvertingToTheDomainObject() {
        // GIVEN
        var entity = new DocumentEntity(
                DOCUMENT_ID.toString(),
                USER_UUID.toString(),
                "Tax return",
                "/documents/" + DOCUMENT_ID,
                Optional.empty(),
                Instant.now());

        // WHEN
        var document = converter.toDomain(entity);

        // THEN
        assertThat(document.description()).isEmpty();
    }

    @Test
    void survivesARoundTripThroughTheEntity() {
        // GIVEN
        var document = document(Optional.of("2026 filing"));

        // WHEN
        var roundTripped = converter.toDomain(converter.fromDomain(document));

        // THEN
        assertThat(roundTripped).isEqualTo(document);
    }

    @Test
    void rejectsAnEntityWhoseIdIsNotAUuid() {
        // GIVEN
        var entity = new DocumentEntity(
                "not-a-uuid",
                USER_UUID.toString(),
                "Tax return",
                "/documents/1",
                Optional.empty(),
                Instant.now());

        // WHEN / THEN
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> converter.toDomain(entity));
    }

    private static Document document(Optional<String> description) {
        return new Document(
                DOCUMENT_ID,
                UserId.of(USER_UUID),
                "Tax return",
                "/documents/" + DOCUMENT_ID,
                description);
    }
}
