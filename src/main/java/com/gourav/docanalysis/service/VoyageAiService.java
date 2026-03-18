package com.gourav.docanalysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoyageAiService {

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VoyageAiService(@Value("${app.voyage.api-key}") String apiKey,
                           @Value("${app.voyage.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public List<Double> embedText(String input) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.voyageai.com/v1/embeddings");
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");

            String body = """
                    {
                      "input": [%s],
                      "model": "%s"
                    }
                    """.formatted(objectMapper.writeValueAsString(input), model);

            post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            String response = client.execute(post, httpResponse ->
                    new String(httpResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8)
            );

            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddingNode = root.path("data").get(0).path("embedding");

            List<Double> embedding = new ArrayList<>();
            for (JsonNode node : embeddingNode) {
                embedding.add(node.asDouble());
            }
            return embedding;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Voyage embedding", e);
        }
    }
}