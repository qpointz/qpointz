import {
    Card,
    Stack,
    Group,
    Text,
    Badge,
    Divider,
} from "@mantine/core";
import { TbTag, TbUser, TbBuilding, TbShield, TbRuler } from "react-icons/tb";
import type { ReactNode } from "react";

interface DescriptiveFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
}

export default function DescriptiveFacetView({ data, toggleButton }: DescriptiveFacetViewProps) {
    // Handle both direct merged data and scoped data (global, user:xxx, etc.)
    // DTO returns merged facet data directly, but we also support scoped structure
    const facetData = data?.global || data;
    
    const displayName = facetData?.displayName;
    const description = facetData?.description;
    const businessMeaning = facetData?.businessMeaning;
    const synonyms = facetData?.synonyms || [];
    const aliases = facetData?.aliases || [];
    const tags = facetData?.tags || [];
    const businessDomain = facetData?.businessDomain;
    const businessOwner = facetData?.businessOwner;
    const classification = facetData?.classification;
    const unit = facetData?.unit;

    const getClassificationColor = (classification?: string): string => {
        switch (classification?.toUpperCase()) {
            case 'PUBLIC':
                return 'green';
            case 'INTERNAL':
                return 'blue';
            case 'CONFIDENTIAL':
                return 'orange';
            case 'RESTRICTED':
                return 'red';
            default:
                return 'gray';
        }
    };

    return (
        <Card withBorder>
            <Stack gap="md">
                {/* Header with Display Name and Toggle */}
                {displayName && (
                    <Group justify="space-between" align="flex-start">
                        <div style={{ flex: 1 }}>
                            <Text size="sm" c="dimmed" mb={4}>Display Name</Text>
                            <Text fw={600} size="lg">{displayName}</Text>
                        </div>
                        {toggleButton && (
                            <div style={{ marginTop: 4 }}>
                                {toggleButton}
                            </div>
                        )}
                    </Group>
                )}
                {!displayName && toggleButton && (
                    <Group justify="flex-end">
                        {toggleButton}
                    </Group>
                )}

                {/* Description */}
                {description && (
                    <div>
                        <Text size="sm" c="dimmed" mb={4}>Description</Text>
                        <Text>{description}</Text>
                    </div>
                )}

                {/* Business Meaning */}
                {businessMeaning && (
                    <div>
                        <Text size="sm" c="dimmed" mb={4}>Business Meaning</Text>
                        <Text>{businessMeaning}</Text>
                    </div>
                )}

                {/* Metadata Row */}
                {(businessDomain || businessOwner || classification || unit) && (
                    <>
                        <Divider />
                        <Group gap="md">
                            {businessDomain && (
                                <Group gap={4}>
                                    <TbBuilding size={16} />
                                    <div>
                                        <Text size="xs" c="dimmed">Domain</Text>
                                        <Badge variant="light" size="sm">{businessDomain}</Badge>
                                    </div>
                                </Group>
                            )}
                            {businessOwner && (
                                <Group gap={4}>
                                    <TbUser size={16} />
                                    <div>
                                        <Text size="xs" c="dimmed">Owner</Text>
                                        <Text size="sm" fw={500}>{businessOwner}</Text>
                                    </div>
                                </Group>
                            )}
                            {classification && (
                                <Group gap={4}>
                                    <TbShield size={16} />
                                    <div>
                                        <Text size="xs" c="dimmed">Classification</Text>
                                        <Badge variant="light" color={getClassificationColor(classification)} size="sm">
                                            {classification}
                                        </Badge>
                                    </div>
                                </Group>
                            )}
                            {unit && (
                                <Group gap={4}>
                                    <TbRuler size={16} />
                                    <div>
                                        <Text size="xs" c="dimmed">Unit</Text>
                                        <Text size="sm" fw={500}>{unit}</Text>
                                    </div>
                                </Group>
                            )}
                        </Group>
                    </>
                )}

                {/* Synonyms */}
                {synonyms.length > 0 && (
                    <div>
                        <Text size="sm" c="dimmed" mb={8}>Synonyms</Text>
                        <Group gap={4}>
                            {synonyms.map((synonym: string, index: number) => (
                                <Badge key={index} variant="outline" size="sm">
                                    {synonym}
                                </Badge>
                            ))}
                        </Group>
                    </div>
                )}

                {/* Aliases */}
                {aliases.length > 0 && (
                    <div>
                        <Text size="sm" c="dimmed" mb={8}>Aliases</Text>
                        <Group gap={4}>
                            {aliases.map((alias: string, index: number) => (
                                <Badge key={index} variant="outline" size="sm">
                                    {alias}
                                </Badge>
                            ))}
                        </Group>
                    </div>
                )}

                {/* Tags */}
                {tags.length > 0 && (
                    <div>
                        <Group gap={4} mb={8}>
                            <TbTag size={16} />
                            <Text size="sm" c="dimmed">Tags</Text>
                        </Group>
                        <Group gap={4}>
                            {tags.map((tag: string, index: number) => (
                                <Badge key={index} variant="light" color="blue" size="sm">
                                    #{tag}
                                </Badge>
                            ))}
                        </Group>
                    </div>
                )}

                {/* Empty State */}
                {!displayName && !description && !businessMeaning && 
                 !businessDomain && !businessOwner && !classification && !unit &&
                 synonyms.length === 0 && aliases.length === 0 && tags.length === 0 && (
                    <Text c="dimmed" size="sm" ta="center" py="md">
                        No descriptive information available
                    </Text>
                )}
            </Stack>
        </Card>
    );
}

