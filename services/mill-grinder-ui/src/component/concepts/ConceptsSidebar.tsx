import { Box, ScrollArea, Button } from "@mantine/core";
import { useMantineTheme } from "@mantine/core";
import { useMediaQuery } from "@mantine/hooks";
import { TbLayoutSidebarRightExpand, TbLayoutSidebarLeftExpand } from "react-icons/tb";
import { useState } from "react";
import TopNavigation from "../data-model/components/TopNavigation";
import SidebarNavSection from "../data-model/components/SidebarNavSection";
import ScopeSelector from "../data-model/components/ScopeSelector";
import ConceptsView from "./ConceptsView";
import { useMetadataContext } from "../data-model/MetadataProvider";

export function ConceptsSidebar() {
    const theme = useMantineTheme();
    const { scope } = useMetadataContext();
    const [isCollapsed, setIsCollapsed] = useState(false);
    const isMobile = useMediaQuery('(max-width: 768px)');
    
    // Auto-collapse on mobile only, manual toggle on tablet and desktop
    const shouldCollapse = isMobile || isCollapsed;

    return (
        <Box 
            w={shouldCollapse ? 50 : 350} 
            style={{ 
                height: "100vh", 
                boxSizing: "border-box", 
                borderRight: `1px solid ${theme.colors.gray[3]}`,
                transition: 'width 0.3s ease, border 0.3s ease',
                overflow: 'hidden',
                position: 'relative'
            }}
        >
            {/* Toggle Button - only show on tablet and larger */}
            {!isMobile && (
                <Button
                    variant="subtle"
                    size="xs"
                    pos="absolute"
                    top={10}
                    right={shouldCollapse ? 0 : -5}
                    onClick={() => setIsCollapsed(!isCollapsed)}
                    style={{ 
                        transition: 'right 0.3s ease',
                        zIndex: 10
                    }}
                >
                    {shouldCollapse ? <TbLayoutSidebarLeftExpand size={20} /> : <TbLayoutSidebarRightExpand size={20} />}
                </Button>
            )}

            <ScrollArea type="hover" style={{ minHeight: "100%", height: "100vh" }}>
                <Box m={0} pl={shouldCollapse ? 5 : 10} pr={shouldCollapse ? 5 : 10} pt={40} style={{ width: "100%", boxSizing: "border-box" }}>
                    
                    {/* Top Navigation */}
                    <TopNavigation collapsed={shouldCollapse} />
                    
                    {!shouldCollapse && (
                        <SidebarNavSection collapsed={shouldCollapse}>
                            {/* Concepts View */}
                            <Box mt={15}>
                                <ConceptsView />
                            </Box>

                            {/* Scope Selector */}
                            <ScopeSelector
                                currentScope={scope.current}
                                onScopeChange={scope.set}
                                collapsed={shouldCollapse}
                            />
                        </SidebarNavSection>
                    )}
                </Box>
            </ScrollArea>
        </Box>
    );
}

