import React from 'react';

export default function ChatHistory({ sessions, currentId, onSelect, onDelete, onNew }) {
  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h5>Chats</h5>
        <button className="btn btn-sm btn-outline-primary" onClick={onNew}>New</button>
      </div>
      <ul className="list-group">
        {sessions.map(s => (
          <li key={s.id} className={`list-group-item d-flex justify-content-between align-items-center ${s.id === currentId ? 'active' : ''}`}>
            <span onClick={() => onSelect(s.id)} style={{ cursor: 'pointer' }}>{s.title || 'Untitled'}</span>
            <button className="btn btn-sm btn-danger" onClick={() => onDelete(s.id)}>✕</button>
          </li>
        ))}
      </ul>
    </div>
  );
}
