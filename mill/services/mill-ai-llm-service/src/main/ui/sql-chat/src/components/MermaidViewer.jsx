import React, { useEffect, useRef } from 'react';
import mermaid from 'mermaid';

mermaid.initialize({
  startOnLoad: false,
  theme: 'base',
  /*themeVariables: {
    fontFamily: 'inherit',
    fontSize: '18px',
    fontWeight: 'normal',
    fontColor: '#000',      // Set dark font color
    textColor: '#000',      // For nodes and labels
    mainTextColor: '#000',  // For main text
    nodeTextColor: '#000',  // For nodes
    classText: '#000',
    er : {
      fontSize: '20px',
      fontColor: '#000'
    }      // For class diagrams
  }*/
});


export default function MermaidViewer({ code }) {
  const containerRef = useRef(null);

  useEffect(() => {
    let mounted = true;
    if (!containerRef.current || !code) return;

    // Generate a unique id for each diagram
    const id = `mmd-${Math.random().toString(36).substr(2, 9)}`;
    containerRef.current.innerHTML = '';

    // Delay rendering to ensure DOM is ready
    setTimeout(() => {
      mermaid
        .render(id, code)
        .then(({ svg }) => {
          if (mounted && containerRef.current) {
            containerRef.current.innerHTML = svg;
          }
        })
        .catch((err) => {
          if (mounted && containerRef.current) {
            containerRef.current.innerHTML =
              `<pre style="color:red;">Invalid mermaid diagram\n\n${err?.message || ''}</pre>`;
          }
        });
    }, 0);

    return () => {
      mounted = false;
    };
  }, [code]);

  return (
    <div
      className="border rounded p-2 bg-light mb-2"
      ref={containerRef}
      style={{ minHeight: 40 }}
    />
  );
}
