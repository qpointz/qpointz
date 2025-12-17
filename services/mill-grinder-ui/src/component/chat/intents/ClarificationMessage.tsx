import { Card, Button, Stack, Text, Group } from "@mantine/core";
import type { ChatMessage } from "../../../api/mill";
import { TbHelp } from "react-icons/tb";

interface ClarificationMessageProps {
    message: ChatMessage;
    onReply: (message: ChatMessage) => void;
    onCancel: () => void;
}

export default function ClarificationMessage({ message, onReply, onCancel }: ClarificationMessageProps) {
    // Extract questions from various possible locations
    const questions = message?.content?.questions || 
                     message?.content?.['step-back']?.questions || 
                     [];
    
    // Extract user-message from various possible locations
    const userMessage = message?.content?.['user-message'] || 
                       message?.content?.clarification?.['user-message'] ||
                       "To answer this accurately, I need a quick clarification:";

    // Handle questions that might be strings or objects with question/id fields
    const questionList = questions.map((q: any, idx: number) => {
        if (typeof q === 'string') {
            return { id: `q${idx}`, question: q };
        }
        return { id: q.id || `q${idx}`, question: q.question || q };
    });

    return (
        <Card w="100%" p="md" mb="xs">
            <Group gap="xs" mb="sm">
                <TbHelp size={16} />
                <Text fw={600} size="sm">Clarification</Text>
            </Group>
            <Text size="sm" mb="sm">{userMessage}</Text>
            <Stack gap="xs" mb="md">
                {questionList.map((q: { id: string, question: string }) => (
                    <Text key={q.id} size="sm" c="dimmed">• {q.question}</Text>
                ))}
            </Stack>
            <Group justify="flex-end" gap="xs">
                <Button 
                    size="xs" 
                    variant="light"
                    onClick={() => onReply(message)}
                >
                    Reply
                </Button>
                <Button 
                    size="xs" 
                    variant="subtle" 
                    color="gray"
                    onClick={onCancel}
                >
                    Start Over
                </Button>
            </Group>
        </Card>
    );
}
