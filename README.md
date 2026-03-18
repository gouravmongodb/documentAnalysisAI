# AI-Powered Document Analysis Tool

An AI-driven Spring Boot application that enables semantic search and conversational Q&A over unstructured documents (PDFs and Text). It leverages MongoDB Atlas Vector Search for storing and retrieving high-dimensional document embeddings, Voyage AI for high-quality text embedding, and OpenAI (GPT models) for generating intelligent, context-aware responses with citations.

## Features

- **Semantic Document Ingestion**: Process and chunk text and PDF documents for efficient retrieval.
- **Vector Search**: Utilize MongoDB Atlas Vector Search to find semantically relevant document context.
- **Contextual Q&A**: Generate answers to user questions using retrieved document context and conversation history.
- **Citations**: Automatically generate citations for sources used to build answers.
- **Persistent Conversation History**: Maintain session-based context across multiple user queries.

## Tech Stack

- **Java 21**
- **Spring Boot 3.3.2**
- **MongoDB Atlas** (Vector Search & Operational Data)
- **Voyage AI** (Embeddings: `voyage-3-large`)
- **OpenAI** (LLM: `gpt-5-mini`)
- **Apache PDFBox** (PDF Parsing)
- **Maven** (Build Tool)

## Prerequisites

- JDK 21 or higher
- Maven 3.6+
- A MongoDB Atlas cluster with Vector Search enabled.
- Voyage AI API Key.
- OpenAI API Key.

## Environment Variables

To run this application, you need to configure the following environment variables:

| Variable | Description |
| :--- | :--- |
| `MONGODB_URI` | Your MongoDB Atlas connection string |
| `VOYAGE_API_KEY` | API Key for Voyage AI |
| `OPENAI_API_KEY` | API Key for OpenAI |

### IntelliJ IDEA Setup
A shared Sample Run Configuration is provided in `.run/SampleDocumentAnalysisApplication.run.xml`. You can edit this configuration in IntelliJ to set your actual secrets with filename `.run/DocumentAnalysisApplication.run.xml`

## API Endpoints

### 1. Ingest Text
**POST** `/api/ingest`
- **Body**: `IngestRequest` (JSON)
- **Description**: Ingests raw text content for a given document ID.

### 2. Ingest PDF
**POST** `/api/ingest/pdf`
- **Content-Type**: `multipart/form-data`
- **Parameters**: `documentId` (String), `file` (MultipartFile)
- **Description**: Extracts text from a PDF file, chunks it, and stores embeddings in MongoDB.

### 3. Ask Question
**POST** `/api/ask`
- **Body**: `AskRequest` (JSON)
- **Description**: Performs a vector search for the question, retrieves relevant context, and generates an answer using OpenAI.

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