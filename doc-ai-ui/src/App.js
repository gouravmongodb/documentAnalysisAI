import React, { useState, useEffect } from "react";
import Upload from "./components/Upload";
import Chat from "./components/Chat";
import Answer from "./components/Answer";
import Citations from "./components/Citations";
import Sources from "./components/Sources";
import "./styles.css";

function App() {
  const [documentId, setDocumentId] = useState(null);
  const [response, setResponse] = useState(null);
  const [sessionId, setSessionId] = useState("");

  useEffect(() => {
    setSessionId(`session_${Math.random().toString(36).substr(2, 9)}`);
  }, []);

  return (
      <div className="container">
        <h1>📄 AI Document Analysis</h1>

        <Upload setDocumentId={setDocumentId} />

        {documentId && (
            <Chat documentId={documentId} setResponse={setResponse} sessionId={sessionId} />
        )}

        {response && (
            <>
              <Answer answer={response.answer} />
              <Citations citations={response.citations} />
              <Sources sources={response.sources} />
            </>
        )}
      </div>
  );
}

export default App;