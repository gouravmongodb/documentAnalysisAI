package com.gourav.docanalysis.model;

import lombok.*;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private ObjectId id;
    private String sessionId;
    private String question;
    private String answer;
    private List<String> retrievedChunkIds;
    private Instant createdAt;
}