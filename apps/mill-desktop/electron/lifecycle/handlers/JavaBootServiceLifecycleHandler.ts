import { ServiceDefinition } from "../../../shared/service";
import {
  ServiceLifecycleContext,
  SpawnBasedLifecycleHandler,
  SpawnRequest
} from "../ServiceLifecycleHandler";

export class JavaBootServiceLifecycleHandler extends SpawnBasedLifecycleHandler {
  readonly serviceType = "javaBoot" as const;

  check(): { ok: boolean; message?: string } {
    const ok = this.commandExists("java");
    return ok ? { ok: true } : { ok: false, message: "Java runtime is not installed or not in PATH." };
  }

  protected buildSpawnRequest(definition: ServiceDefinition, context: ServiceLifecycleContext): SpawnRequest {
    const config = definition.lifecycle?.start ?? {};
    const jarPath = config.jarPath as string | undefined;
    if (!jarPath) {
      throw new Error(`Service '${definition.id}' requires lifecycle.start.jarPath`);
    }

    const command = "java";
    const jvmArgs = (config.jvmArgs as string[]) ?? [];
    const appArgs = (config.appArgs as string[]) ?? [];
    const args = [...jvmArgs, "-jar", context.resolveWorkspacePath(jarPath), ...appArgs];
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
