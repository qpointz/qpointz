import {
    Stack,
    Group,
    Text,
    Badge,
} from "@mantine/core";
import { TbTag, TbUser, TbBuilding, TbShield, TbRuler } from "react-icons/tb";
import type { ReactNode } from "react";

interface DescriptiveFacetViewProps {
    data: any;
    toggleButton?: ReactNode;
}

export default function DescriptiveFacetView({ data, toggleButton }: DescriptiveFacetViewProps) {
    // Handle both direct merged data and scoped data (global, user:xxx, etc.)
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

    // Empty state
    if (!displayName && !description && !businessMeaning && 
        !businessDomain && !businessOwner && !classification && !unit &&
        synonyms.length === 0 && aliases.length === 0 && tags.length === 0) {
        return (
            <Group justify="space-between">
                <Text c="dimmed" size="sm">No descriptive information available</Text>
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

            {/* Display Name */}
            {displayName && (
                <Text fw={600}>{displayName}</Text>
            )}

            {/* Description */}
            {description && (
                <Text size="sm">{description}</Text>
            )}

            {/* Business Meaning */}
            {businessMeaning && (
                <Text size="sm" c="dimmed" fs="italic">{businessMeaning}</Text>
            )}

            {/* Metadata Row - compact inline */}
            {(businessDomain || businessOwner || classification || unit) && (
                <Group gap="md" mt="xs">
                    {businessDomain && (
                        <Group gap={4}>
                            <TbBuilding size={14} />
                            <Badge variant="light" size="xs">{businessDomain}</Badge>
                        </Group>
                    )}
                    {businessOwner && (
                        <Group gap={4}>
                            <TbUser size={14} />
                            <Text size="xs">{businessOwner}</Text>
                        </Group>
                    )}
                    {classification && (
                        <Group gap={4}>
                            <TbShield size={14} />
                            <Badge variant="light" color={getClassificationColor(classification)} size="xs">
                                {classification}
                            </Badge>
                        </Group>
                    )}
                    {unit && (
                        <Group gap={4}>
                            <TbRuler size={14} />
                            <Text size="xs">{unit}</Text>
                        </Group>
                    )}
                </Group>
            )}

            {/* Synonyms & Aliases - combined row */}
            {(synonyms.length > 0 || aliases.length > 0) && (
                <Group gap="xs">
                    {synonyms.map((synonym: string, index: number) => (
                        <Badge key={`syn-${index}`} variant="outline" size="xs">
                            {synonym}
                        </Badge>
                    ))}
                    {aliases.map((alias: string, index: number) => (
                        <Badge key={`alias-${index}`} variant="outline" size="xs" color="gray">
                            {alias}
                        </Badge>
                    ))}
                </Group>
            )}

            {/* Tags */}
            {tags.length > 0 && (
                <Group gap={4}>
                    <TbTag size={14} />
                    {tags.map((tag: string, index: number) => (
                        <Badge key={index} variant="light" color="blue" size="xs">
                            #{tag}
                        </Badge>
                    ))}
                </Group>
            )}
        </Stack>
    );
}

