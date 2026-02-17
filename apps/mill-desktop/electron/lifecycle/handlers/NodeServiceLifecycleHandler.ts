import { ServiceDefinition } from "../../../shared/service";
import {
  ServiceLifecycleContext,
  SpawnBasedLifecycleHandler,
  SpawnRequest
} from "../ServiceLifecycleHandler";

export class NodeServiceLifecycleHandler extends SpawnBasedLifecycleHandler {
  readonly serviceType = "node" as const;

  check(): { ok: boolean; message?: string } {
    const ok = this.commandExists("node");
    return ok ? { ok: true } : { ok: false, message: "Node.js runtime is not installed or not in PATH." };
  }

  protected buildSpawnRequest(definition: ServiceDefinition, context: ServiceLifecycleContext): SpawnRequest {
    const config = definition.lifecycle?.start ?? {};
    const scriptPath = config.scriptPath as string | undefined;
    if (!scriptPath) {
      throw new Error(`Service '${definition.id}' requires lifecycle.start.scriptPath`);
    }

    const command = "node";
    const nodeArgs = (config.nodeArgs as string[]) ?? [];
    const scriptArgs = (config.scriptArgs as string[]) ?? [];
    const args = [...nodeArgs, context.resolveWorkspacePath(scriptPath), ...scriptArgs];
    const cwd = this.resolveCwd(definition, context, config.cwd as string | undefined);
    const env = {
      ...process.env,
      ...this.buildOrchestrationEnv(context),
      ...definition.env,
      ...(config.env as Record<string, string> | undefined)
    };

    return {
      command,
      args,
      cwd,
      env,
      shell: false
    };
  }
}
