/**
 * Concepts View Component
 * 
 * Displays concepts organized by categories and tags.
 * Allows filtering and selection of concepts.
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
} from "@mantine/core";
import { TbTag, TbCategory, TbBulb } from "react-icons/tb";
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

export default function ConceptsView() {
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
            navigate(`/concepts/${conceptEntity.id}`);
        }
    };

    if (concepts.loading) {
        return (
            <Box p="md" style={{ display: 'flex', justifyContent: 'center' }}>
                <Loader size="sm" />
            </Box>
        );
    }

    // Show message if no concepts at all
    if (concepts.data.length === 0) {
        return (
            <Box p="md">
                <Text size="sm" c="dimmed">No concepts found. Concepts will appear here once they are added to the metadata.</Text>
            </Box>
        );
    }

    return (
        <Stack gap="md">
            {/* Categories Section */}
            <Box>
                <Group gap="xs" mb={8}>
                    <TbCategory size={16} />
                    <Text size="sm" fw={500} color="gray.7">Categories</Text>
                </Group>
                {concepts.categories.length === 0 ? (
                    <Text size="sm" c="dimmed" mt={8}>No categories found</Text>
                ) : (
                    <Box>
                        {concepts.categories.map((category) => (
                            <Box
                                key={category}
                                p={6}
                                mb={4}
                                style={{
                                    borderRadius: 6,
                                    background: selectedCategory === category ? 'var(--mantine-color-blue-0)' : undefined,
                                    cursor: 'pointer',
                                    transition: 'background 0.2s',
                                }}
                                onClick={() => handleCategoryClick(category)}
                                onMouseEnter={(e) => {
                                    if (selectedCategory !== category) {
                                        e.currentTarget.style.background = 'var(--mantine-color-gray-1)';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (selectedCategory !== category) {
                                        e.currentTarget.style.background = '';
                                    }
                                }}
                            >
                                <Group gap="xs">
                                    <TbCategory size={14} />
                                    <Text size="sm">{category}</Text>
                                    <Badge size="xs" variant="light">
                                        {concepts.getByCategory(category).length}
                                    </Badge>
                                </Group>
                            </Box>
                        ))}
                    </Box>
                )}
            </Box>

            {/* Tags Section */}
            <Box>
                <Group gap="xs" mb={8}>
                    <TbTag size={16} />
                    <Text size="sm" fw={500} color="gray.7">Tags</Text>
                </Group>
                {concepts.tags.length === 0 ? (
                    <Text size="sm" c="dimmed" mt={8}>No tags found</Text>
                ) : (
                    <Box>
                        {concepts.tags.map((tag) => (
                            <Box
                                key={tag}
                                p={6}
                                mb={4}
                                style={{
                                    borderRadius: 6,
                                    background: selectedTag === tag ? 'var(--mantine-color-blue-0)' : undefined,
                                    cursor: 'pointer',
                                    transition: 'background 0.2s',
                                }}
                                onClick={() => handleTagClick(tag)}
                                onMouseEnter={(e) => {
                                    if (selectedTag !== tag) {
                                        e.currentTarget.style.background = 'var(--mantine-color-gray-1)';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (selectedTag !== tag) {
                                        e.currentTarget.style.background = '';
                                    }
                                }}
                            >
                                <Group gap="xs">
                                    <TbTag size={14} />
                                    <Text size="sm">#{tag}</Text>
                                    <Badge size="xs" variant="light">
                                        {concepts.getByTag(tag).length}
                                    </Badge>
                                </Group>
                            </Box>
                        ))}
                    </Box>
                )}
            </Box>

            {/* Filtered Concepts List */}
            <Box mt={15}>
                <Text size="sm" fw={500} color="gray.7" mb={8}>
                    {selectedCategory 
                        ? `Concepts in "${selectedCategory}"` 
                        : selectedTag 
                        ? `Concepts tagged "${selectedTag}"` 
                        : "All Concepts"}
                </Text>
                {filteredConcepts.length === 0 ? (
                    <Text size="sm" c="dimmed" mt={8}>No concepts found</Text>
                ) : (
                    <Stack gap={4}>
                        {filteredConcepts.map((conceptEntity) => {
                            const conceptNames = getConceptNames(conceptEntity);
                            const isSelected = entity.selected?.id === conceptEntity.id;
                            
                            return (
                                <Box
                                    key={conceptEntity.id}
                                    p={6}
                                    mb={2}
                                    style={{
                                        borderRadius: 6,
                                        background: isSelected ? 'var(--mantine-color-blue-0)' : undefined,
                                        cursor: 'pointer',
                                        transition: 'background 0.2s',
                                    }}
                                    onClick={() => handleConceptClick(conceptEntity)}
                                    onMouseEnter={(e) => {
                                        if (!isSelected) {
                                            e.currentTarget.style.background = 'var(--mantine-color-gray-1)';
                                        }
                                    }}
                                    onMouseLeave={(e) => {
                                        if (!isSelected) {
                                            e.currentTarget.style.background = '';
                                        }
                                    }}
                                >
                                    <Group gap="xs">
                                        <TbBulb size={14} />
                                        <Text size="sm" fw={isSelected ? 600 : 400}>
                                            {conceptNames.length > 0 ? conceptNames.join(', ') : conceptEntity.id}
                                        </Text>
                                    </Group>
                                </Box>
                            );
                        })}
                    </Stack>
                )}
            </Box>
        </Stack>
    );
}

