package com.gourav.docanalysis.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

@Service
public class ConversationService {

    private final MongoCollection<Document> chatCollection;

    public ConversationService(MongoDatabase database) {
        this.chatCollection = database.getCollection("chat_history");
    }

    public void save(String sessionId,
                     String question,
                     String answer,
                     List<String> retrievedChunkIds,
                     List<String> citationLabels) {
        Document doc = new Document()
                .append("sessionId", sessionId)
                .append("question", question)
                .append("answer", answer)
                .append("retrievedChunkIds", retrievedChunkIds)
                .append("citations", citationLabels)
                .append("createdAt", Instant.now());

        chatCollection.insertOne(doc);
    }

    public List<String> getRecentHistory(String sessionId, int limit) {
        List<String> history = new ArrayList<>();

        for (Document doc : chatCollection.find(eq("sessionId", sessionId))
                .sort(descending("createdAt"))
                .limit(limit)) {

            history.add("Q: " + doc.getString("question"));
            history.add("A: " + doc.getString("answer"));

            List<String> citations = doc.getList("citations", String.class);
            if (citations != null && !citations.isEmpty()) {
                history.add("Citations: " + String.join("; ", citations));
            }
        }

        return history;
    }
}