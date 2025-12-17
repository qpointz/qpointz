import { Card, Text, Group } from "@mantine/core";
import type { ChatMessage } from "../../../api/mill";
import { RingsLoader } from "../RingsLoader";

interface AssistantMessageProps {
    message: ChatMessage;
    isLoading?: boolean;
}

export default function AssistantMessage({ message, isLoading }: AssistantMessageProps) {
    const messageText = message?.content?.['user-message'] || message?.message || "";

    return (
        <Card 
            w="100%" 
            p="sm" 
            mb="xs"
            bg="gray.0"
            shadow="none"
            withBorder={false}
        >
            <Group gap="sm" align="flex-start">
                {isLoading && <RingsLoader size={16} />}
                <Text size="sm" style={{ flex: 1 }}>
                    {messageText.split('\n').map((line: any, idx: any, arr: any) => (
                        <span key={idx}>
                            {line}
                            {idx < arr.length - 1 && <br />}
                        </span>
                    ))}
                </Text>
            </Group>
        </Card>
    );
}
