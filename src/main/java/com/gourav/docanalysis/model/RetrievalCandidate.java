package com.gourav.docanalysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RetrievalCandidate {
    private String chunkId;
    private String chunkText;
    private String fileName;
    private String documentId;
    private Integer pageNumber;
    private Integer chunkIndex;

    private Double vectorScore;
    private Double keywordScore;

    private Integer vectorRank;
    private Integer keywordRank;

    private Double fusedScore;
    private Double rerankScore;

    private String retrievalSource; // vector, keyword, hybrid
}