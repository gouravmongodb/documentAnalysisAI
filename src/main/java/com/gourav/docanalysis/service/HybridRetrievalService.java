package com.gourav.docanalysis.service;

import com.gourav.docanalysis.model.RetrievalCandidate;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
Reciprocal Rank Fusion (RRF) formula:
        For each candidate:
            RRF score = Σ 1 / (k + rank)
        Where:
            rank is the position in each result list
            k is usually a constant like 60
        If a chunk appears in both:
            vector rank 1
            keyword rank 3
        then:
            1 / (60 + 1) + 1 / (60 + 3)
        Higher total means better fused relevance.
*/


@Service
public class HybridRetrievalService {

    private final AtlasVectorSearchService atlasVectorSearchService;
    private final AtlasKeywordSearchService atlasKeywordSearchService;

    private final int rrfK;

    public HybridRetrievalService(AtlasVectorSearchService atlasVectorSearchService,
                                  AtlasKeywordSearchService atlasKeywordSearchService,
                                  @Value("${app.retrieval.rrf-k:60}") int rrfK) {
        this.atlasVectorSearchService = atlasVectorSearchService;
        this.atlasKeywordSearchService = atlasKeywordSearchService;
        this.rrfK = rrfK;
    }

    public List<RetrievalCandidate> retrieve(List<Double> queryVector,
                                             String query,
                                             String documentId) {

        List<Document> vectorResults = atlasVectorSearchService.search(queryVector, documentId);
        List<Document> keywordResults = atlasKeywordSearchService.search(query, documentId);

        Map<String, RetrievalCandidate> merged = new LinkedHashMap<>();

        addVectorResults(merged, vectorResults);
        addKeywordResults(merged, keywordResults);
        applyRrfFusion(merged);

        List<RetrievalCandidate> candidates = new ArrayList<>(merged.values());

        candidates.sort(Comparator.comparing(
                RetrievalCandidate::getFusedScore,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return candidates;
    }

    private void addVectorResults(Map<String, RetrievalCandidate> merged, List<Document> vectorResults) {
        for (int i = 0; i < vectorResults.size(); i++) {
            Document d = vectorResults.get(i);
            String chunkId = getChunkId(d);

            RetrievalCandidate candidate = merged.get(chunkId);
            if (candidate == null) {
                candidate = new RetrievalCandidate(
                        chunkId,
                        d.getString("chunkText"),
                        d.getString("fileName"),
                        d.getString("documentId"),
                        d.getInteger("pageNumber"),
                        d.getInteger("chunkIndex"),
                        getNumericScore(d.get("score")),
                        null,
                        i + 1,
                        null,
                        null,
                        null,
                        "vector"
                );
                merged.put(chunkId, candidate);
            } else {
                candidate.setVectorScore(getNumericScore(d.get("score")));
                candidate.setVectorRank(i + 1);
                candidate.setRetrievalSource("hybrid");
            }
        }
    }

    private void addKeywordResults(Map<String, RetrievalCandidate> merged, List<Document> keywordResults) {
        for (int i = 0; i < keywordResults.size(); i++) {
            Document d = keywordResults.get(i);
            String chunkId = getChunkId(d);

            RetrievalCandidate candidate = merged.get(chunkId);
            if (candidate == null) {
                candidate = new RetrievalCandidate(
                        chunkId,
                        d.getString("chunkText"),
                        d.getString("fileName"),
                        d.getString("documentId"),
                        d.getInteger("pageNumber"),
                        d.getInteger("chunkIndex"),
                        null,
                        getNumericScore(d.get("score")),
                        null,
                        i + 1,
                        null,
                        null,
                        "keyword"
                );
                merged.put(chunkId, candidate);
            } else {
                candidate.setKeywordScore(getNumericScore(d.get("score")));
                candidate.setKeywordRank(i + 1);
                candidate.setRetrievalSource("hybrid");
            }
        }
    }

    private void applyRrfFusion(Map<String, RetrievalCandidate> merged) {
        for (RetrievalCandidate candidate : merged.values()) {
            double rrfScore = 0.0;

            if (candidate.getVectorRank() != null) {
                rrfScore += 1.0 / (rrfK + candidate.getVectorRank());
            }

            if (candidate.getKeywordRank() != null) {
                rrfScore += 1.0 / (rrfK + candidate.getKeywordRank());
            }

            candidate.setFusedScore(rrfScore);
        }
    }

    private String getChunkId(Document d) {
        if (d.getObjectId("_id") == null) {
            throw new IllegalStateException("Missing _id in retrieval result");
        }
        return d.getObjectId("_id").toHexString();
    }

    private Double getNumericScore(Object value) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }
}