package com.gourav.docanalysis.service;

import com.gourav.docanalysis.model.PageChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChunkingService {

    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) break;
            start = Math.max(0, end - overlap);
        }
        return chunks;
    }

    public List<PageChunk> chunkTextByPage(Map<Integer, String> pageTextMap, int chunkSize, int overlap) {
        List<PageChunk> pageChunks = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : pageTextMap.entrySet()) {
            Integer pageNumber = entry.getKey();
            String pageText = entry.getValue();

            List<String> chunks = chunkText(pageText, chunkSize, overlap);

            for (int i = 0; i < chunks.size(); i++) {
                pageChunks.add(new PageChunk(pageNumber, i, chunks.get(i)));
            }
        }

        return pageChunks;
    }
}