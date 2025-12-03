import { Box, Group, Text, Badge } from "@mantine/core";
import { TbDatabase, TbTable, TbColumns, TbBulb } from "react-icons/tb";
import { useNavigate } from "react-router";
import { useMantineTheme } from "@mantine/core";
import { useMetadataContext } from "../MetadataProvider";
import type { MetadataEntityDto } from "../../../api/mill/api.ts";
import { buildLocation } from "../utils/entityUtils";

interface RelatedItemLinkProps {
    entity: MetadataEntityDto;
    onClick?: () => void;
}

export default function RelatedItemLink({ entity, onClick }: RelatedItemLinkProps) {
    const theme = useMantineTheme();
    const navigate = useNavigate();
    const { entity: entityContext } = useMetadataContext();

    const getTypeIcon = () => {
        switch (entity.type) {
            case 'SCHEMA':
                return <TbDatabase size={16} />;
            case 'TABLE':
                return <TbTable size={16} />;
            case 'ATTRIBUTE':
                return <TbColumns size={16} />;
            case 'CONCEPT':
                return <TbBulb size={16} />;
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
                navigate(`/concepts/${entity.id}`);
                break;
            case 'TABLE':
                if (entity.schemaName && entity.tableName) {
                    navigate(`/data-model/${entity.schemaName}/${entity.tableName}`);
                } else if (entity.id) {
                    // Select entity by ID as fallback
                    entityContext.select(entity.id);
                }
                break;
            case 'ATTRIBUTE':
                if (entity.schemaName && entity.tableName && entity.attributeName) {
                    navigate(`/data-model/${entity.schemaName}/${entity.tableName}/${entity.attributeName}`);
                } else if (entity.id) {
                    // Select entity by ID as fallback
                    entityContext.select(entity.id);
                }
                break;
            case 'SCHEMA':
                // For schemas, select by ID since we don't have a schema-only route
                if (entity.id) {
                    entityContext.select(entity.id);
                }
                break;
            default:
                // Fallback: select by ID
                if (entity.id) {
                    entityContext.select(entity.id);
                }
        }
    };

    const displayName = entity.id || buildLocation(entity) || 'Unknown';

    return (
        <Box
            p={8}
            mb={4}
            style={{
                borderRadius: 6,
                cursor: 'pointer',
                transition: 'background 0.2s',
            }}
            onClick={handleClick}
            onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
            onMouseLeave={e => e.currentTarget.style.background = ''}
        >
            <Group gap="sm" justify="space-between">
                <Group gap="xs">
                    {getTypeIcon()}
                    <Text size="sm" fw={500}>
                        {displayName}
                    </Text>
                </Group>
                {entity.type && (
                    <Badge variant="light" size="xs" color="primary">
                        {entity.type}
                    </Badge>
                )}
            </Group>
        </Box>
    );
}

