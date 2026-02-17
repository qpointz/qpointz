import { ServiceDefinition, ServiceType } from "../../shared/service";
import { ServiceLifecycleHandler } from "./ServiceLifecycleHandler";
import { CommandServiceLifecycleHandler } from "./handlers/CommandServiceLifecycleHandler";
import { DockerComposeServiceLifecycleHandler } from "./handlers/DockerComposeServiceLifecycleHandler";
import { DockerServiceLifecycleHandler } from "./handlers/DockerServiceLifecycleHandler";
import { JavaBootServiceLifecycleHandler } from "./handlers/JavaBootServiceLifecycleHandler";
import { NodeServiceLifecycleHandler } from "./handlers/NodeServiceLifecycleHandler";
import { PythonServiceLifecycleHandler } from "./handlers/PythonServiceLifecycleHandler";

export class ServiceLifecycleFactory {
  private readonly handlers = new Map<ServiceType, ServiceLifecycleHandler>([
    ["command", new CommandServiceLifecycleHandler()],
    ["node", new NodeServiceLifecycleHandler()],
    ["javaBoot", new JavaBootServiceLifecycleHandler()],
    ["docker", new DockerServiceLifecycleHandler()],
    ["docker-compose", new DockerComposeServiceLifecycleHandler()],
    ["python", new PythonServiceLifecycleHandler()]
  ]);

  getHandler(definition: ServiceDefinition): ServiceLifecycleHandler {
    const serviceType = definition.serviceType ?? "command";
    return this.handlers.get(serviceType)
      ?? this.handlers.get("command")!;
  }

  isKnownType(serviceType: string): boolean {
    return this.handlers.has(serviceType as ServiceType);
  }
}
