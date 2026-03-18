package com.gourav.docanalysis.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AskRequest {
    @NotBlank
    private String sessionId;

    @NotBlank
    private String question;

    private String documentId;
}