package com.gourav.docanalysis.service;

import com.gourav.docanalysis.model.RetrievalCandidate;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HybridRetrievalServiceSimpleWeighted {

    private final AtlasVectorSearchService atlasVectorSearchService;
    private final AtlasKeywordSearchService atlasKeywordSearchService;

    public HybridRetrievalServiceSimpleWeighted(AtlasVectorSearchService atlasVectorSearchService,
                                                AtlasKeywordSearchService atlasKeywordSearchService) {
        this.atlasVectorSearchService = atlasVectorSearchService;
        this.atlasKeywordSearchService = atlasKeywordSearchService;
    }

    public List<RetrievalCandidate> retrieve(List<Double> queryVector,
                                             String query,
                                             String documentId) {

        List<Document> vectorResults = atlasVectorSearchService.search(queryVector, documentId);
        List<Document> keywordResults = atlasKeywordSearchService.search(query, documentId);

        Map<String, RetrievalCandidate> merged = new LinkedHashMap<>();

        for (Document d : vectorResults) {
            String chunkId = d.getObjectId("_id").toHexString();

            merged.put(chunkId, new RetrievalCandidate(
                    chunkId,
                    d.getString("chunkText"),
                    d.getString("fileName"),
                    d.getString("documentId"),
                    d.getInteger("pageNumber"),
                    d.getInteger("chunkIndex"),
                    getNumericScore(d.get("score")),
                    null,
                    null,
                    null,
                    null,
                    null,
                    "vector"
            ));
        }

        for (Document d : keywordResults) {
            String chunkId = d.getObjectId("_id").toHexString();

            if (merged.containsKey(chunkId)) {
                RetrievalCandidate existing = merged.get(chunkId);
                existing.setKeywordScore(getNumericScore(d.get("score")));
                existing.setRetrievalSource("hybrid");
            } else {
                merged.put(chunkId, new RetrievalCandidate(
                        chunkId,
                        d.getString("chunkText"),
                        d.getString("fileName"),
                        d.getString("documentId"),
                        d.getInteger("pageNumber"),
                        d.getInteger("chunkIndex"),
                        null,
                        getNumericScore(d.get("score")),
                        null,
                        null,
                        null,
                        null,
                        "keyword"
                ));
            }
        }

        List<RetrievalCandidate> candidates = new ArrayList<>(merged.values());
        applySimpleFusion(candidates);

        return candidates.stream()
                .sorted(Comparator.comparing(
                        RetrievalCandidate::getFusedScore,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    private void applySimpleFusion(List<RetrievalCandidate> candidates) {
        double maxKeyword = candidates.stream()
                .map(RetrievalCandidate::getKeywordScore)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(1.0);

        for (RetrievalCandidate c : candidates) {
            double vector = c.getVectorScore() != null ? c.getVectorScore() : 0.0;
            double keyword = c.getKeywordScore() != null ? (c.getKeywordScore() / maxKeyword) : 0.0;

            // simple weighted fusion
            double fused = (0.65 * vector) + (0.35 * keyword);
            c.setFusedScore(fused);
        }
    }

    private Double getNumericScore(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }
}