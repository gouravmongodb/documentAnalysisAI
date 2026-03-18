import React, { useState } from "react";
import { uploadPdf } from "../api";

const Upload = ({ setDocumentId }) => {
    const [file, setFile] = useState(null);
    const [docId, setDocId] = useState("");

    const handleUpload = async () => {
        if (!file || !docId) return alert("Provide file + documentId");

        await uploadPdf(docId, file);
        alert("Upload successful!");
        setDocumentId(docId);
    };

    return (
        <div className="card">
            <h3>Upload PDF</h3>
            <input
                type="text"
                placeholder="Document ID"
                value={docId}
                onChange={(e) => setDocId(e.target.value)}
            />
            <input type="file" onChange={(e) => setFile(e.target.files[0])} />
            <button onClick={handleUpload}>Upload</button>
        </div>
    );
};

export default Upload;