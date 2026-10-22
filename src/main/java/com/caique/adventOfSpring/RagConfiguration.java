package com.caique.adventOfSpring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class RagConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RagConfiguration.class);

    @Value("vectorestore.json")
    private String vectoreStoreName;

    @Value("classpath:/docs/qa.txt")
    private Resource qa;

    @Bean
    @ConditionalOnProperty(prefix = "app.rag", name = "enabled", havingValue = "true")
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        try {
            var simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
            var vectorStoreFile = getVectoreStoreFile();

            if (vectorStoreFile.exists()) {
                logger.info("Vector file already exists");
                simpleVectorStore.load(vectorStoreFile);
            } else {
                logger.info("Vector file creation");
                TextReader textReader = new TextReader(qa);
                textReader.getCustomMetadata().put("filename", "qa.txt");
                List<Document> documents = textReader.get();
                TextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocuments = textSplitter.apply(documents);
                simpleVectorStore.add(splitDocuments);
                simpleVectorStore.save(vectorStoreFile);
            }

            return simpleVectorStore;
        } catch (Exception ex) {
            logger.warn("RAG vector store disabled because the embedding service is not available. Set a valid GROQ_API_KEY to enable it. Message: {}", ex.getMessage());
            return emptyVectorStore();
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.rag", name = "enabled", havingValue = "false", matchIfMissing = true)
    VectorStore disabledVectorStore() {
        return emptyVectorStore();
    }

    private VectorStore emptyVectorStore() {
        return new VectorStore() {
            @Override
            public void add(List<Document> documents) {
            }

            @Override
            public void delete(List<String> ids) {
            }

            @Override
            public void delete(Filter.Expression filter) {
            }

            @Override
            public List<Document> similaritySearch(SearchRequest request) {
                return List.of();
            }
        };
    }

    private File getVectoreStoreFile() {
        Path path = Paths.get("src", "main", "resources", "data");
        String absolutePath = path.toFile().getAbsolutePath() + "/" + vectoreStoreName;
        return new File(absolutePath);
    }
}
