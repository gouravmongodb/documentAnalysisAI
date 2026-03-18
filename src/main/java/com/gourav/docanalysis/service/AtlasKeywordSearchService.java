package com.gourav.docanalysis.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AtlasKeywordSearchService {

    private final MongoCollection<Document> chunkCollection;
    private final int topK;

    public AtlasKeywordSearchService(MongoDatabase database,
                                     @Value("${app.retrieval.top-k}") int topK) {
        this.chunkCollection = database.getCollection("document_chunks");
        this.topK = topK;
    }

    public List<Document> search(String query, String documentId) {
        Document textOperator = new Document("query", query)
                .append("path", "chunkText");

        Document searchStage = new Document("index", "text_index")
                .append("text", textOperator);

        List<Document> pipeline = new ArrayList<>();

        pipeline.add(new Document("$search", searchStage));

        if (documentId != null && !documentId.isBlank()) {
            pipeline.add(new Document("$match", new Document("documentId", documentId)));
        }

        pipeline.add(new Document("$limit", topK));

        pipeline.add(new Document("$project", new Document()
                .append("chunkText", 1)
                .append("documentId", 1)
                .append("fileName", 1)
                .append("pageNumber", 1)
                .append("chunkIndex", 1)
                .append("score", new Document("$meta", "searchScore"))));

        return chunkCollection.aggregate(pipeline).into(new ArrayList<>());
    }
}