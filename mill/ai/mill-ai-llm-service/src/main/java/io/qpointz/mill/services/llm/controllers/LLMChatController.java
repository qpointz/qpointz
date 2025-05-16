package io.qpointz.mill.services.llm.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qpointz.mill.proto.ParseSqlRequest;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.services.MetadataProvider;
import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.services.llm.SqlAgent;
import io.qpointz.mill.sql.RecordReader;
import io.qpointz.mill.sql.RecordReaders;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@ConditionalOnService("data-bot")
@RestController
@RequestMapping("/data-bot")
@CrossOrigin(origins = "http://localhost:5173")
public class LLMChatController {


    private final SqlAgent sqlAgent;
    private final DataOperationDispatcher dataDispatcher;

    LLMChatController(SqlAgent sqlAgent, DataOperationDispatcher dataDispatcher)  {
        log.info("LLM Service. created");
        this.sqlAgent = sqlAgent;
        this.dataDispatcher = dataDispatcher;
    }


    @PostMapping("chat")
    public Map<String,Object> chat(@RequestBody Map<String, String> request) throws JsonProcessingException {
        var resp = new HashMap<String,Object>(request);
        try {
            String question = request.get("query");
            resp  = new HashMap<>(sqlAgent.queryAsMap(question));
            return reply(resp);
        } catch (Exception ex) {
            resp.put("error", ex.getMessage());
            return resp;
        }
    }

    public record ExecResult(List<String> columns, List<List<Object>> rows) {
    }

    private Map<String, Object> reply(Map<String, Object> respone) {
        return execute(respone);
    }

    private HashMap<String, Object> execute(Map<String, Object> agentResponse) {
        val resp = new HashMap<>(agentResponse);
        if (! resp.containsKey("query")) {
            return resp;
        }

        val query = (Map<String,Object>)agentResponse.get("query");

        if (query == null || ! query.containsKey("sql")) {
            return resp;
        }

        val sql = query.get("sql").toString();
        log.debug("Query:{}", query);
        val parsed = dataDispatcher.parseSql(ParseSqlRequest.newBuilder().setStatement(SQLStatement.newBuilder().setSql(sql).build()).build());
        val queryRequest = QueryRequest.newBuilder()
                .setConfig(QueryExecutionConfig.newBuilder().setFetchSize(10000).build())
                .setPlan(parsed.getPlan())
                .build();
        val iterator = dataDispatcher.execute(queryRequest);
        val fields = iterator.schema().getFieldsList().stream()
                .map(k-> k.getName())
                .toList();
        val recordReader = RecordReaders.recordReader(iterator);
        List<List<Object>> data = new ArrayList<>();
        val sz = fields.size();
        while (recordReader.next()) {
            val record = new ArrayList<Object>(sz);
            for (int i=0;i<sz;i++) {
                record.add(recordReader.getObject(i));
            }
            data.add(record);
        }

        resp.put("result", new ExecResult(fields, data));
        return resp;
    }


    @GetMapping("chat")
    public String ping() {
        return "I am data-bot";
    }


}
