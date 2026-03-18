package com.gourav.docanalysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gourav.docanalysis.model.ContextSource;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class OpenAiService {

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiService(@Value("${app.openai.api-key}") String apiKey,
                         @Value("${app.openai.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    private String extractAnswer(JsonNode root) {
        try {
            JsonNode output = root.path("output");

            for (JsonNode item : output) {
                if ("message".equals(item.path("type").asText())) {
                    JsonNode contentArray = item.path("content");

                    for (JsonNode content : contentArray) {
                        if ("output_text".equals(content.path("type").asText())) {
                            return content.path("text").asText();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "No valid answer found.";
    }

    public String answerQuestion(String question,
                                 List<ContextSource> sources,
                                 List<String> history,
                                 String citationSummary) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api.openai.com/v1/responses");
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");

            String prompt = buildPrompt(question, sources, history, citationSummary);

            String body = """
                    {
                      "model": "%s",
                      "input": %s
                    }
                    """.formatted(model, objectMapper.writeValueAsString(prompt));

            post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

            String response = client.execute(post, httpResponse ->
                    new String(httpResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8)
            );

            JsonNode root = objectMapper.readTree(response);

            return extractAnswer(root);

        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI", e);
        }
    }

    private String buildPrompt(String question,
                               List<ContextSource> sources,
                               List<String> history,
                               String citationSummary) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a grounded document analysis assistant.\n");
        sb.append("Use only the provided source context when answering.\n");
        sb.append("Do not invent facts.\n");
        sb.append("When citing evidence, use this style exactly: [fileName, page X]\n");
        sb.append("If multiple pages support the answer, include multiple citations.\n\n");

        if (history != null && !history.isEmpty()) {
            sb.append("Recent conversation history:\n");
            history.forEach(h -> sb.append("- ").append(h).append("\n"));
            sb.append("\n");
        }

        if (citationSummary != null && !citationSummary.isBlank()) {
            sb.append("Available citations:\n");
            sb.append(citationSummary).append("\n\n");
        }

        sb.append("Retrieved document context:\n");
        for (int i = 0; i < sources.size(); i++) {
            ContextSource source = sources.get(i);
            sb.append("Source ").append(i + 1).append(":\n");
            sb.append("File: ").append(source.getFileName()).append("\n");
            sb.append("Document ID: ").append(source.getDocumentId()).append("\n");
            sb.append("Page: ").append(source.getPageNumber()).append("\n");
            sb.append("Chunk Index: ").append(source.getChunkIndex()).append("\n");
            sb.append("Score: ").append(source.getScore()).append("\n");
            sb.append("Text: ").append(source.getChunkText()).append("\n\n");
        }

        sb.append("Question: ").append(question).append("\n\n");
        sb.append("Answer requirements:\n");
        sb.append("1. Answer clearly and concisely.\n");
        sb.append("2. Cite supporting statements in the format [fileName, page X].\n");
        sb.append("3. If the context is insufficient, say so.\n");

        return sb.toString();
    }
}