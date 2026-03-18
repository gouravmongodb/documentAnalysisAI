import React from "react";

const formatAnswer = (text) => {
    if (!text) return "";

    return text.split("\n").map((line, i) => {
        const formatted = line.replace(
            /\[(.*?)\]/g,
            '<span class="citation">[$1]</span>'
        );

        return (
            <p
                key={i}
                dangerouslySetInnerHTML={{ __html: formatted }}
            />
        );
    });
};

const Answer = ({ answer }) => {
    if (!answer) return null;

    return (
        <div className="card">
            <h3>Answer</h3>
            <div className="answer-text">{formatAnswer(answer)}</div>
        </div>
    );
};

export default Answer;