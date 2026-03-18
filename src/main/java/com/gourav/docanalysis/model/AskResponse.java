package com.gourav.docanalysis.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AskResponse {
    private String answer;
    private List<Citation> citations;
    private List<ContextSource> sources;
}