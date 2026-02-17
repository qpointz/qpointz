import { ServiceDefinition } from "../../../shared/service";
import {
  ServiceLifecycleContext,
  SpawnBasedLifecycleHandler,
  SpawnRequest
} from "../ServiceLifecycleHandler";

export class DockerServiceLifecycleHandler extends SpawnBasedLifecycleHandler {
  readonly serviceType = "docker" as const;

  check(): { ok: boolean; message?: string } {
    const ok = this.commandExists("docker");
    return ok ? { ok: true } : { ok: false, message: "Docker CLI is not installed or not in PATH." };
  }

  protected buildSpawnRequest(definition: ServiceDefinition, context: ServiceLifecycleContext): SpawnRequest {
    const config = definition.lifecycle?.start ?? {};
    const command = (config.command as string) ?? "docker";
    const args = (config.args as string[]) ?? [];
    const cwd = this.resolveCwd(definition, context, config.cwd as string | undefined);
    const env = {
      ...process.env,
      ...this.buildOrchestrationEnv(context),
      ...definition.env,
      ...(config.env as Record<string, string> | undefined)
    };
    const shell = (config.shell as boolean | undefined) ?? context.defaultShell;

    return {
      command: context.resolveWorkspacePath(command),
      args,
      cwd,
      env,
      shell
    };
  }
}
