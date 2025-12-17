import { Group, Text, UnstyledButton } from "@mantine/core";
import { TbDatabase, TbTable, TbColumns, TbBulb } from "react-icons/tb";
import { useNavigate } from "react-router";
import { useMetadataContext } from "../MetadataProvider";
import type { MetadataEntityDto } from "../../../api/mill/api.ts";
import { buildLocation } from "../utils/entityUtils";

interface RelatedItemLinkProps {
    entity: MetadataEntityDto;
    onClick?: () => void;
}

export default function RelatedItemLink({ entity, onClick }: RelatedItemLinkProps) {
    const navigate = useNavigate();
    const { entity: entityContext } = useMetadataContext();

    const getTypeIcon = () => {
        switch (entity.type) {
            case 'SCHEMA':
                return <TbDatabase size={12} />;
            case 'TABLE':
                return <TbTable size={12} />;
            case 'ATTRIBUTE':
                return <TbColumns size={12} />;
            case 'CONCEPT':
                return <TbBulb size={12} />;
            default:
                return null;
        }
    };

    const handleClick = () => {
        if (onClick) {
            onClick();
        }
        
        if (!entity.id) return;

        // Navigate based on entity type
        switch (entity.type) {
            case 'CONCEPT':
                navigate(`/context/${entity.id}`);
                break;
            case 'TABLE':
                if (entity.schemaName && entity.tableName) {
                    navigate(`/data-model/${entity.schemaName}/${entity.tableName}`);
                } else if (entity.id) {
                    entityContext.select(entity.id);
                }
                break;
            case 'ATTRIBUTE':
                if (entity.schemaName && entity.tableName && entity.attributeName) {
                    navigate(`/data-model/${entity.schemaName}/${entity.tableName}/${entity.attributeName}`);
                } else if (entity.id) {
                    entityContext.select(entity.id);
                }
                break;
            case 'SCHEMA':
                if (entity.id) {
                    entityContext.select(entity.id);
                }
                break;
            default:
                if (entity.id) {
                    entityContext.select(entity.id);
                }
        }
    };

    const displayName = entity.id || buildLocation(entity) || 'Unknown';

    return (
        <UnstyledButton
            onClick={handleClick}
            py={2}
            px={4}
            w="100%"
            style={{ borderRadius: 'var(--mantine-radius-sm)' }}
            styles={{
                root: {
                    '&:hover': {
                        backgroundColor: 'var(--mantine-color-gray-2)',
                    },
                },
            }}
        >
            <Group gap={4} wrap="nowrap">
                {getTypeIcon()}
                <Text size="xs" truncate>{displayName}</Text>
            </Group>
        </UnstyledButton>
    );
}

