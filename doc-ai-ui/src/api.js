import axios from "axios";

const API_BASE = "http://localhost:8080/api";

export const uploadPdf = async (documentId, file) => {
    const formData = new FormData();
    formData.append("documentId", documentId);
    formData.append("file", file);

    return axios.post(`${API_BASE}/ingest/pdf`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
};

export const askQuestion = async (payload) => {
    return axios.post(`${API_BASE}/ask`, payload);
};