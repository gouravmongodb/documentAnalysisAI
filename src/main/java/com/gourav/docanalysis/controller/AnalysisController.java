package com.gourav.docanalysis.controller;

import com.gourav.docanalysis.model.AskRequest;
import com.gourav.docanalysis.model.AskResponse;
import com.gourav.docanalysis.model.Citation;
import com.gourav.docanalysis.model.ContextSource;
import com.gourav.docanalysis.model.IngestRequest;
import com.gourav.docanalysis.model.RetrievalCandidate;
import com.gourav.docanalysis.service.AtlasVectorSearchService;
import com.gourav.docanalysis.service.CitationService;
import com.gourav.docanalysis.service.ConversationService;
import com.gourav.docanalysis.service.IngestionService;
import com.gourav.docanalysis.service.OpenAiService;
import com.gourav.docanalysis.service.PdfIngestionService;
import com.gourav.docanalysis.service.VoyageAiService;
import com.gourav.docanalysis.service.HybridRetrievalServiceSimpleWeighted;
import com.gourav.docanalysis.service.VoyageRerankService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class AnalysisController {

    private final IngestionService ingestionService;
    private final PdfIngestionService pdfIngestionService;
    private final VoyageAiService voyageAiService;
    private final AtlasVectorSearchService atlasVectorSearchService;
    private final OpenAiService openAiService;
    private final ConversationService conversationService;
    private final CitationService citationService;
    private final HybridRetrievalServiceSimpleWeighted hybridRetrievalService;
    private final VoyageRerankService voyageRerankService;

    public AnalysisController(IngestionService ingestionService,
                              PdfIngestionService pdfIngestionService,
                              VoyageAiService voyageAiService,
                              AtlasVectorSearchService atlasVectorSearchService,
                              OpenAiService openAiService,
                              ConversationService conversationService,
                              CitationService citationService,
                              HybridRetrievalServiceSimpleWeighted hybridRetrievalService,
                              VoyageRerankService voyageRerankService) {
        this.ingestionService = ingestionService;
        this.pdfIngestionService = pdfIngestionService;
        this.voyageAiService = voyageAiService;
        this.atlasVectorSearchService = atlasVectorSearchService;
        this.openAiService = openAiService;
        this.conversationService = conversationService;
        this.citationService = citationService;
        this.hybridRetrievalService = hybridRetrievalService;
        this.voyageRerankService = voyageRerankService;
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

        List<RetrievalCandidate> hybridCandidates = hybridRetrievalService.retrieve(
                queryVector,
                request.getQuestion(),
                request.getDocumentId()
        );

        int hybridPoolSize = Math.min(12, hybridCandidates.size());
        List<RetrievalCandidate> pooledCandidates = hybridCandidates.subList(0, hybridPoolSize);

        List<RetrievalCandidate> rerankedCandidates = voyageRerankService.rerank(
                request.getQuestion(),
                pooledCandidates,
                Math.min(6, pooledCandidates.size())
        );

        List<ContextSource> rawSources = rerankedCandidates.stream()
                .map(c -> new ContextSource(
                        c.getChunkText(),
                        c.getFileName(),
                        c.getDocumentId(),
                        c.getPageNumber(),
                        c.getChunkIndex(),
                        c.getRerankScore() != null ? c.getRerankScore() : c.getFusedScore(),
                        c.getRetrievalSource(),
                        c.getVectorRank(),
                        c.getKeywordRank(),
                        c.getFusedScore(),
                        c.getRerankScore()
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

        List<String> chunkIds = rerankedCandidates.stream()
                .map(RetrievalCandidate::getChunkId)
                .toList();

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