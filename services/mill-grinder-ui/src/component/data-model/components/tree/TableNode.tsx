import { Box, NavLink, Group, Text, Collapse } from "@mantine/core";
import { TbTable, TbChevronRight, TbChevronDown } from "react-icons/tb";
import { useNavigate } from "react-router";
import type { TreeNodeDto } from "../../../../api/mill/api.ts";
import { parseEntityId } from "../../utils/entityUtils";
import ColumnNode from "./ColumnNode";

interface TableNodeProps {
    table: TreeNodeDto;
    columns: TreeNodeDto[];
    isExpanded: boolean;
    isSelected: boolean;
    selectedEntityId?: string;
    onToggle: (tableId: string) => void;
    onSelect: (tableId: string) => void;
    onColumnSelect: (columnId: string) => void;
}

export default function TableNode({ 
    table, 
    columns, 
    isExpanded, 
    isSelected, 
    selectedEntityId,
    onToggle, 
    onSelect,
    onColumnSelect 
}: TableNodeProps) {
    const navigate = useNavigate();
    const hasColumns = columns.length > 0;

    const handleClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (hasColumns && table.id) {
            onToggle(table.id);
        }
        if (table.id) {
            const location = parseEntityId(table.id);
            if (location.schema && location.table) {
                navigate(`/data-model/${location.schema}/${location.table}`);
            } else {
                onSelect(table.id);
            }
        }
    };

    return (
        <Box mt="xs">
            <NavLink
                component="div"
                label={table.name || parseEntityId(table.id).table || table.id}
                active={isSelected}
                leftSection={
                    <Group gap={4}>
                        {hasColumns && (
                            isExpanded ? <TbChevronDown size={12} /> : <TbChevronRight size={12} />
                        )}
                        <TbTable size={14} />
                    </Group>
                }
                rightSection={hasColumns ? (
                    <Text size="xs" c="dimmed">{columns.length}</Text>
                ) : undefined}
                onClick={handleClick}
            />
            {hasColumns && (
                <Collapse in={isExpanded}>
                    <Box pl="lg" pt="xs">
                        {columns.map((column) => {
                            const isColumnSelected = selectedEntityId === column.id;
                            return (
                                <ColumnNode
                                    key={column.id}
                                    column={column}
                                    isSelected={isColumnSelected}
                                    onSelect={onColumnSelect}
                                />
                            );
                        })}
                    </Box>
                </Collapse>
            )}
        </Box>
    );
}

