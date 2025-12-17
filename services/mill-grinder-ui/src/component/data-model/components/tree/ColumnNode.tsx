import { NavLink } from "@mantine/core";
import { TbColumns } from "react-icons/tb";
import { useNavigate } from "react-router";
import type { TreeNodeDto } from "../../../../api/mill/api.ts";
import { parseEntityId } from "../../utils/entityUtils";

interface ColumnNodeProps {
    column: TreeNodeDto;
    isSelected: boolean;
    onSelect: (columnId: string) => void;
}

export default function ColumnNode({ column, isSelected, onSelect }: ColumnNodeProps) {
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
        <NavLink
            component="div"
            label={column.name || parseEntityId(column.id).attribute || column.id}
            active={isSelected}
            leftSection={<TbColumns size={12} />}
            onClick={handleClick}
            py="xs"
        />
    );
}

