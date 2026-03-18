package com.gourav.docanalysis.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AtlasVectorSearchService {

    private final MongoCollection<Document> chunkCollection;
    private final int topK;
    private final int numCandidates;

    public AtlasVectorSearchService(MongoDatabase database,
                                    @Value("${app.retrieval.top-k}") int topK,
                                    @Value("${app.retrieval.num-candidates}") int numCandidates) {
        this.chunkCollection = database.getCollection("document_chunks");
        this.topK = topK;
        this.numCandidates = numCandidates;
    }

    public List<Document> search(List<Double> queryVector, String documentId) {
        Document vectorSearch = new Document()
                .append("index", "vector_index")
                .append("path", "embedding")
                .append("queryVector", queryVector)
                .append("numCandidates", numCandidates)
                .append("limit", topK);

        if (documentId != null && !documentId.isBlank()) {
            vectorSearch.append("filter", new Document("documentId", documentId));
        }

        List<Document> pipeline = List.of(
                new Document("$vectorSearch", vectorSearch),
                new Document("$project", new Document()
                        .append("chunkText", 1)
                        .append("documentId", 1)
                        .append("fileName", 1)
                        .append("chunkIndex", 1)
                        .append("pageNumber", 1)
                        .append("score", new Document("$meta", "vectorSearchScore")))
        );

        return chunkCollection.aggregate(pipeline).into(new ArrayList<>());
    }
}