package com.gourav.docanalysis.service;

import com.gourav.docanalysis.model.PageChunk;
import com.gourav.docanalysis.util.PdfTextExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class PdfIngestionService {

    private final PdfTextExtractor pdfTextExtractor;
    private final ChunkingService chunkingService;
    private final IngestionService ingestionService;

    public PdfIngestionService(PdfTextExtractor pdfTextExtractor,
                               ChunkingService chunkingService,
                               IngestionService ingestionService) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.chunkingService = chunkingService;
        this.ingestionService = ingestionService;
    }

    public void ingestPdf(String documentId, MultipartFile file) {
        validatePdf(file);

        try {
            Map<Integer, String> pageTextMap = pdfTextExtractor.extractTextByPage(file.getInputStream());

            if (pageTextMap.isEmpty()) {
                throw new IllegalArgumentException("No readable text found in PDF");
            }

            List<PageChunk> pageChunks = chunkingService.chunkTextByPage(pageTextMap, 1000, 150);

            System.out.println("Pages extracted: " + pageTextMap.size());
            System.out.println("Page-aware chunks created: " + pageChunks.size());

            ingestionService.ingestPageAwareChunks(documentId, file.getOriginalFilename(), pageChunks);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest PDF", e);
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }
    }
}