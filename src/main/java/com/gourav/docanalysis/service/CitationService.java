package com.gourav.docanalysis.service;

import com.gourav.docanalysis.model.Citation;
import com.gourav.docanalysis.model.ContextSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CitationService {

    public List<ContextSource> deduplicateSources(List<ContextSource> sources) {
        Map<String, ContextSource> unique = new LinkedHashMap<>();

        List<ContextSource> sorted = sources.stream()
                .sorted(Comparator.comparing(
                        ContextSource::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        for (ContextSource source : sorted) {
            String key = buildDedupKey(source);

            if (!unique.containsKey(key)) {
                unique.put(key, source);
            }
        }

        return new ArrayList<>(unique.values());
    }

    public List<Citation> buildCitations(List<ContextSource> sources) {
        Map<String, Citation> uniqueCitations = new LinkedHashMap<>();

        List<ContextSource> sorted = sources.stream()
                .sorted(Comparator
                        .comparing(ContextSource::getFileName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(ContextSource::getPageNumber, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        for (ContextSource source : sorted) {
            String key = citationKey(source);

            if (!uniqueCitations.containsKey(key)) {
                uniqueCitations.put(key, new Citation(
                        source.getFileName(),
                        source.getDocumentId(),
                        source.getPageNumber(),
                        formatCitationLabel(source)
                ));
            }
        }

        return new ArrayList<>(uniqueCitations.values());
    }

    public String buildCitationSummary(List<Citation> citations) {
        if (citations == null || citations.isEmpty()) {
            return "";
        }

        return citations.stream()
                .map(Citation::getLabel)
                .distinct()
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }

    private String buildDedupKey(ContextSource source) {
        String fileName = source.getFileName() == null ? "" : source.getFileName();
        String documentId = source.getDocumentId() == null ? "" : source.getDocumentId();
        Integer pageNumber = source.getPageNumber() == null ? -1 : source.getPageNumber();

        return documentId + "|" + fileName + "|" + pageNumber;
    }

    private String citationKey(ContextSource source) {
        String fileName = source.getFileName() == null ? "" : source.getFileName();
        String documentId = source.getDocumentId() == null ? "" : source.getDocumentId();
        Integer pageNumber = source.getPageNumber() == null ? -1 : source.getPageNumber();

        return documentId + "|" + fileName + "|" + pageNumber;
    }

    private String formatCitationLabel(ContextSource source) {
        String file = source.getFileName() != null ? source.getFileName() : "unknown-file";

        if (source.getPageNumber() != null) {
            return file + ", page " + source.getPageNumber();
        }

        return file;
    }
}