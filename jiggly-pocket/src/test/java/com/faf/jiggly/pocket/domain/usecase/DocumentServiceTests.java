package com.faf.jiggly.pocket.domain.usecase;

import com.faf.jiggly.pocket.domain.model.Document;
import com.faf.jiggly.pocket.domain.model.DocumentDraft;
import com.faf.jiggly.pocket.domain.model.UserId;
import com.faf.jiggly.pocket.domain.port.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTests {

    private static final UserId USER_ID = UserId.of(UUID.randomUUID());

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    @Captor
    private ArgumentCaptor<Document> documentCaptor;

    @Test
    void savesDocumentCarryingOverEveryDraftField() {
        // GIVEN
        var draft = DocumentDraft.builder()
                .userId(USER_ID)
                .title("Tax return")
                .description(Optional.of("2026 filing"))
                .build();
        when(documentRepository.save(any(Document.class))).thenAnswer(call -> call.getArgument(0));

        // WHEN
        documentService.saveDocument(draft);

        // THEN
        verify(documentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue())
                .extracting(Document::userId, Document::title, Document::description)
                .containsExactly(USER_ID, "Tax return", Optional.of("2026 filing"));
    }

    @Test
    void keepsDescriptionEmptyWhenTheDraftHasNone() {
        // GIVEN
        when(documentRepository.save(any(Document.class))).thenAnswer(call -> call.getArgument(0));

        // WHEN
        documentService.saveDocument(DocumentDraft.builder()
                .userId(USER_ID)
                .title("No description")
                .description(Optional.empty())
                .build());

        // THEN
        verify(documentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().description()).isEmpty();
    }

    @Test
    void assignsAnIdToTheSavedDocument() {
        // GIVEN
        when(documentRepository.save(any(Document.class))).thenAnswer(call -> call.getArgument(0));

        // WHEN
        documentService.saveDocument(DocumentDraft.builder()
                .userId(USER_ID)
                .title("Tax return")
                .description(Optional.empty())
                .build());

        // THEN
        verify(documentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().id()).isNotNull();
        assertThat(documentCaptor.getValue().path()).startsWith("/documents/");
    }

    @Test
    void givesEachSavedDocumentItsOwnIdAndPath() {
        // GIVEN
        when(documentRepository.save(any(Document.class))).thenAnswer(call -> call.getArgument(0));

        // WHEN
        var first = documentService.saveDocument(DocumentDraft.builder()
                .userId(USER_ID)
                .title("First")
                .description(Optional.empty())
                .build());
        var second = documentService.saveDocument(DocumentDraft.builder()
                .userId(USER_ID)
                .title("Second")
                .description(Optional.empty())
                .build());

        // THEN
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.path()).isNotEqualTo(second.path());
    }

    @Test
    void returnsWhatTheRepositoryPersisted() {
        // GIVEN
        var persisted = Document.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .title("Whatever the repository decided")
                .path("/documents/" + UUID.randomUUID())
                .description(Optional.empty())
                .build();
        when(documentRepository.save(any(Document.class))).thenReturn(persisted);

        // WHEN
        var saved = documentService.saveDocument(DocumentDraft.builder()
                .userId(USER_ID)
                .title("Tax return")
                .description(Optional.empty())
                .build());

        // THEN
        assertThat(saved).isSameAs(persisted);
    }

    @Test
    void rejectsANullDraft() {
        // WHEN / THEN
        assertThatNullPointerException().isThrownBy(() -> documentService.saveDocument(null));
    }
}
