package com.gourav.docanalysis.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IngestRequest {
    @NotBlank
    private String documentId;

    @NotBlank
    private String fileName;

    @NotBlank
    private String text;
}