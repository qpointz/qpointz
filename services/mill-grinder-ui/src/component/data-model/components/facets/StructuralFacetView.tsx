import {
    Card,
    Stack,
    Group,
    Text,
    Badge,
    Divider,
    Grid,
} from "@mantine/core";
import { TbKey, TbLink, TbShieldCheck, TbDatabase, TbClock } from "react-icons/tb";
import type { ReactNode } from "react";

interface StructuralFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
}

export default function StructuralFacetView({ data, toggleButton }: StructuralFacetViewProps) {
    // Handle both direct data and scoped data (global, user:xxx, etc.)
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
            return date.toLocaleString();
        } catch {
            return dateStr;
        }
    };

    return (
        <Card withBorder>
            <Stack gap="md">
                {/* Header with Physical Name and Toggle */}
                {physicalName && (
                    <Group justify="space-between" align="flex-start">
                        <div style={{ flex: 1 }}>
                            <Text size="sm" c="dimmed" mb={4}>Physical Name</Text>
                            <Text fw={600} size="lg">{physicalName}</Text>
                        </div>
                        {toggleButton && (
                            <div style={{ marginTop: 4 }}>
                                {toggleButton}
                            </div>
                        )}
                    </Group>
                )}
                {!physicalName && toggleButton && (
                    <Group justify="flex-end">
                        {toggleButton}
                    </Group>
                )}

                {/* Type Information */}
                {(physicalType || precision !== null || scale !== null) && (
                    <div>
                        <Text size="sm" c="dimmed" mb={4}>Data Type</Text>
                        <Badge variant="light" size="lg" color="blue">
                            {formatType()}
                        </Badge>
                    </div>
                )}

                <Divider />

                {/* Constraints */}
                {(isPrimaryKey || isForeignKey || isUnique || nullable !== null) && (
                    <div>
                        <Text size="sm" c="dimmed" mb={8}>Constraints</Text>
                        <Group gap="sm">
                            {isPrimaryKey && (
                                <Badge variant="filled" color="yellow" leftSection={<TbKey size={14} />}>
                                    Primary Key
                                </Badge>
                            )}
                            {isForeignKey && (
                                <Badge variant="filled" color="orange" leftSection={<TbLink size={14} />}>
                                    Foreign Key
                                </Badge>
                            )}
                            {isUnique && (
                                <Badge variant="filled" color="green" leftSection={<TbShieldCheck size={14} />}>
                                    Unique
                                </Badge>
                            )}
                            {nullable !== null && (
                                <Badge variant={nullable ? "light" : "filled"} color={nullable ? "gray" : "red"}>
                                    {nullable ? "Nullable" : "Not Null"}
                                </Badge>
                            )}
                        </Group>
                    </div>
                )}

                {/* Metadata Grid */}
                {(backendType || tableType || lastSyncedAt) && (
                    <>
                        <Divider />
                        <Grid>
                            {backendType && (
                                <Grid.Col span={6}>
                                    <Group gap={4}>
                                        <TbDatabase size={16} />
                                        <div>
                                            <Text size="xs" c="dimmed">Backend Type</Text>
                                            <Text size="sm" fw={500}>{backendType}</Text>
                                        </div>
                                    </Group>
                                </Grid.Col>
                            )}
                            {tableType && (
                                <Grid.Col span={6}>
                                    <Group gap={4}>
                                        <TbDatabase size={16} />
                                        <div>
                                            <Text size="xs" c="dimmed">Table Type</Text>
                                            <Text size="sm" fw={500}>{tableType}</Text>
                                        </div>
                                    </Group>
                                </Grid.Col>
                            )}
                            {lastSyncedAt && (
                                <Grid.Col span={6}>
                                    <Group gap={4}>
                                        <TbClock size={16} />
                                        <div>
                                            <Text size="xs" c="dimmed">Last Synced</Text>
                                            <Text size="sm" fw={500}>{formatDate(lastSyncedAt)}</Text>
                                        </div>
                                    </Group>
                                </Grid.Col>
                            )}
                        </Grid>
                    </>
                )}

                {/* Empty State */}
                {!physicalName && !physicalType && !backendType && !tableType && 
                 !lastSyncedAt && nullable === null && !isPrimaryKey && !isForeignKey && !isUnique && (
                    <Text c="dimmed" size="sm" ta="center" py="md">
                        No structural information available
                    </Text>
                )}
            </Stack>
        </Card>
    );
}

