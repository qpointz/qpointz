import { Card, Group, Text, Box } from "@mantine/core";
import { TbSchool } from "react-icons/tb";
import Markdown from "react-markdown";

function ExplainIntent(data: any) {
    const renderDescription = (description: any) => {
        const blocks: Array<string> = description.split(/```/);
        return blocks.map((block: string, i: number) => {
            if (block.trim().startsWith('mermaid')) {
                return null;
            } else {
                return (
                    <Markdown key={i}>{block}</Markdown>
                );
            }
        });
    };

    return (
        <Card w="100%" p="md" mb="xs">
            <Group gap="xs" mb="xs">
                <TbSchool size={16} />
                <Text fw={600} size="sm">Explain</Text>
            </Group>
            <Box style={{ fontSize: 'var(--mantine-font-size-sm)' }}>
                {renderDescription(data?.message?.content?.description || '')}
            </Box>
        </Card>
    );
}

export default ExplainIntent;