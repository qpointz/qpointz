import {Box, Group, Tabs, Text, Title} from "@mantine/core";
import {TbChartBar, TbDatabase, TbFileTypeSql} from "react-icons/tb";
import { format } from 'sql-formatter';
import ChartView from "../../../data/ChartView.tsx";
import DataContainer from "../../../data/DataContainer.tsx";
import {CodeHighlight} from "@mantine/code-highlight";




export default function GetDataIntent(data: any) {
    const { message } = data;
    const container = message?.content?.data?.container || {};
    const chart = message?.content?.chart || {};
    const hasChart = chart?.type && chart?.type !== 'none';
    return (
        <Box bg="white" p={20} m={10} key={message.id}>
            <Group>
                { hasChart && ( <><TbChartBar/><Title order={4}>Chart</Title></> )}
                { !hasChart && ( <><TbDatabase/><Title order={4}>Query</Title></> )}
            </Group>
            <Text fs="italic">{message.content.explanation}</Text>
            <Tabs defaultValue={hasChart ? "chart" : "data"}>
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

                    <Tabs.Tab value="answer" leftSection={<TbFileTypeSql size={12} />}>
                        Answer
                    </Tabs.Tab>
                </Tabs.List>

                { hasChart && (
                <Tabs.Panel value="chart">
                    <ChartView chart={chart} container={container} />
                </Tabs.Panel> )}

                <Tabs.Panel value="data">
                    <DataContainer data={message.content.data?.container}/>
                </Tabs.Panel>

                <Tabs.Panel value="sql">
                    <CodeHighlight code={format(message.content?.sql ?? "")} language="sql"/>
                </Tabs.Panel>

            </Tabs>
        </Box>

    );
}