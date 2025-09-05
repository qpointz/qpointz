import {Text, Alert, Box, Group, Stack, Title, Modal, Button} from "@mantine/core";
import {CodeHighlight} from "@mantine/code-highlight";
import {TbDetails, TbHelp} from "react-icons/tb";
import { useDisclosure } from "@mantine/hooks";

type UnsupportedIntentProps = {
    intent: string;
    content: any;
};

export default function UnsupportedIntent(ct: UnsupportedIntentProps) {
    const hints = ct.content?.reasoning?.hints ?? [];
    const message = ct.content?.reasoning?.hintMessage ?? null;
    const [opened, { open, close }] = useDisclosure(false);

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
                <Group mt={12} justify="flex-end">
                    <Button variant="outline" size="xs" onClick={open} leftSection={<TbDetails size={16} />}>Details</Button>
                </Group>
            </Stack>            
        </Box>
         )}