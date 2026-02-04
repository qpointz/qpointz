import { Center, Stack, ThemeIcon, Title, Text, Button } from "@mantine/core";
import { Link } from "react-router";
import { TbFileOff } from "react-icons/tb";

export default function NotFound() {
    return (
        <Center h="100%">
            <Stack align="center" gap="md">
                <ThemeIcon size={80} radius="xl" variant="light" color="gray">
                    <TbFileOff size={40} />
                </ThemeIcon>
                <Title order={2}>Page Not Found</Title>
                <Text c="dimmed">The page you're looking for doesn't exist.</Text>
                <Button component={Link} to="/" variant="light">
                    Go to Home
                </Button>
            </Stack>
        </Center>
    );
}