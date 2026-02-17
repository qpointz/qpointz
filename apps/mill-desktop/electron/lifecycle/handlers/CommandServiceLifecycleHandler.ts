import { ServiceDefinition } from "../../../shared/service";
import {
  ServiceLifecycleContext,
  SpawnBasedLifecycleHandler,
  SpawnRequest
} from "../ServiceLifecycleHandler";

export class CommandServiceLifecycleHandler extends SpawnBasedLifecycleHandler {
  readonly serviceType = "command" as const;

  check(definition: ServiceDefinition, context: ServiceLifecycleContext): { ok: boolean; message?: string } {
    const config = definition.lifecycle?.start;
    const command = (config?.command as string) ?? "echo";
    const resolved = context.resolveWorkspacePath(command);
    const ok = this.commandResolvable(resolved);
    return ok
      ? { ok: true }
      : { ok: false, message: `Missing command prerequisite: ${command}` };
  }

  protected buildSpawnRequest(definition: ServiceDefinition, context: ServiceLifecycleContext): SpawnRequest {
    const config = definition.lifecycle?.start ?? {};
    const command = (config.command as string) ?? "echo";
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
