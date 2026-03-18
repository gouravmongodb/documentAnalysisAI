package com.gourav.docanalysis.controller;

import com.gourav.docanalysis.model.AskRequest;
import com.gourav.docanalysis.model.AskResponse;
import com.gourav.docanalysis.model.Citation;
import com.gourav.docanalysis.model.ContextSource;
import com.gourav.docanalysis.model.IngestRequest;
import com.gourav.docanalysis.service.AtlasVectorSearchService;
import com.gourav.docanalysis.service.CitationService;
import com.gourav.docanalysis.service.ConversationService;
import com.gourav.docanalysis.service.IngestionService;
import com.gourav.docanalysis.service.OpenAiService;
import com.gourav.docanalysis.service.PdfIngestionService;
import com.gourav.docanalysis.service.VoyageAiService;
import jakarta.validation.Valid;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final IngestionService ingestionService;
    private final PdfIngestionService pdfIngestionService;
    private final VoyageAiService voyageAiService;
    private final AtlasVectorSearchService atlasVectorSearchService;
    private final OpenAiService openAiService;
    private final ConversationService conversationService;
    private final CitationService citationService;

    public AnalysisController(IngestionService ingestionService,
                              PdfIngestionService pdfIngestionService,
                              VoyageAiService voyageAiService,
                              AtlasVectorSearchService atlasVectorSearchService,
                              OpenAiService openAiService,
                              ConversationService conversationService,
                              CitationService citationService) {
        this.ingestionService = ingestionService;
        this.pdfIngestionService = pdfIngestionService;
        this.voyageAiService = voyageAiService;
        this.atlasVectorSearchService = atlasVectorSearchService;
        this.openAiService = openAiService;
        this.conversationService = conversationService;
        this.citationService = citationService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@Valid @RequestBody IngestRequest request) {
        ingestionService.ingest(request.getDocumentId(), request.getFileName(), request.getText());
        return ResponseEntity.ok("Document ingested successfully");
    }

    @PostMapping(value = "/ingest/pdf", consumes = {"multipart/form-data"})
    public ResponseEntity<String> ingestPdf(@RequestParam("documentId") String documentId,
                                            @RequestParam("file") MultipartFile file) {
        pdfIngestionService.ingestPdf(documentId, file);
        return ResponseEntity.ok("PDF ingested successfully");
    }

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        List<Double> queryVector = voyageAiService.embedText(request.getQuestion());
        List<Document> results = atlasVectorSearchService.search(queryVector, request.getDocumentId());

        List<String> chunkIds = results.stream()
                .map(d -> d.getObjectId("_id") != null ? d.getObjectId("_id").toHexString() : "")
                .toList();

        List<ContextSource> rawSources = results.stream()
                .map(d -> new ContextSource(
                        d.getString("chunkText"),
                        d.getString("fileName"),
                        d.getString("documentId"),
                        d.getInteger("pageNumber"),
                        d.getInteger("chunkIndex"),
                        d.get("score") instanceof Number ? ((Number) d.get("score")).doubleValue() : null
                ))
                .toList();

        List<ContextSource> deduplicatedSources = citationService.deduplicateSources(rawSources);
        List<Citation> citations = citationService.buildCitations(deduplicatedSources);
        String citationSummary = citationService.buildCitationSummary(citations);

        List<String> history = conversationService.getRecentHistory(request.getSessionId(), 4);

        String answer = openAiService.answerQuestion(
                request.getQuestion(),
                deduplicatedSources,
                history,
                citationSummary
        );

        conversationService.save(
                request.getSessionId(),
                request.getQuestion(),
                answer,
                chunkIds,
                citations.stream().map(Citation::getLabel).toList()
        );

        return ResponseEntity.ok(new AskResponse(answer, citations, deduplicatedSources));
    }
}