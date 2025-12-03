import '@mantine/core/styles.css';
import {AppShell} from '@mantine/core';
import {Navigate, Route, Routes, useParams} from "react-router";
import NotFound from "./component/NotFound";
import ChatLayout from "./component/chat/ChatLayout.tsx";
import MetadataLayout from "./component/data-model/MetadataLayout.tsx";
import ConceptsLayout from "./component/concepts/ConceptsLayout.tsx";

function ExploreRedirect() {
    const params = useParams<{ schema?: string; table?: string; attribute?: string }>();
    let newPath = "/data-model";
    if (params.schema) {
        newPath += `/${params.schema}`;
        if (params.table) {
            newPath += `/${params.table}`;
            if (params.attribute) {
                newPath += `/${params.attribute}`;
            }
        }
    }
    return <Navigate to={newPath} replace />;
}

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
                  <Route path="/data-model/:schema?/:table?/:attribute?" element={<MetadataLayout />} />
                  <Route path="/concepts/:conceptId?" element={<ConceptsLayout />} />
                  <Route path="/explore" element={<Navigate to="/data-model" replace />} />
                  <Route path="/explore/data/:schema?/:table?/:attribute?" element={<ExploreRedirect />} />
                  <Route path="/explore/:schema/:table/:attribute?" element={<ExploreRedirect />} />
                  <Route path="/chat/*" element={<ChatLayout  />} />
                  <Route path="/stats/*" element={<NotFound />} />
                  <Route index element={<Navigate to="/chat" replace/>} />
              </Routes>
          </AppShell.Main>
          
        </AppShell>
  )
}

export default App
