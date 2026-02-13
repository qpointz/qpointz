package io.qpointz.mill.ai.capabilities;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

public interface Capability {

    List<Advisor> getAdvisors();

    List<Object> getTools();

    List<ToolCallback> getToolCalbacks();


}
