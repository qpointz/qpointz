import { ServiceDefinition } from "../../../shared/service";
import {
  ServiceLifecycleContext,
  SpawnBasedLifecycleHandler,
  SpawnRequest
} from "../ServiceLifecycleHandler";

export class PythonServiceLifecycleHandler extends SpawnBasedLifecycleHandler {
  readonly serviceType = "python" as const;

  check(definition: ServiceDefinition, context: ServiceLifecycleContext): { ok: boolean; message?: string } {
    const config = definition.lifecycle?.start;
    const command = (config?.command as string) ?? "python";
    const resolved = context.resolveWorkspacePath(command);
    const ok = this.commandResolvable(resolved);
    return ok
      ? { ok: true }
      : { ok: false, message: `Missing command prerequisite: ${command}` };
  }

  protected buildSpawnRequest(definition: ServiceDefinition, context: ServiceLifecycleContext): SpawnRequest {
    const config = definition.lifecycle?.start ?? {};
    const command = (config.command as string) ?? "python";
    const scriptPath = config.scriptPath as string | undefined;
    const scriptArgs = (config.args as string[]) ?? [];
    const pythonArgs = (config.pythonArgs as string[]) ?? [];
    const cwd = this.resolveCwd(definition, context, config.cwd as string | undefined);
    const env = {
      ...process.env,
      ...this.buildOrchestrationEnv(context),
      ...definition.env,
      ...(config.env as Record<string, string> | undefined)
    };
    const shell = (config.shell as boolean | undefined) ?? context.defaultShell;

    const args: string[] = [...pythonArgs];
    if (scriptPath) {
      args.push(context.resolveWorkspacePath(scriptPath));
    }
    args.push(...scriptArgs);

    return {
      command: context.resolveWorkspacePath(command),
      args,
      cwd,
      env,
      shell
    };
  }
}
