import { Card, Group, Tabs, Text, Button, Modal, Box } from "@mantine/core";
import { TbChartBar, TbDatabase, TbFileTypeSql, TbMaximize } from "react-icons/tb";
import { format } from 'sql-formatter';
import { useState } from 'react';
import ChartView from "../../data/ChartView.tsx";
import DataContainer from "../../data/DataContainer.tsx";
import { ErrorBoundary } from "../../data/ErrorBoundary.tsx";
import { CodeHighlight } from "@mantine/code-highlight";

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
    const tabPanelHeight = isModal ? "100%" : undefined;
    const codeHighlightStyle = isModal ? { height: '100%', overflow: 'auto' } : undefined;
    const [activeTab, setActiveTab] = useState<string>(hasChart ? "chart" : "data");

    return (
        <Tabs value={activeTab} onChange={(val) => {
            setActiveTab(val || (hasChart ? "chart" : "data"));
        }} h={isModal ? "100%" : undefined} style={isModal ? { display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' } : undefined}>
            <Tabs.List>
                {hasChart && (
                    <Tabs.Tab value="chart" leftSection={<TbChartBar size={12} />}>
                        Chart
                    </Tabs.Tab>
                )}
                <Tabs.Tab value="data" leftSection={<TbDatabase size={12} />}>
                    Data
                </Tabs.Tab>
                <Tabs.Tab value="sql" leftSection={<TbFileTypeSql size={12} />}>
                    SQL
                </Tabs.Tab>
            </Tabs.List>

            {hasChart && (
                <Tabs.Panel value="chart" h={tabPanelHeight} style={isModal ? { flex: 1, minHeight: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column' } : undefined}>
                    <ChartView chart={chart} container={container} />
                </Tabs.Panel>
            )}

            {activeTab === "data" && (
                <Tabs.Panel value="data" h={tabPanelHeight} style={isModal ? { flex: 1, minHeight: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column' } : undefined}>
                    <ErrorBoundary>
                        <DataContainer data={message.content.data?.container} fullHeight={isModal} />
                    </ErrorBoundary>
                </Tabs.Panel>
            )}

            {activeTab === "sql" && (
                <Tabs.Panel value="sql" h={tabPanelHeight}>
                    <CodeHighlight 
                        code={(() => {
                            try {
                                return format(message.content?.sql ?? "");
                            } catch (error) {
                                return message.content?.sql ?? "";
                            }
                        })()}
                        language="sql"
                        style={codeHighlightStyle}
                        expandCodeLabel=""
                    />
                </Tabs.Panel>
            )}
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
            <Card key={message.id} w="100%" p="md" mb="xs">
                <Group justify="space-between" mb="xs">
                    <Group gap="xs">
                        {hasChart ? <TbChartBar size={16} /> : <TbDatabase size={16} />}
                        <Text fw={600} size="sm">
                            {hasChart ? "Chart" : "Query"}
                        </Text>
                    </Group>
                    <Button 
                        variant="subtle" 
                        size="xs" 
                        leftSection={<TbMaximize size={14} />}
                        onClick={() => setModalOpened(true)}
                    >
                        Expand
                    </Button>
                </Group>
                <Text size="sm" c="dimmed" fs="italic" mb="sm">
                    {message.content.explanation}
                </Text>
                <TabbedContent 
                    message={message}
                    container={container}
                    chart={chart}
                    hasChart={hasChart}
                    isModal={false}
                />
            </Card>

            <Modal
                opened={modalOpened}
                onClose={() => setModalOpened(false)}
                title={
                    <Group gap="xs">
                        {hasChart ? <TbChartBar size={18} /> : <TbDatabase size={18} />}
                        <Text fw={600}>{hasChart ? "Chart" : "Query"}</Text>
                    </Group>
                }
                size="100%"
                styles={{
                    content: { height: '100vh', maxHeight: '100vh' },
                    body: { height: 'calc(100vh - 60px)', padding: 0, overflow: 'hidden' },
                    inner: { padding: 0 }
                }}
                centered={false}
                fullScreen={false}
            >
                <Box p="md" h="100%" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                    <Text size="sm" c="dimmed" fs="italic" mb="md" style={{ flexShrink: 0 }}>
                        {message.content.explanation}
                    </Text>
                    <Box style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
                        <TabbedContent 
                            message={message}
                            container={container}
                            chart={chart}
                            hasChart={hasChart}
                            isModal={true}
                        />
                    </Box>
                </Box>
            </Modal>
        </>
    );
}