import { Anchor, Box, Button, Center, Divider, Fieldset, Group, Loader, Paper, SimpleGrid, Stack, Text, TextInput } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useCallback, useEffect, useMemo, useState } from "react";
import { RecentWorkspace, ServiceViewModel, WorkspaceSettings } from "../shared/service";
import { AppHeader } from "./components/layout/AppHeader";
import { AppShell } from "./components/layout/AppShell";
import { ServiceCard } from "./components/service/ServiceCard";
import { StoppedServiceRow } from "./components/service/StoppedServiceRow";

export function App() {
  const [services, setServices] = useState<ServiceViewModel[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [workspaceDir, setWorkspaceDir] = useState("");
  const [workspaceSettings, setWorkspaceSettings] = useState<WorkspaceSettings>({
    general: { workspaceId: "", workspaceSlug: "" },
    ai: { provider: "", apiKey: "", chatModel: "", embeddingModel: "" }
  });
  const [recentWorkspaces, setRecentWorkspaces] = useState<RecentWorkspace[]>([]);
  const [activeView, setActiveView] = useState<"services" | "workspaceSettings">("services");

  const getApi = useCallback(() => {
    const api = window.controlPanel;
    if (!api) {
      throw new Error("Electron IPC bridge is unavailable. Restart dev mode and try again.");
    }
    return api;
  }, []);

  const loadServices = useCallback(async () => {
    try {
      const [workspace, payload] = await Promise.all([
        getApi().getWorkspace(),
        getApi().listServices()
      ]);
      const recent = await getApi().getRecentWorkspaces();
      setWorkspaceDir(workspace.directory);
      setWorkspaceSettings(workspace.settings);
      setServices(payload);
      setRecentWorkspaces(recent);
    } catch (error) {
      notifications.show({
        title: "Failed to load services",
        message: error instanceof Error ? error.message : String(error),
        color: "red"
      });
    } finally {
      setLoading(false);
    }
  }, [getApi]);

  const refreshStatuses = useCallback(async () => {
    try {
      const payload = await getApi().refreshStatuses();
      setServices(payload);
    } catch {
      // Ignore transient refresh errors to avoid noisy notifications.
    }
  }, [getApi]);

  useEffect(() => {
    void loadServices();
  }, [loadServices]);

  useEffect(() => {
    const timer = setInterval(() => {
      void refreshStatuses();
    }, 2000);
    return () => clearInterval(timer);
  }, [refreshStatuses]);

  const withBusy = useCallback(async (work: () => Promise<void>) => {
    try {
      setBusy(true);
      await work();
    } catch (error) {
      notifications.show({
        title: "Operation failed",
        message: error instanceof Error ? error.message : String(error),
        color: "red"
      });
    } finally {
      setBusy(false);
      await refreshStatuses();
      const recent = await getApi().getRecentWorkspaces();
      setRecentWorkspaces(recent);
    }
  }, [refreshStatuses]);

  const runningCount = useMemo(
    () => services.filter((item) => item.status === "running" || item.status === "starting").length,
    [services]
  );

  const startService = useCallback(
    async (serviceId: string) => {
      await withBusy(async () => {
        const updated = await getApi().startService(serviceId);
        setServices((prev) => prev.map((service) => (service.id === serviceId ? updated : service)));
      });
    },
    [getApi, withBusy]
  );

  const stopService = useCallback(
    async (serviceId: string) => {
      await withBusy(async () => {
        const updated = await getApi().stopService(serviceId);
        setServices((prev) => prev.map((service) => (service.id === serviceId ? updated : service)));
      });
    },
    [getApi, withBusy]
  );

  const startAll = useCallback(async () => {
    await withBusy(async () => {
      const updated = await getApi().startAll();
      setServices(updated);
    });
  }, [getApi, withBusy]);

  const stopAll = useCallback(async () => {
    await withBusy(async () => {
      const updated = await getApi().stopAll();
      setServices(updated);
    });
  }, [getApi, withBusy]);

  const chooseWorkspace = useCallback(async () => {
    await withBusy(async () => {
      const workspace = await getApi().chooseWorkspace();
      setWorkspaceDir(workspace.directory);
      setWorkspaceSettings(workspace.settings);
      const refreshed = await getApi().listServices();
      setServices(refreshed);
    });
  }, [getApi, withBusy]);

  const reloadWorkspace = useCallback(async () => {
    const confirmed = await getApi().showConfirm("All services will be stopped. Continue workspace reload?");
    if (!confirmed) {
      return;
    }
    await withBusy(async () => {
      const reloaded = await getApi().reloadWorkspaceServices();
      setServices(reloaded);
      const workspace = await getApi().getWorkspace();
      setWorkspaceDir(workspace.directory);
      setWorkspaceSettings(workspace.settings);
      const recent = await getApi().getRecentWorkspaces();
      setRecentWorkspaces(recent);
      notifications.show({
        title: "Workspace reloaded",
        message: "Services were stopped and reloaded from .mill/services.manifest.yaml",
        color: "teal"
      });
    });
  }, [getApi, withBusy]);

  const importWorkspaceServices = useCallback(async () => {
    const url = await getApi().showPrompt("Import services", "Paste URL to services definition JSON:");
    if (!url) {
      return;
    }
    await withBusy(async () => {
      const imported = await getApi().importWorkspaceServicesFromUrl(url);
      setServices(imported);
      notifications.show({
        title: "Services imported",
        message: "Imported docker/docker-compose services into workspace .mill file.",
        color: "teal"
      });
    });
  }, [getApi, withBusy]);

  const selectRecentWorkspace = useCallback(async (directory: string) => {
    await withBusy(async () => {
      const workspace = await getApi().switchToRecentWorkspace(directory);
      setWorkspaceDir(workspace.directory);
      setWorkspaceSettings(workspace.settings);
      const refreshed = await getApi().listServices();
      setServices(refreshed);
    });
  }, [getApi, withBusy]);

  const saveWorkspaceSettings = useCallback(async (settings: WorkspaceSettings) => {
    await withBusy(async () => {
      const workspace = await getApi().saveWorkspaceSettings(settings);
      setWorkspaceDir(workspace.directory);
      setWorkspaceSettings(workspace.settings);
      notifications.show({
        title: "Workspace settings saved",
        message: `.mill/settings.json updated for ${workspace.settings.general.workspaceSlug}`,
        color: "teal"
      });
    });
  }, [getApi, withBusy]);

  const openServiceLink = useCallback(async (serviceId: string, url: string) => {
    await getApi().openServiceLink(serviceId, url);
  }, [getApi]);

  const openLog = useCallback(async (serviceId: string) => {
    await getApi().openLog(serviceId);
  }, [getApi]);

  const deleteService = useCallback(async (service: ServiceViewModel) => {
    const isRunning = service.status === "running" || service.status === "starting";
    const confirmed = await getApi().showConfirm(
      isRunning
        ? `Service '${service.name}' is running. It will be stopped and deleted. Continue?`
        : `Delete service '${service.name}' from workspace manifest?`
    );
    if (!confirmed) {
      return;
    }

    await withBusy(async () => {
      const updated = await getApi().deleteService(service.id);
      setServices(updated);
      notifications.show({
        title: "Service deleted",
        message: `${service.name} was removed from workspace service definitions.`,
        color: "teal"
      });
    });
  }, [getApi, withBusy]);

  const cleanService = useCallback(async (service: ServiceViewModel) => {
    const confirmed = await getApi().showConfirm(
      `Run clean command for '${service.name}'? The service will be stopped first if running.`
    );
    if (!confirmed) {
      return;
    }
    await withBusy(async () => {
      const updated = await getApi().cleanService(service.id);
      setServices((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
      notifications.show({
        title: "Service cleaned",
        message: `Clean command executed for ${service.name}.`,
        color: "teal"
      });
    });
  }, [getApi, withBusy]);

  if (loading) {
    return (
      <Center h="100vh">
        <Stack gap="xs" align="center">
          <Loader />
          <Text size="sm" c="dimmed">
            Loading service catalog...
          </Text>
        </Stack>
      </Center>
    );
  }

  return (
    <AppShell
      header={
        <AppHeader
          runningCount={runningCount}
          totalCount={services.length}
          onStartAll={startAll}
          onStopAll={stopAll}
          onRefresh={refreshStatuses}
          onChooseWorkspace={chooseWorkspace}
          onReloadWorkspace={reloadWorkspace}
          onImportWorkspaceServices={importWorkspaceServices}
          onSelectRecentWorkspace={selectRecentWorkspace}
          onOpenWorkspaceSettings={() => setActiveView("workspaceSettings")}
          onOpenLink={(url) => void openServiceLink("", url)}
          workspaceDir={workspaceDir}
          workspaceSlug={workspaceSettings.general.workspaceSlug}
          recentWorkspaces={recentWorkspaces}
          busy={busy}
          settingsActive={activeView === "workspaceSettings"}
        />
      }
    >
      {activeView === "workspaceSettings" ? (
        <Paper withBorder p="lg" shadow="xs">
          <Stack>
            <Group justify="space-between" align="center">
              <Group gap="sm">
                <Text fw={650} size="lg">
                  Workspace Settings
                </Text>
                <Anchor
                  component="button"
                  type="button"
                  size="sm"
                  onClick={() => void openServiceLink("", "https://docs.qpointz.io/latest/launcher/settings")}
                >
                  Help
                </Anchor>
              </Group>
              <Button variant="default" onClick={() => setActiveView("services")} disabled={busy}>
                Back
              </Button>
            </Group>
            <Text size="sm" c="dimmed">
              Settings are saved to <code>{workspaceDir}\.mill\settings.json</code>
            </Text>

            <Fieldset legend="General">
              <Stack gap="sm">
                <TextInput
                  label="Workspace ID"
                  description="Read-only alphanumeric identifier"
                  value={workspaceSettings.general.workspaceId}
                  disabled
                />
                <TextInput
                  label="Workspace Slug"
                  value={workspaceSettings.general.workspaceSlug}
                  onChange={(event) =>
                    setWorkspaceSettings((prev) => ({
                      ...prev,
                      general: { ...prev.general, workspaceSlug: event.currentTarget.value }
                    }))
                  }
                  disabled={busy}
                />
              </Stack>
            </Fieldset>

            <Fieldset legend="AI">
              <Stack gap="sm">
                <TextInput
                  label="Provider"
                  value={workspaceSettings.ai.provider}
                  onChange={(event) =>
                    setWorkspaceSettings((prev) => ({
                      ...prev,
                      ai: { ...prev.ai, provider: event.currentTarget.value }
                    }))
                  }
                  disabled={busy}
                />
                <TextInput
                  label="API Key"
                  type="password"
                  value={workspaceSettings.ai.apiKey}
                  onChange={(event) =>
                    setWorkspaceSettings((prev) => ({
                      ...prev,
                      ai: { ...prev.ai, apiKey: event.currentTarget.value }
                    }))
                  }
                  disabled={busy}
                />
                <TextInput
                  label="Chat Model"
                  value={workspaceSettings.ai.chatModel}
                  onChange={(event) =>
                    setWorkspaceSettings((prev) => ({
                      ...prev,
                      ai: { ...prev.ai, chatModel: event.currentTarget.value }
                    }))
                  }
                  disabled={busy}
                />
                <TextInput
                  label="Embedding Model"
                  value={workspaceSettings.ai.embeddingModel}
                  onChange={(event) =>
                    setWorkspaceSettings((prev) => ({
                      ...prev,
                      ai: { ...prev.ai, embeddingModel: event.currentTarget.value }
                    }))
                  }
                  disabled={busy}
                />
              </Stack>
            </Fieldset>

            <Group justify="flex-end">
              <Button onClick={() => void saveWorkspaceSettings(workspaceSettings)} disabled={busy}>
                Save Workspace Settings
              </Button>
            </Group>
          </Stack>
        </Paper>
      ) : services.length === 0 ? (
        <Center h="100%">
          <Stack gap={2} align="center">
            <Text fw={600}>No services configured</Text>
            <Text size="sm" c="dimmed">
              Add entries to resources/services.manifest.yaml and package platform-specific binaries.
            </Text>
          </Stack>
        </Center>
      ) : (
        <ServiceSplitView
          services={services}
          onStart={startService}
          onStop={stopService}
          onOpenLink={openServiceLink}
          onOpenLog={openLog}
          onDelete={deleteService}
          onClean={cleanService}
          busy={busy}
        />
      )}
    </AppShell>
  );
}

interface ServiceSplitViewProps {
  services: ServiceViewModel[];
  onStart: (serviceId: string) => Promise<void>;
  onStop: (serviceId: string) => Promise<void>;
  onOpenLink: (serviceId: string, url: string) => Promise<void>;
  onOpenLog: (serviceId: string) => Promise<void>;
  onDelete: (service: ServiceViewModel) => Promise<void>;
  onClean: (service: ServiceViewModel) => Promise<void>;
  busy: boolean;
}

function ServiceSplitView({ services, onStart, onStop, onOpenLink, onOpenLog, onDelete, onClean, busy }: ServiceSplitViewProps) {
  const activeStatuses = new Set(["running", "starting", "stopping"]);
  const active = services.filter((s) => activeStatuses.has(s.status));
  const inactive = services.filter((s) => !activeStatuses.has(s.status));

  return (
    <Stack gap="lg">
      {active.length > 0 && (
        <Box>
          <Text size="sm" fw={600} c="dimmed" mb="sm">
            Active ({active.length})
          </Text>
          <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }} spacing="md" verticalSpacing="md">
            {active.map((service) => (
              <ServiceCard
                key={service.id}
                service={service}
                onStart={onStart}
                onStop={onStop}
                onOpenLink={onOpenLink}
                onOpenLog={onOpenLog}
                onDelete={onDelete}
                onClean={onClean}
                busy={busy}
              />
            ))}
          </SimpleGrid>
        </Box>
      )}

      {active.length > 0 && inactive.length > 0 && <Divider />}

      {inactive.length > 0 && (
        <Box>
          <Text size="sm" fw={600} c="dimmed" mb="sm">
            Available ({inactive.length})
          </Text>
          <Stack gap="xs">
            {inactive.map((service) => (
              <StoppedServiceRow
                key={service.id}
                service={service}
                onStart={onStart}
                onOpenLink={onOpenLink}
                onOpenLog={onOpenLog}
                onDelete={onDelete}
                onClean={onClean}
                busy={busy}
              />
            ))}
          </Stack>
        </Box>
      )}
    </Stack>
  );
}
