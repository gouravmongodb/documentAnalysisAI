package com.gourav.docanalysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContextSource {
    private String chunkText;
    private String fileName;
    private String documentId;
    private Integer pageNumber;
    private Integer chunkIndex;
    private Double score;
}