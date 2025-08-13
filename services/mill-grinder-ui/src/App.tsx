import '@mantine/core/styles.css';
import {AppShell} from '@mantine/core';
import Topbar from "./component/layout/Topbar";
import Navbar from "./component/layout/Navbar";
import Footer from './component/layout/Footer';
import {Navigate, Route, Routes} from "react-router";
import NotFound from "./component/NotFound";
import ChatLayout from "./component/chat/ChatLayout.tsx";

function App() {
  return (
      <AppShell
            h="100vh"
            padding={0}
            header={{ height: 50 }}
            footer={{ height: 50 }}
            navbar={{ width: 50, breakpoint: "sm" }}
            styles={{
                main : {minWidth: 1024, width: "100%"},
                navbar: { maxWidth: 50, minWidth: 50, width: 50 }
            }}
        >
          <AppShell.Header>
              <Topbar/>
          </AppShell.Header>

          <AppShell.Navbar>
              <Navbar/>
          </AppShell.Navbar>

          <AppShell.Main bg="gray.1" h="100%">
              <Routes>
                  <Route path="/overview/*" element={<NotFound />} />
                  <Route path="/data/*" element={<NotFound />} />
                  <Route path="/explore/*" element={<NotFound />} />
                  <Route path="/chat/*" element={<ChatLayout  />} />
                  <Route path="/stats/*" element={<NotFound />} />
                  <Route index element={<Navigate to="/chat" replace/>} />
              </Routes>
          </AppShell.Main>

          <AppShell.Footer>
              <Footer/>
          </AppShell.Footer>
        </AppShell>
  )
}

export default App
