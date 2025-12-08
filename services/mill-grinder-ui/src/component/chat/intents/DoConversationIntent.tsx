import {Box, Group, Title, Text} from "@mantine/core";
import {TbMessage} from "react-icons/tb";

export default function DoConversationIntent(data: any) {
    const {message} = data;
    const response = message?.content?.response || '';
    
    return (
        <Box bg="white" p={20} m={10} maw={"95%"} style={{borderRadius: 10}}>
            <Group>
                <TbMessage/>
                <Title order={4}>Chat</Title>
            </Group>
            <Text mt="md">{response}</Text>
        </Box>
    );
}