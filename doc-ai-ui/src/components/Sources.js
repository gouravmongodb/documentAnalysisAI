import React from "react";

const Sources = ({ sources }) => {
    if (!sources) return null;

    return (
        <div className="card">
            <h3>Source Context</h3>
            {sources.map((s, i) => (
                <div key={i} className="source">
                    <b>
                        {s.fileName} (Page {s.pageNumber})
                    </b>
                    <p>{s.chunkText}</p>
                    <small>
                        Score: {s.score?.toFixed(3)} | {s.retrievalSource}
                    </small>
                </div>
            ))}
        </div>
    );
};

export default Sources;