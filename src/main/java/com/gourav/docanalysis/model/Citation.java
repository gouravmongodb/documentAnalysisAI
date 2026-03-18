package com.gourav.docanalysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Citation {
    private String fileName;
    private String documentId;
    private Integer pageNumber;
    private String label;
}