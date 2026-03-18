import React from "react";

const Citations = ({ citations }) => {
    if (!citations || citations.length === 0) return null;

    return (
        <div className="card">
            <h3>Citations</h3>
            <ul>
                {citations.map((c, i) => (
                    <li key={i}>{c.label}</li>
                ))}
            </ul>
        </div>
    );
};

export default Citations;