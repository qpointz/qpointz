import {Box, Group, Tabs, Text, Title, Button, Modal} from "@mantine/core";
import {TbChartBar, TbDatabase, TbFileTypeSql, TbMaximize} from "react-icons/tb";
import { format } from 'sql-formatter';
import { useState } from 'react';
import ChartView from "../../data/ChartView.tsx";
import DataContainer from "../../data/DataContainer.tsx";
import {CodeHighlight} from "@mantine/code-highlight";

// Reusable component for tabbed content
function TabbedContent({ 
    message, 
    container, 
    chart, 
    hasChart, 
    isModal = false 
}: {
    message: any;
    container: any;
    chart: any;
    hasChart: boolean;
    isModal?: boolean;
}) {
    const tabPanelHeight = isModal ? "calc(100% - 50px)" : undefined;
    const codeHighlightStyle = isModal ? { height: '100%', overflow: 'auto' } : undefined;

    return (
        <Tabs defaultValue={hasChart ? "chart" : "data"} h={isModal ? "100%" : undefined}>
            <Tabs.List>
                { hasChart && (
                <Tabs.Tab value="chart" leftSection={<TbChartBar size={12} />}>
                    Chart
                </Tabs.Tab> )}

                <Tabs.Tab value="data" leftSection={<TbDatabase size={12} />}>
                    Data
                </Tabs.Tab>
                <Tabs.Tab value="sql" leftSection={<TbFileTypeSql size={12} />}>
                    SQL
                </Tabs.Tab>
            </Tabs.List>

            { hasChart && (
            <Tabs.Panel value="chart" h={tabPanelHeight}>
                <ChartView chart={chart} container={container} />
            </Tabs.Panel> )}

            <Tabs.Panel value="data" h={tabPanelHeight}>
                <DataContainer data={message.content.data?.container}/>
            </Tabs.Panel>

            <Tabs.Panel value="sql" h={tabPanelHeight}>
                <CodeHighlight 
                    code={format(message.content?.sql ?? "")} 
                    language="sql"
                    style={codeHighlightStyle}
                    expandCodeLabel=""
                />
            </Tabs.Panel>
        </Tabs>
    );
}


export default function GetDataIntent(data: any) {
    const { message } = data;
    const container = message?.content?.data?.container || {};
    const chart = message?.content?.chart || {};
    const hasChart = chart?.type && chart?.type !== 'none';
    const [modalOpened, setModalOpened] = useState(false);
    return (
        <>
            <Box bg="white" p={20} m={10} key={message.id} maw={"95%"} style={{borderRadius: 10}}>
                <Group justify="space-between">
                    <Group>
                        { hasChart && ( <><TbChartBar/><Title order={4}>Chart</Title></> )}
                        { !hasChart && ( <><TbDatabase/><Title order={4}>Query</Title></> )}
                    </Group>
                    <Button 
                        variant="subtle" 
                        size="sm" 
                        leftSection={<TbMaximize size={16} />}
                        onClick={() => setModalOpened(true)}
                    >
                        Expand
                    </Button>
                </Group>
                <Text fs="italic">{message.content.explanation}</Text>
                <TabbedContent 
                    message={message}
                    container={container}
                    chart={chart}
                    hasChart={hasChart}
                    isModal={false}
                />
            </Box>

            <Modal
                opened={modalOpened}
                onClose={() => setModalOpened(false)}
                title={
                    <Group>
                        { hasChart && ( <><TbChartBar/><Title order={3}>Chart</Title></> )}
                        { !hasChart && ( <><TbDatabase/><Title order={3}>Query</Title></> )}
                    </Group>
                }
                size="100%"
                styles={{
                    content: { height: '100vh' },
                    body: { height: 'calc(100vh - 60px)', padding: 0 }
                }}
            >
            <Box p={20} h="100%">
                <Text fs="italic" mb="md">{message.content.explanation}</Text>
                <TabbedContent 
                    message={message}
                    container={container}
                    chart={chart}
                    hasChart={hasChart}
                    isModal={true}
                />
            </Box>
            </Modal>
        </>
    );
}