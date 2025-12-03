import { Box, Group, Text } from "@mantine/core";
import { TbColumns } from "react-icons/tb";
import { useMantineTheme } from "@mantine/core";
import { useNavigate } from "react-router";
import type { TreeNodeDto } from "../../../../api/mill/api.ts";
import { parseEntityId } from "../../utils/entityUtils";

interface ColumnNodeProps {
    column: TreeNodeDto;
    isSelected: boolean;
    onSelect: (columnId: string) => void;
}

export default function ColumnNode({ column, isSelected, onSelect }: ColumnNodeProps) {
    const theme = useMantineTheme();
    const navigate = useNavigate();

    const handleClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (column.id) {
            const location = parseEntityId(column.id);
            if (location.schema && location.table && location.attribute) {
                navigate(`/data-model/${location.schema}/${location.table}/${location.attribute}`);
            } else {
                onSelect(column.id);
            }
        }
    };

    return (
        <Box
            p={3}
            m={0}
            mb={2}
            style={{
                borderRadius: 6,
                background: isSelected ? theme.colors.blue[0] : undefined,
                transition: 'background 0.2s',
                cursor: 'pointer',
            }}
            onClick={handleClick}
            onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
            onMouseLeave={e => e.currentTarget.style.background = isSelected ? theme.colors.blue[0] : ''}
        >
            <Group gap="xs">
                <TbColumns size={12} />
                <Text size="sm" fw={isSelected ? 600 : 400}>
                    {column.name || parseEntityId(column.id).attribute || column.id}
                </Text>
            </Group>
        </Box>
    );
}

