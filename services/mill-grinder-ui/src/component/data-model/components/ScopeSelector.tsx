import { Box, Group, Text, Select } from "@mantine/core";

interface ScopeSelectorProps {
    currentScope: string;
    availableScopes?: string[];
    onScopeChange: (scope: string) => void;
    collapsed: boolean;
}

export default function ScopeSelector({ 
    currentScope, 
    availableScopes,
    onScopeChange, 
    collapsed 
}: ScopeSelectorProps) {
    if (collapsed) {
        return null;
    }

    const scopeOptions = availableScopes || [
        { value: 'global', label: 'Global' },
        { value: 'user:default', label: 'User' },
    ];

    return (
        <Box mt={20} mb={15}>
            <Group gap="xs" mb={8}>
                <Text size="sm" c="dimmed">Scope:</Text>
            </Group>
            <Select
                value={currentScope}
                onChange={(value) => {
                    if (value) {
                        onScopeChange(value);
                    }
                }}
                data={scopeOptions}
                size="sm"
            />
        </Box>
    );
}

