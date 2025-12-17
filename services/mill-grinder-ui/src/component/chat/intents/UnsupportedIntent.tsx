import { Card, Text, Alert, Box, Group, Stack, Modal, Button, UnstyledButton } from "@mantine/core";
import { CodeHighlight } from "@mantine/code-highlight";
import { TbDetails, TbHelp, TbDatabase, TbChartBar, TbBulb, TbMessage, TbRefresh, TbCode } from "react-icons/tb";
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
            return <TbDatabase size={14} />;
        case "get-chart":
            return <TbChartBar size={14} />;
        case "explain":
            return <TbBulb size={14} />;
        case "refine":
            return <TbRefresh size={14} />;
        case "do-conversation":
            return <TbMessage size={14} />;
        case "enrich-model":
            return <TbCode size={14} />;
        default:
            return <TbHelp size={14} />;
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
        <Card w="100%" p="md" mb="xs">
            <Group gap="xs" mb="xs">
                <TbHelp size={16} />
                <Text fw={600} size="sm">Question</Text>
            </Group>

            <Modal
                size="70%"
                opened={opened}
                onClose={close}
                title={ct.intent}
                overlayProps={{
                    backgroundOpacity: 0.55,
                    blur: 3
                }}
            >
                <Alert title={ct.intent} color="red">
                    <Box w="400px">
                        <CodeHighlight expanded={true} expandCodeLabel="" code={JSON.stringify(ct.content, null, 2)} language="json" />
                    </Box>
                </Alert>
            </Modal>

            <Stack gap="xs">
                <Text size="sm" c="dimmed" fs="italic">{message}</Text>
                <Text size="sm">
                    {hints && hints.join("\n").split("\n").map((line: string, idx: number) => (
                        <span key={idx}>
                            {line}
                            {idx < hints.length - 1 && <br />}
                        </span>
                    ))}
                </Text>

                {suggestedIntents && suggestedIntents.length > 0 && (
                    <Box mt="sm">
                        <Text size="xs" fw={500} c="dimmed" mb="xs">Suggested Actions:</Text>
                        <Stack gap={4}>
                            {suggestedIntents.map((suggestedIntent, idx) => {
                                const letter = String.fromCharCode(65 + idx);
                                return (
                                    <UnstyledButton
                                        key={idx}
                                        onClick={() => handleSuggestedIntentClick(suggestedIntent)}
                                        p="xs"
                                        w="100%"
                                        styles={{
                                            root: {
                                                borderRadius: 'var(--mantine-radius-sm)',
                                                border: '1px solid var(--mantine-color-gray-3)',
                                                '&:hover': {
                                                    backgroundColor: 'var(--mantine-color-gray-0)',
                                                    borderColor: 'var(--mantine-color-primary-4)',
                                                },
                                            },
                                        }}
                                    >
                                        <Group gap="xs" align="center" wrap="nowrap">
                                            <Text fw={700} size="xs" c="primary" style={{ minWidth: 16 }}>
                                                {letter}.
                                            </Text>
                                            {getIntentIcon(suggestedIntent.intent)}
                                            <Box style={{ flex: 1 }}>
                                                <Group gap={4} align="center" wrap="nowrap">
                                                    <Text fw={500} size="xs">
                                                        {getIntentLabel(suggestedIntent.intent)}
                                                    </Text>
                                                    <Text size="xs" c="dimmed" style={{ flex: 1 }}>
                                                        {suggestedIntent.description}
                                                    </Text>
                                                </Group>
                                                {suggestedIntent.exampleQuery && (
                                                    <Text size="xs" fs="italic" c="primary" mt={2}>
                                                        "{suggestedIntent.exampleQuery}"
                                                    </Text>
                                                )}
                                            </Box>
                                        </Group>
                                    </UnstyledButton>
                                );
                            })}
                        </Stack>
                    </Box>
                )}

                <Group mt="sm" justify="flex-end">
                    <Button variant="outline" size="xs" onClick={open} leftSection={<TbDetails size={14} />}>
                        Details
                    </Button>
                </Group>
            </Stack>
        </Card>
    );
}