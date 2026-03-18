# AI-Powered Document Analysis Tool

An AI-driven Spring Boot application that enables semantic search and conversational Q&A over unstructured documents (PDFs and Text). It leverages MongoDB Atlas Vector Search for storing and retrieving high-dimensional document embeddings, Voyage AI for high-quality text embedding and reranking, and OpenAI (GPT models) for generating intelligent, context-aware responses with citations.

## Features

- **Semantic Document Ingestion**: Process and chunk text and PDF documents. It uses page-aware chunking for PDFs to maintain context.
- **Hybrid Search**: Combines MongoDB Atlas Vector Search and Keyword Search for robust retrieval.
- **Voyage AI Reranking**: Utilizes Voyage AI's reranking model (`rerank-2.5-lite`) to prioritize the most relevant document chunks.
- **Contextual Q&A**: Generate answers to user questions using retrieved document context and conversation history (memory).
- **Citations & Sources**: Automatically generate citations for sources used to build answers, with deduplication of source chunks.
- **Persistent Conversation History**: Maintain session-based context across multiple user queries using MongoDB.

## Backend Tech Stack

- **Java 21**
- **Spring Boot 3.3.2**
- **MongoDB Atlas** (Vector Search & Keyword Search)
- **Voyage AI** (Embeddings: `voyage-3-large`, Reranking: `rerank-2.5-lite`)
- **OpenAI** (LLM: `gpt-4o-mini` or `gpt-4-turbo`)
- **Apache PDFBox** (PDF Parsing)
- **Maven** (Build Tool)

## Prerequisites

- JDK 21 or higher
- Maven 3.6+
- A MongoDB Atlas cluster with Vector Search and Search Index enabled.
- Voyage AI API Key.
- OpenAI API Key.

## Environment Variables

To run this application, you need to configure the following environment variables:

| Variable | Description                |
| :--- |:---------------------------|
| `MONGO_USER` | Your MongoDB Atlas username |
| `MONGO_PASS` | Your MongoDB Atlas password |
| `MONGO_HOST` | Your MongoDB hostname      |
| `VOYAGE_API_KEY` | API Key for Voyage AI      |
| `OPENAI_API_KEY` | API Key for OpenAI         |

### MongoDB Atlas Setup
You can create a new cluster and enable Vector Search in the MongoDB Atlas UI.
Create text and vector search index for the collection - `document_ai_demo.document_chunks`
by following the definition `src/main/resources/search_index.txt` and `src/main/resources/vector_search_index.txt`

### IntelliJ IDEA Setup
A shared Sample Run Configuration is provided in `.run/SampleDocumentAnalysisApplication.run.xml`. You can edit this configuration in IntelliJ to set your actual secrets with filename `.run/DocumentAnalysisApplication.run.xml`

## API Endpoints

### 1. Ingest Text
**POST** `/api/ingest`
- **Body**: `IngestRequest` (JSON)
- **Description**: Ingests raw text content, chunks it (size 1000, overlap 150), and stores embeddings in MongoDB.

### 2. Ingest PDF
**POST** `/api/ingest/pdf`
- **Content-Type**: `multipart/form-data`
- **Parameters**: `documentId` (String), `file` (MultipartFile)
- **Description**: Extracts text from a PDF file using page-aware chunking, and stores embeddings in MongoDB.

### 3. Ask Question
**POST** `/api/ask`
- **Body**: `AskRequest` (JSON)
- **Description**: 
    1. Embeds the question using Voyage AI.
    2. Performs **Hybrid Search** (Vector + Keyword) on MongoDB.
    3. **Reranks** the top candidates using Voyage AI Reranker.
    4. Retrieves **Conversation History** (last 4 turns) for the session.
    5. Generates an answer using OpenAI with the retrieved context and citations.

## Getting Started

### Backend

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd documentAnalysisAI
   ```

2. **Configure Environment Variables**:
   Set `MONGODB_URI`, `VOYAGE_API_KEY`, and `OPENAI_API_KEY`.

3. **Build and Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   The server will start at `http://localhost:8080`.

### Frontend (UI)

1. **Navigate to the UI directory**:
   ```bash
   cd doc-ai-ui
   ```

2. **Install and Run**:
   ```bash
   npm install
   npm start
   ```
   The UI will be available at `http://localhost:3000`.

## License
This project is for demonstration purposes.