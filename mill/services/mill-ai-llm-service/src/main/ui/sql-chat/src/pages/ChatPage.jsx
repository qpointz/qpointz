import React, { useEffect, useState } from 'react';
import MessageTabs from '../components/MessageTabs';
import QueryInput from '../components/QueryInput';
import ChatHistory from '../components/ChatHistory';
import Spinner from '../components/Spinner';
import { sendChatQuery } from '../api/chat';
import ReactMarkdown from 'react-markdown';
import MermaidViewer from '../components/MermaidViewer';

export default function ChatPage() {
  const [sessions, setSessions] = useState(() => {
    const saved = localStorage.getItem('chatSessions');
    return saved ? JSON.parse(saved) : [];
  });
  const [currentId, setCurrentId] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (sessions.length === 0) {
      const newId = Date.now().toString();
      const newSession = { id: newId, messages: [], title: 'New Chat' };
      setSessions([newSession]);
      setCurrentId(newId);
    } else if (!currentId) {
      setCurrentId(sessions[0].id);
    }
  }, [sessions, currentId]);

  const currentSession = sessions.find(s => s.id === currentId);

  const handleSend = async (text) => {
    const userMsg = { sender: 'user', text };
    const newMessages = [...currentSession.messages, userMsg];
    updateSession(currentId, { ...currentSession, messages: newMessages });
    setLoading(true);

    try {
      const response = await sendChatQuery(text);
      const botMsg = response.error
        ? { sender: 'bot', error: response.error }
        : { sender: 'bot', response };
      updateSession(currentId, {
        ...currentSession,
        messages: [...newMessages, botMsg]
      });
    } catch (err) {
      const botMsg = { sender: 'bot', error: err.message };
      updateSession(currentId, {
        ...currentSession,
        messages: [...newMessages, botMsg]
      });
    } finally {
      setLoading(false);
    }
  };

  const updateSession = (id, updated) => {
    const newSessions = sessions.map(s => s.id === id ? updated : s);
    setSessions(newSessions);
    localStorage.setItem('chatSessions', JSON.stringify(newSessions));
  };

  const handleDeleteSession = (id) => {
    const filtered = sessions.filter(s => s.id !== id);
    setSessions(filtered);
    localStorage.setItem('chatSessions', JSON.stringify(filtered));
    if (id === currentId && filtered.length > 0) {
      setCurrentId(filtered[0].id);
    } else if (filtered.length === 0) {
      setCurrentId(null);
    }
  };

  const handleNewSession = () => {
    const newId = Date.now().toString();
    const newSession = { id: newId, messages: [], title: 'New Chat' };
    const updated = [newSession, ...sessions];
    setSessions(updated);
    setCurrentId(newId);
    localStorage.setItem('chatSessions', JSON.stringify(updated));
  };

  const renderDescribe = (text) => {
    //return <ReactMarkdown>{text}</ReactMarkdown>;
    
    const blocks = text.split(/```/);
    return blocks.map((block, i) => {
      if (block.trim().startsWith('mermaid')) {
         const code = block.replace(/^mermaid\s*/, '');
         //const code = '```mermaid\n' + code + '\n```'
         return <MermaidViewer key={i} code={code} />;
       } else {
         return <ReactMarkdown key={i}>{block}</ReactMarkdown>;
       }
     });
  };

  return (
    <div className="container-fluid d-flex">
      <div className="col-3 border-end vh-100 p-3 overflow-auto">
        <ChatHistory
          sessions={sessions}
          currentId={currentId}
          onSelect={setCurrentId}
          onDelete={handleDeleteSession}
          onNew={handleNewSession}
        />
      </div>
      <div className="col-9 d-flex flex-column vh-100">
        <div className="flex-grow-1 overflow-auto p-3">
          {currentSession?.messages.map((msg, i) => (
            <div key={i} className="mb-3">
              {msg.sender === 'user' ? (
                <div className="alert alert-primary"><strong>User:</strong> {msg.text}</div>
              ) : msg.error ? (
                <div className="alert alert-danger"><strong>Error:</strong> {msg.error}</div>
              ) : (
                <>
                  {msg.response?.describe && (
                    <div className="alert alert-secondary">
                      {renderDescribe(msg.response.describe)}
                    </div>
                  )}
                  <MessageTabs message={msg} />
                </>
              )}
            </div>
          ))}
          {loading && <Spinner />}
        </div>
        <div className="p-3 border-top">
          <QueryInput onSend={handleSend} />
        </div>
      </div>
    </div>
  );
}
