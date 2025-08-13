import {Text, Alert, Box, Group, Stack, Title} from "@mantine/core";
import {CodeHighlight} from "@mantine/code-highlight";
import {TbHelp} from "react-icons/tb";

type UnsupportedIntentProps = {
    intent: string;
    content: any;
};

export default function UnsupportedIntent(ct: UnsupportedIntentProps) {
    const hints = ct.content?.reasoning?.hints ?? [];
    return (
        <Group maw="800px" bg="gray.2" p={10} mb={10} style={{borderRadius: 10, alignSelf: "flex-start"}}>
            <Box bg="white" p={20} m={10}>
                <Group>
                    <><TbHelp/><Title order={4}>Query</Title></>
                </Group>
                <Stack>
                    <Alert title={`${ct.intent}`} color="danger">
                        <Box>
                            <CodeHighlight expanded={true} expandCodeLabel={""} code={JSON.stringify(ct.content, null, 2)} language="json"/>
                        </Box>
                    </Alert>
                    {hints && hints.map((h:any)=> <Text>{h}</Text> )}
                </Stack>
            </Box>
        </Group> )}