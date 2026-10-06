package com.faf.jiggly.pocket.adapter.out.storage;

import com.faf.jiggly.pocket.domain.port.DocumentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public DocumentRepository documentRepository(DocumentConverter documentConverter) {
        return new DocumentInMemoryRepositoryImpl(documentConverter);
    }
}
