package com.gourav.docanalysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageChunk {
    private int pageNumber;
    private int chunkIndex;
    private String text;
}