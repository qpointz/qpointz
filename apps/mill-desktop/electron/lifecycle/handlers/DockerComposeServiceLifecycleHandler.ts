import { ServiceDefinition } from "../../../shared/service";
import {
  ServiceLifecycleContext,
  SpawnBasedLifecycleHandler,
  SpawnRequest
} from "../ServiceLifecycleHandler";

export class DockerComposeServiceLifecycleHandler extends SpawnBasedLifecycleHandler {
  readonly serviceType = "docker-compose" as const;

  check(): { ok: boolean; message?: string } {
    if (this.commandExists("docker", ["compose", "version"])) {
      return { ok: true };
    }
    if (this.commandExists("docker-compose")) {
      return { ok: true };
    }
    return { ok: false, message: "Docker Compose is not installed or not in PATH." };
  }

  protected buildSpawnRequest(definition: ServiceDefinition, context: ServiceLifecycleContext): SpawnRequest {
    const config = definition.lifecycle?.start ?? {};
    const configuredCommand = config.command as string | undefined;
    const configuredArgs = (config.args as string[]) ?? [];

    let command = configuredCommand ?? "docker";
    let args = configuredArgs;
    if (!configuredCommand) {
      args = ["compose", ...configuredArgs];
    }

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
