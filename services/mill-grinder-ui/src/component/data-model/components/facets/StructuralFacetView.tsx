import {
    Stack,
    Group,
    Text,
    Badge,
} from "@mantine/core";
import { TbKey, TbLink, TbShieldCheck, TbDatabase, TbClock } from "react-icons/tb";
import type { ReactNode } from "react";

interface StructuralFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
}

export default function StructuralFacetView({ data, toggleButton }: StructuralFacetViewProps) {
    const facetData = data?.global || data;
    
    const physicalName = facetData?.physicalName;
    const physicalType = facetData?.physicalType;
    const precision = facetData?.precision;
    const scale = facetData?.scale;
    const nullable = facetData?.nullable;
    const isPrimaryKey = facetData?.isPrimaryKey;
    const isForeignKey = facetData?.isForeignKey;
    const isUnique = facetData?.isUnique;
    const backendType = facetData?.backendType;
    const tableType = facetData?.tableType;
    const lastSyncedAt = facetData?.lastSyncedAt;

    const formatType = (): string => {
        if (!physicalType) return '';
        if (precision !== null && precision !== undefined) {
            if (scale !== null && scale !== undefined) {
                return `${physicalType}(${precision},${scale})`;
            }
            return `${physicalType}(${precision})`;
        }
        return physicalType;
    };

    const formatDate = (dateStr?: string): string => {
        if (!dateStr) return '';
        try {
            const date = new Date(dateStr);
            return date.toLocaleDateString();
        } catch {
            return dateStr;
        }
    };

    // Empty state
    if (!physicalName && !physicalType && !backendType && !tableType && 
        !lastSyncedAt && nullable === null && !isPrimaryKey && !isForeignKey && !isUnique) {
        return (
            <Group justify="space-between">
                <Text c="dimmed" size="sm">No structural information available</Text>
                {toggleButton}
            </Group>
        );
    }

    return (
        <Stack gap="xs">
            {/* Toggle button row */}
            {toggleButton && (
                <Group justify="flex-end">
                    {toggleButton}
                </Group>
            )}

            {/* Physical Name and Type in one row */}
            <Group gap="md">
                {physicalName && (
                    <Text size="sm" fw={500}>{physicalName}</Text>
                )}
                {physicalType && (
                    <Badge variant="light" size="sm" color="blue">
                        {formatType()}
                    </Badge>
                )}
            </Group>

            {/* Constraints - compact badges */}
            {(isPrimaryKey || isForeignKey || isUnique || nullable !== null) && (
                <Group gap="xs">
                    {isPrimaryKey && (
                        <Badge variant="filled" color="yellow" size="xs" leftSection={<TbKey size={12} />}>
                            PK
                        </Badge>
                    )}
                    {isForeignKey && (
                        <Badge variant="filled" color="orange" size="xs" leftSection={<TbLink size={12} />}>
                            FK
                        </Badge>
                    )}
                    {isUnique && (
                        <Badge variant="filled" color="green" size="xs" leftSection={<TbShieldCheck size={12} />}>
                            Unique
                        </Badge>
                    )}
                    {nullable !== null && (
                        <Badge variant={nullable ? "light" : "filled"} color={nullable ? "gray" : "red"} size="xs">
                            {nullable ? "Null" : "Not Null"}
                        </Badge>
                    )}
                </Group>
            )}

            {/* Metadata - compact inline */}
            {(backendType || tableType || lastSyncedAt) && (
                <Group gap="md">
                    {backendType && (
                        <Group gap={4}>
                            <TbDatabase size={14} />
                            <Text size="xs" c="dimmed">{backendType}</Text>
                        </Group>
                    )}
                    {tableType && (
                        <Group gap={4}>
                            <TbDatabase size={14} />
                            <Text size="xs" c="dimmed">{tableType}</Text>
                        </Group>
                    )}
                    {lastSyncedAt && (
                        <Group gap={4}>
                            <TbClock size={14} />
                            <Text size="xs" c="dimmed">{formatDate(lastSyncedAt)}</Text>
                        </Group>
                    )}
                </Group>
            )}
        </Stack>
    );
}

