import '@mantine/core/styles.css';
import {AppShell} from '@mantine/core';
import Topbar from "./component/layout/Topbar";
import Navbar from "./component/layout/Navbar";
import Footer from './component/layout/Footer';
import {Navigate, Route, Routes} from "react-router";
import NotFound from "./component/NotFound";
import AssistLayout from "./component/assist/AssistLayout";
function App() {
  return (
      <AppShell
            h="100vh"
            padding={0}
            header={{ height: 50 }}
            footer={{ height: 50 }}
            navbar={{ width: 35, breakpoint:"lg" }}
            styles={{
                main: { flex: 1, display: 'flex', flexDirection: 'column' }
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

                  <Route path="/assist/*" element={<AssistLayout  />} />

                  <Route path="/stats/*" element={<NotFound />} />
                  <Route path="/" element={<Navigate replace to="/assist" />} />
              </Routes>
          </AppShell.Main>

          <AppShell.Footer>
              <Footer/>
          </AppShell.Footer>
        </AppShell>
  )
}

export default App
