/**
 * Context Sidebar Content Component
 * 
 * Content displayed in the sidebar when on context routes.
 * Uses the MetadataProvider from ContextLayout.
 */
import { Stack } from "@mantine/core";
import ContextView from "./ContextView";
import ScopeSelector from "../data-model/components/ScopeSelector";
import { MetadataProvider, useMetadataContext } from "../data-model/MetadataProvider";

function ContextSidebarInner() {
    const { scope } = useMetadataContext();

    return (
        <Stack gap="md">
            <ContextView />
            <ScopeSelector
                currentScope={scope.current}
                onScopeChange={scope.set}
                collapsed={false}
            />
        </Stack>
    );
}

export function ContextSidebarContent() {
    return (
        <MetadataProvider>
            <ContextSidebarInner />
        </MetadataProvider>
    );
}
