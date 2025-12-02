import '@mantine/core/styles.css';
import {AppShell} from '@mantine/core';
import {Navigate, Route, Routes} from "react-router";
import NotFound from "./component/NotFound";
import ChatLayout from "./component/chat/ChatLayout.tsx";
import MetadataLayout from "./component/explore/MetadataLayout.tsx";

function App() {
  return (
      <AppShell
            h="100vh"
            padding={0}            
            styles={{
                main : {minWidth: 1024, width: "100%"},                
            }}
        >          

          <AppShell.Main bg="gray.1" h="100%">
              <Routes>
                  <Route path="/overview/*" element={<NotFound />} />
                  <Route path="/data/*" element={<NotFound />} />
                  <Route path="/explore/:schema?/:table?/:attribute?" element={<MetadataLayout />} />
                  <Route path="/chat/*" element={<ChatLayout  />} />
                  <Route path="/stats/*" element={<NotFound />} />
                  <Route index element={<Navigate to="/chat" replace/>} />
              </Routes>
          </AppShell.Main>
          
        </AppShell>
  )
}

export default App
