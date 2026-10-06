package com.faf.jiggly.pocket.adapter.out.storage;

import com.faf.jiggly.pocket.domain.usecase.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class EncryptedFileStore {
    private final Path root;
    private final EncryptionService encryption;

    public EncryptedFileStore(@Value("${jiggly.storage-dir:uploads}") String dir,
                              EncryptionService encryption) throws IOException {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        this.encryption = encryption;
        Files.createDirectories(root);
    }

    /** Encrypts the bytes, writes them under a random name, returns that name. */
    public String store(byte[] plainBytes) {
        var storedName = UUID.randomUUID().toString();
        try {
            Files.write(root.resolve(storedName), encryption.encrypt(plainBytes));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write file", e);
        }
        return storedName;
    }

    public byte[] read(String storedName) {
    UUID.fromString(storedName);   // throws if it is not a UUID, so no path tricks
    try {
        return encryption.decrypt(Files.readAllBytes(root.resolve(storedName)));
    } catch (IOException e) {
        throw new UncheckedIOException("Could not read file", e);
    }
}


}