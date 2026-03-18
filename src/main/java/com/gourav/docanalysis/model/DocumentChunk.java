package com.gourav.docanalysis.model;

import lombok.*;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {
    private ObjectId id;
    private String documentId;
    private String fileName;
    private Integer chunkIndex;
    private String chunkText;
    private List<Double> embedding;
    private Map<String, Object> metadata;
    private Instant createdAt;
}