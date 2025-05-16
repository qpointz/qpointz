import React from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { docco } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import { format } from 'sql-formatter';

export default function SqlViewer({ sql }) {
  return <SyntaxHighlighter language="sql" style={docco}>
      {format(sql)}
    </SyntaxHighlighter>
}
