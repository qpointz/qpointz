import { useState, useRef, useEffect } from "react";

type Message = {
  role: "user" | "copilot";
  content: string;
};

const START_MSGS: Message[] = [
  { role: "copilot", content: "Hello! I'm your Copilot.\nDescribe your query in natural language and I'll generate SQL for you." }
];

export default function CopilotContent() {
  const [messages, setMessages] = useState<Message[]>(START_MSGS);
  const [input, setInput] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  function sendMessage(e: React.FormEvent) {
    e.preventDefault();
    if (!input.trim()) return;
    const userMsg: Message = { role: "user", content: input };
    // Fake Copilot response for demo
    const reply: Message = {
      role: "copilot",
      content: `SELECT * FROM users WHERE ...; // (Generated from: "${input}")`
    };
    setMessages(m => [...m, userMsg, reply]);
    setInput("");
  }

  return (
    <section className="flex-1 flex flex-col h-full bg-gray-50">
      <div className="flex-1 overflow-y-auto px-8 py-6">
        {messages.map((msg, i) => (
          <div
            key={i}
            className={`mb-4 flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
          >
            <div
              className={`rounded-lg px-4 py-2 shadow
                ${msg.role === "user" ? "bg-blue-200 text-blue-900" : "bg-white border text-gray-900"}
              `}
              style={{ maxWidth: "60%" }}
            >
              <span className="whitespace-pre-line">{msg.content}</span>
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      <form className="border-t bg-white flex px-4 py-3" onSubmit={sendMessage}>
        <input
          className="flex-1 rounded border p-2 mr-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200"
          placeholder="Ask in plain English..."
          value={input}
          autoFocus
          onChange={e => setInput(e.target.value)}
        />
        <button
          className="bg-blue-600 text-white px-4 py-2 rounded font-semibold"
          type="submit"
        >
          Send
        </button>
      </form>
    </section>
  );
}