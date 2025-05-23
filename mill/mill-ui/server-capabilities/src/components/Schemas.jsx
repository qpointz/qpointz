import React from 'react';

function Schemas({ data }) {
  if (!data) return null;
  return (
    <div className="mb-4">
      <h5>Schemas</h5>
      <ul className="list-group">
        {Object.entries(data).map(([key, schema]) => (
          <li className="list-group-item" key={key}>
            <strong>Name:</strong> {schema.name} <br />
            <strong>Link:</strong>{' '}
            <a href={schema.link} target="_blank" rel="noopener noreferrer">
              {schema.link}
            </a>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Schemas;
