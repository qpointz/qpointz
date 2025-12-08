import {Text, Alert, Box, Group, Stack, Title, Modal, Button} from "@mantine/core";
import {CodeHighlight} from "@mantine/code-highlight";
import {TbDetails, TbHelp, TbDatabase, TbChartBar, TbBulb, TbMessage, TbRefresh, TbCode} from "react-icons/tb";
import { useDisclosure } from "@mantine/hooks";
import { useChatContext } from "../ChatProvider";

type UnsupportedIntentProps = {
    intent: string;
    content: any;
};

type SuggestedIntent = {
    intent: string;
    description: string;
    exampleQuery?: string;
};

const getIntentIcon = (intent: string) => {
    switch (intent) {
        case "get-data":
            return <TbDatabase size={16} />;
        case "get-chart":
            return <TbChartBar size={16} />;
        case "explain":
            return <TbBulb size={16} />;
        case "refine":
            return <TbRefresh size={16} />;
        case "do-conversation":
            return <TbMessage size={16} />;
        case "enrich-model":
            return <TbCode size={16} />;
        default:
            return <TbHelp size={16} />;
    }
};

const getIntentLabel = (intent: string) => {
    switch (intent) {
        case "get-data":
            return "Get Data";
        case "get-chart":
            return "Get Chart";
        case "explain":
            return "Explain";
        case "refine":
            return "Refine";
        case "do-conversation":
            return "Conversation";
        case "enrich-model":
            return "Enrich Model";
        default:
            return intent;
    }
};

export default function UnsupportedIntent(ct: UnsupportedIntentProps) {
    const hints = ct.content?.reasoning?.hints ?? [];
    const message = ct.content?.reasoning?.hintMessage ?? null;
    const suggestedIntents: SuggestedIntent[] = ct.content?.reasoning?.suggestedIntents ?? [];
    const { messages } = useChatContext();
    const [opened, { open, close }] = useDisclosure(false);

    const handleSuggestedIntentClick = (suggestedIntent: SuggestedIntent) => {
        const query = suggestedIntent.exampleQuery || suggestedIntent.description;
        if (query) {
            messages.post(query);
        }
    };

    return (
        <Box bg="white" p={20} m={10} maw={"95%"} style={{borderRadius: 10}}>        
            <Group>
                <><TbHelp/><Title order={4}>Question</Title></>
            </Group>
            <Modal  size={"70%"} opened={opened}
                        onClose={close}
                        title={`${ct.intent}`}
                        overlayProps={{
                        backgroundOpacity: 0.55,
                        blur: 3}}>
                <Alert title={`${ct.intent}`} color="danger">
                    <Box w="400px">
                        <CodeHighlight expanded={true} expandCodeLabel={""} code={JSON.stringify(ct.content, null, 2)} language="json"/>
                    </Box>
                </Alert>
            </Modal>
            <Stack>
                <Text fs="italic">{message}</Text>
                <Text>
                {hints && hints.join("\n").split("\n").map((line:string, idx:number) => (
                    <span key={idx}>
                        {line}
                        {idx < hints.length - 1 && <br />}
                    </span>
                ))}
                </Text>
                
                {suggestedIntents && suggestedIntents.length > 0 && (
                    <Box mt="md">
                        <Text size="sm" fw={500} mb="sm">Suggested Actions:</Text>
                        <Stack gap={4}>
                            {suggestedIntents.map((suggestedIntent, idx) => {
                                const letter = String.fromCharCode(65 + idx); // A, B, C, etc.
                                return (
                                    <Box
                                        key={idx}
                                        p={8}
                                        style={{ 
                                            cursor: 'pointer',
                                            borderRadius: 4,
                                            border: '1px solid var(--mantine-color-gray-3)'
                                        }}
                                        onClick={() => handleSuggestedIntentClick(suggestedIntent)}
                                        onMouseEnter={(e) => {
                                            e.currentTarget.style.backgroundColor = 'var(--mantine-color-gray-0)';
                                            e.currentTarget.style.borderColor = 'var(--mantine-color-blue-4)';
                                        }}
                                        onMouseLeave={(e) => {
                                            e.currentTarget.style.backgroundColor = 'transparent';
                                            e.currentTarget.style.borderColor = 'var(--mantine-color-gray-3)';
                                        }}
                                    >
                                        <Group gap="xs" align="center" wrap="nowrap">
                                            <Text fw={700} size="sm" c="blue" style={{ minWidth: 20 }}>
                                                {letter}.
                                            </Text>
                                            {getIntentIcon(suggestedIntent.intent)}
                                            <Box style={{ flex: 1 }}>
                                                <Group gap={6} align="center" wrap="nowrap">
                                                    <Text fw={500} size="sm">
                                                        {getIntentLabel(suggestedIntent.intent)}
                                                    </Text>
                                                    <Text size="xs" c="dimmed" style={{ flex: 1 }}>
                                                        {suggestedIntent.description}
                                                    </Text>
                                                </Group>
                                                {suggestedIntent.exampleQuery && (
                                                    <Text size="xs" fs="italic" c="blue" mt={2}>
                                                        "{suggestedIntent.exampleQuery}"
                                                    </Text>
                                                )}
                                            </Box>
                                        </Group>
                                    </Box>
                                );
                            })}
                        </Stack>
                    </Box>
                )}
                
                <Group mt={12} justify="flex-end">
                    <Button variant="outline" size="xs" onClick={open} leftSection={<TbDetails size={16} />}>Details</Button>
                </Group>
            </Stack>            
        </Box>
    );
}