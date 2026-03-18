import React, { useState } from "react";
import { askQuestion } from "../api";

const Chat = ({ documentId, setResponse, sessionId }) => {
    const [question, setQuestion] = useState("");
    const [loading, setLoading] = useState(false);

    const handleAsk = async () => {
        if (!question) return;
        setLoading(true);
        const res = await askQuestion({ question, documentId, sessionId });
        setResponse(res.data);
        setLoading(false);
    };

    return (
        <div className="card">
            <h3>Ask Question</h3>
            <input
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                placeholder="Ask about your document..."
            />
            <button onClick={handleAsk}>
                {loading ? "Thinking..." : "Ask"}
            </button>
        </div>
    );
};

export default Chat;