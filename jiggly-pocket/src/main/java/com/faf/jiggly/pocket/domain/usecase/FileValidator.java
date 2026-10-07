package com.faf.jiggly.pocket.domain.usecase;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Set;


@Component
public class FileValidator {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png", "image/jpeg", "image/gif", "application/pdf", "text/plain");

    // extensions that promise a specific type, and must really be that type
    private static final Map<String, String> EXTENSION_TO_TYPE = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "gif", "image/gif",
            "pdf", "application/pdf");

    private final Tika tika = new Tika();
    private final long maxBytes;

    public FileValidator(@Value("${jiggly.max-upload-bytes:10485760}") long maxBytes) {
        this.maxBytes = maxBytes;
    }

    /** Returns the detected content type, or throws InvalidFileException. */
    public String validate(String filename, byte[] content) {
        if (content.length == 0) {
            throw new InvalidFileException(400, "File is empty");
        }
        if (content.length > maxBytes) {
            throw new InvalidFileException(413, "File is too large");
        }

        String detected = tika.detect(content);   // looks at the bytes, not the name

        if (!ALLOWED_TYPES.contains(detected)) {
            throw new InvalidFileException(415, "File type not allowed: " + detected);
        }

        String expected = EXTENSION_TO_TYPE.get(extensionOf(filename));
        if (expected != null && !expected.equals(detected)) {
            throw new InvalidFileException(415, "File content does not match its extension");
        }
        return detected;
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static class InvalidFileException extends RuntimeException {
        private final int status;

        public InvalidFileException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }
}