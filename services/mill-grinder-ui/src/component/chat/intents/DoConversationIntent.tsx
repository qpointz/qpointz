import { Card, Group, Text } from "@mantine/core";
import { TbMessage } from "react-icons/tb";

export default function DoConversationIntent(data: any) {
    const { message } = data;
    const response = message?.content?.response || '';
    
    return (
        <Card w="100%" p="md" mb="xs">
            <Group gap="xs" mb="xs">
                <TbMessage size={16} />
                <Text fw={600} size="sm">Chat</Text>
            </Group>
            <Text size="sm">{response}</Text>
        </Card>
    );
}