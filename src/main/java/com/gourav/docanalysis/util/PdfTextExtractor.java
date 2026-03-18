package com.gourav.docanalysis.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PdfTextExtractor {

    public Map<Integer, String> extractTextByPage(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            Map<Integer, String> pageTextMap = new LinkedHashMap<>();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            int totalPages = document.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(document);

                if (text != null && !text.isBlank()) {
                    pageTextMap.put(page, text.trim());
                }
            }

            return pageTextMap;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }
}