/**
 * Context View Component
 * 
 * Displays contexts organized by categories and tags.
 * Allows filtering and selection of contexts.
 */
import { useState } from "react";
import { useMetadataContext } from "../data-model/MetadataProvider";
import {
    Box,
    Text,
    Group,
    Loader,
    Badge,
    Stack,
    UnstyledButton,
} from "@mantine/core";
import { TbTag, TbCategory, TbFocusCentered } from "react-icons/tb";
import { useNavigate } from "react-router";
import type { MetadataEntityDto } from "../../api/mill/api.ts";

/**
 * Extract concept names from a concept entity.
 */
function getConceptNames(entity: MetadataEntityDto): string[] {
    const conceptFacet = entity.facets?.concept;
    if (conceptFacet) {
        const globalFacet = conceptFacet.global || conceptFacet;
        const conceptsList = globalFacet.concepts || [];
        return conceptsList.map((c: any) => c.name).filter(Boolean);
    }
    return [];
}

interface ClickableItemProps {
    selected: boolean;
    onClick: () => void;
    children: React.ReactNode;
}

function ClickableItem({ selected, onClick, children }: ClickableItemProps) {
    return (
        <UnstyledButton
            onClick={onClick}
            p={6}
            mb={4}
            w="100%"
            styles={{
                root: {
                    borderRadius: 'var(--mantine-radius-sm)',
                    backgroundColor: selected ? 'var(--mantine-color-primary-0)' : undefined,
                    '&:hover': {
                        backgroundColor: selected 
                            ? 'var(--mantine-color-primary-0)' 
                            : 'var(--mantine-color-gray-1)',
                    },
                },
            }}
        >
            {children}
        </UnstyledButton>
    );
}

export default function ContextView() {
    const { concepts, entity } = useMetadataContext();
    const navigate = useNavigate();
    const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
    const [selectedTag, setSelectedTag] = useState<string | null>(null);

    // Get filtered concepts based on selected category or tag
    const filteredConcepts = selectedCategory
        ? concepts.getByCategory(selectedCategory)
        : selectedTag
        ? concepts.getByTag(selectedTag)
        : concepts.data; // Show all concepts when nothing is selected

    const handleCategoryClick = (category: string) => {
        setSelectedCategory(category);
        setSelectedTag(null);
    };

    const handleTagClick = (tag: string) => {
        setSelectedTag(tag);
        setSelectedCategory(null);
    };

    const handleConceptClick = (conceptEntity: MetadataEntityDto) => {
        if (conceptEntity.id) {
            entity.select(conceptEntity.id);
            navigate(`/context/${conceptEntity.id}`);
        }
    };

    if (concepts.loading) {
        return (
            <Box p="md">
                <Loader size="sm" />
            </Box>
        );
    }

    // Show message if no concepts at all
    if (concepts.data.length === 0) {
        return (
            <Box p="md">
                <Text size="sm" c="dimmed">No contexts found. Contexts will appear here once they are added to the metadata.</Text>
            </Box>
        );
    }

    return (
        <Stack gap="md">
            {/* Categories Section */}
            <Box>
                <Group gap="xs" mb={8}>
                    <TbCategory size={16} />
                    <Text size="sm" fw={500} c="dimmed">Categories</Text>
                </Group>
                {concepts.categories.length === 0 ? (
                    <Text size="sm" c="dimmed" mt={8}>No categories found</Text>
                ) : (
                    <Stack gap={0}>
                        {concepts.categories.map((category) => (
                            <ClickableItem
                                key={category}
                                selected={selectedCategory === category}
                                onClick={() => handleCategoryClick(category)}
                            >
                                <Group gap="xs">
                                    <TbCategory size={14} />
                                    <Text size="sm">{category}</Text>
                                    <Badge size="xs" variant="light">
                                        {concepts.getByCategory(category).length}
                                    </Badge>
                                </Group>
                            </ClickableItem>
                        ))}
                    </Stack>
                )}
            </Box>

            {/* Tags Section */}
            <Box>
                <Group gap="xs" mb={8}>
                    <TbTag size={16} />
                    <Text size="sm" fw={500} c="dimmed">Tags</Text>
                </Group>
                {concepts.tags.length === 0 ? (
                    <Text size="sm" c="dimmed" mt={8}>No tags found</Text>
                ) : (
                    <Stack gap={0}>
                        {concepts.tags.map((tag) => (
                            <ClickableItem
                                key={tag}
                                selected={selectedTag === tag}
                                onClick={() => handleTagClick(tag)}
                            >
                                <Group gap="xs">
                                    <TbTag size={14} />
                                    <Text size="sm">#{tag}</Text>
                                    <Badge size="xs" variant="light">
                                        {concepts.getByTag(tag).length}
                                    </Badge>
                                </Group>
                            </ClickableItem>
                        ))}
                    </Stack>
                )}
            </Box>

            {/* Filtered Concepts List */}
            <Box mt="sm">
                <Text size="sm" fw={500} c="dimmed" mb={8}>
                    {selectedCategory 
                        ? `Contexts in "${selectedCategory}"` 
                        : selectedTag 
                        ? `Contexts tagged "${selectedTag}"` 
                        : "All Contexts"}
                </Text>
                {filteredConcepts.length === 0 ? (
                    <Text size="sm" c="dimmed" mt={8}>No contexts found</Text>
                ) : (
                    <Stack gap={0}>
                        {filteredConcepts.map((conceptEntity) => {
                            const conceptNames = getConceptNames(conceptEntity);
                            const isSelected = entity.selected?.id === conceptEntity.id;
                            
                            return (
                                <ClickableItem
                                    key={conceptEntity.id}
                                    selected={isSelected}
                                    onClick={() => handleConceptClick(conceptEntity)}
                                >
                                    <Group gap="xs">
                                        <TbFocusCentered size={14} />
                                        <Text size="sm" fw={isSelected ? 600 : 400}>
                                            {conceptNames.length > 0 ? conceptNames.join(', ') : conceptEntity.id}
                                        </Text>
                                    </Group>
                                </ClickableItem>
                            );
                        })}
                    </Stack>
                )}
            </Box>
        </Stack>
    );
}
