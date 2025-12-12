import {Box, Button, Stack, Text, Group, Title} from "@mantine/core";
import type {ChatMessage} from "../../../api/mill";
import {TbHelp} from "react-icons/tb";

interface ClarificationMessageProps {
    message: ChatMessage;
    onReply: (message: ChatMessage) => void;
    onCancel: () => void;
}

export default function ClarificationMessage({message, onReply, onCancel}: ClarificationMessageProps) {
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
        <Box 
            bg="white" 
            p={20} 
            m={10} 
            maw="95%" 
            style={{borderRadius: 10}}
        >
            <Group mb="md">
                <TbHelp />
                <Title order={4}>Clarification</Title>
            </Group>
            <Text mb="md">{userMessage}</Text>
            <Stack gap="sm" mb="md">
                {questionList.map((q: {id: string, question: string}) => (
                    <Text key={q.id} size="sm">• {q.question}</Text>
                ))}
            </Stack>
            <Group justify="flex-end" mt="md" gap="sm">
                <Button 
                    size="sm" 
                    variant="light"
                    onClick={() => onReply(message)}
                >
                    Reply
                </Button>
                <Button 
                    size="sm" 
                    variant="subtle" 
                    color="gray"
                    onClick={onCancel}
                >
                    Start Over
                </Button>
            </Group>
        </Box>
    );
}
