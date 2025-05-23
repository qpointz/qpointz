import React from 'react';

function UnknownCapability({ name, data }) {
  return (
    <div className="mb-4">
      <h5>{name} (Unknown Capability)</h5>
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </div>
  );
}

export default UnknownCapability;
