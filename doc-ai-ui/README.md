# AI Document Analysis - UI

This is the React-based frontend for the AI-Powered Document Analysis Tool. It provides a simple and intuitive interface to upload PDF documents and interact with them using natural language.

## Features

- **PDF Upload**: Upload documents to the backend for analysis by providing a unique Document ID.
- **Conversational Q&A**: Ask questions about your uploaded documents and get AI-generated responses.
- **Citations & Sources**: View the exact parts of the document that were used to generate the answer, complete with citations.
- **Session Management**: Automatically manages session IDs to maintain context during a conversation.

## Getting Started

### Prerequisites

- [Node.js](https://nodejs.org/) (v18 or higher recommended)
- [npm](https://www.npmjs.com/)
- The Backend service should be running at `http://localhost:8080`.

### Installation

1. Navigate to the UI directory:
   ```bash
   cd doc-ai-ui
   ```

2. Install the dependencies:
   ```bash
   npm install
   ```

### Running the App

In the project directory, you can run:

#### `npm start`

Runs the app in development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

The page will reload if you make edits.\
You will also see any lint errors in the console.

#### `npm run build`

Builds the app for production to the `build` folder.\
It correctly bundles React in production mode and optimizes the build for the best performance.

## Tech Stack

- **React 19**
- **Axios** (for API communication)
- **CSS3** (custom styles for a clean UI)

## API Configuration

The UI communicates with the backend at `http://localhost:8080/api`. This can be updated in `src/api.js` if your backend is running on a different port or host.
