import React, { useEffect, useState } from 'react';
import Services from './components/Services';
import Schemas from './components/Schemas';
import UnknownCapability from './components/UnknownCapability';

function App() {
  const [capabilities, setCapabilities] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/.well-known/mill')
      .then(res => res.json())
      .then(data => {
        setCapabilities(data);
        setLoading(false);
      })
      .catch(err => {
        setCapabilities({ error: err.message });
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div className="container mt-5">Loading...</div>;
  }

  if (!capabilities) {
    return <div className="container mt-5 text-danger">Loading error</div>;
  }

  const knownCapabilities = {
    services: <Services data={capabilities.services} />,
    schemas: <Schemas data={capabilities.schemas} />,
    security: (
      <div className="mb-4">
        <h5>Security</h5>
        <pre>{JSON.stringify(capabilities.security, null, 2)}</pre>
      </div>
    ),
  };

  const allKeys = Object.keys(capabilities);

  return (
    <div className="container mt-5">
      <h1 className="mb-4">Server Capabilities</h1>
      {allKeys.map(key =>
        knownCapabilities[key] ? (
          <div key={key}>{knownCapabilities[key]}</div>
        ) : (
          <UnknownCapability key={key} name={key} data={capabilities[key]} />
        )
      )}
    </div>
  );
}

export default App;
