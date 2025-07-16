import {Route, Routes} from "react-router";
import ChatView from "./ChatView.tsx";

export default function ChatLayout() {
    return (
        <Routes>
            <Route path="/:chatid?/*" element={<ChatView/>} />
            <Route path="/" element={<ChatView />} />
        </Routes>
    );
}