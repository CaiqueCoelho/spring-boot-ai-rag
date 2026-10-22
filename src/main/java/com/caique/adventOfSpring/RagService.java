package com.caique.adventOfSpring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);
    private static final int TOP_K = 5;
    private static final double MIN_CONFIDENCE = 0.55;
    private static final String DOC_FILTER = "filename == 'qa.txt'";

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public String ask(String question) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(TOP_K)
                .similarityThreshold(0.0)
                .filterExpression(DOC_FILTER)
                .build());

        if (documents.isEmpty()) {
            logger.warn("Filtered RAG search returned no documents for '{}'; retrying without metadata filter.", question);
            documents = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(question)
                    .topK(TOP_K)
                    .similarityThreshold(0.0)
                    .build());
        }

        if (documents.isEmpty()) {
            return "I could not find relevant context in the knowledge base.";
        }

        Double topScore = documents.get(0).getScore();
        double bestScore = topScore == null ? 0.0 : topScore;

        logger.info("RAG search for '{}': {} results, best score {}", question, documents.size(), bestScore);

        if (bestScore < MIN_CONFIDENCE) {
            return "I found related information, but the confidence is too low to answer reliably. Please rephrase the question.";
        }

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        return chatClient.prompt()
                .user(user -> user.text("Use only the information in the context below. If the answer is not present, say you do not know.\n\nContext:\n" + context + "\n\nQuestion:\n" + question))
                .call()
                .content();
    }
}
