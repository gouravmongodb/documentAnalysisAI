package com.gourav.docanalysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gourav.docanalysis.model.RetrievalCandidate;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class VoyageRerankService {

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VoyageRerankService(@Value("${app.voyage.api-key}") String apiKey,
                               @Value("${app.voyage.rerank-model:rerank-2.5-lite}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public List<RetrievalCandidate> rerank(String query,
                                           List<RetrievalCandidate> candidates,
                                           int topK) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.voyageai.com/v1/rerank");
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");

            List<String> docs = candidates.stream()
                    .map(RetrievalCandidate::getChunkText)
                    .toList();

            String body = objectMapper.writeValueAsString(new RerankRequest(
                    query,
                    docs,
                    model,
                    Math.min(topK, candidates.size())
            ));

            post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            String response = client.execute(post, httpResponse ->
                    new String(httpResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8)
            );

            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.path("data");

            List<RetrievalCandidate> reranked = new ArrayList<>();

            for (JsonNode item : data) {
                int index = item.path("index").asInt();
                double relevanceScore = item.path("relevance_score").asDouble();

                RetrievalCandidate candidate = candidates.get(index);
                candidate.setRerankScore(relevanceScore);
                reranked.add(candidate);
            }

            reranked.sort(Comparator.comparing(
                    RetrievalCandidate::getRerankScore,
                    Comparator.nullsLast(Comparator.reverseOrder())
            ));

            return reranked;
        } catch (Exception e) {
            throw new RuntimeException("Failed to rerank with Voyage AI", e);
        }
    }

    public static class RerankRequest {
        public String query;
        public List<String> documents;
        public String model;
        public Integer top_k;

        public RerankRequest(String query, List<String> documents, String model, Integer top_k) {
            this.query = query;
            this.documents = documents;
            this.model = model;
            this.top_k = top_k;
        }
    }
}