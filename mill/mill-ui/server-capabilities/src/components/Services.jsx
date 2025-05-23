import React from 'react';

function Services({ data }) {
  if (!data) return null;
  return (
    <div className="mb-4">
      <h5>Services</h5>
      <ul className="list-group">
        {data.map((service, idx) => (
          <li className="list-group-item" key={idx}>
            <strong>Stereotype:</strong> {service.stereotype}
            {service.port && (
              <>
                <br />
                <strong>Port:</strong> {service.port}
              </>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default Services;
