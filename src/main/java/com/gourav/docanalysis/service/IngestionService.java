package com.gourav.docanalysis.service;

import com.gourav.docanalysis.model.PageChunk;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IngestionService {

    private final MongoCollection<Document> chunkCollection;
    private final ChunkingService chunkingService;
    private final VoyageAiService voyageAiService;

    public IngestionService(MongoDatabase database,
                            ChunkingService chunkingService,
                            VoyageAiService voyageAiService) {
        this.chunkCollection = database.getCollection("document_chunks");
        this.chunkingService = chunkingService;
        this.voyageAiService = voyageAiService;
    }

    public void ingest(String documentId, String fileName, String text) {
        List<String> chunks = chunkingService.chunkText(text, 1000, 150);

        int index = 0;
        for (String chunk : chunks) {
            List<Double> embedding = voyageAiService.embedText(chunk);

            Document doc = new Document()
                    .append("documentId", documentId)
                    .append("fileName", fileName)
                    .append("chunkIndex", index)
                    .append("chunkText", chunk)
                    .append("embedding", embedding)
                    .append("metadata", new HashMap<String, Object>())
                    .append("createdAt", Instant.now());

            chunkCollection.insertOne(doc);
            index++;
        }
    }

    public void ingestPageAwareChunks(String documentId, String fileName, List<PageChunk> pageChunks) {
        for (PageChunk pageChunk : pageChunks) {
            List<Double> embedding = voyageAiService.embedText(pageChunk.getText());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("pageNumber", pageChunk.getPageNumber());

            Document doc = new Document()
                    .append("documentId", documentId)
                    .append("fileName", fileName)
                    .append("chunkIndex", pageChunk.getChunkIndex())
                    .append("pageNumber", pageChunk.getPageNumber())
                    .append("chunkText", pageChunk.getText())
                    .append("embedding", embedding)
                    .append("metadata", metadata)
                    .append("createdAt", Instant.now());

            chunkCollection.insertOne(doc);
        }
    }
}