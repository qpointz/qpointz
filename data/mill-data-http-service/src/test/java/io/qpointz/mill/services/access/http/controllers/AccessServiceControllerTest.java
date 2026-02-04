package io.qpointz.mill.services.access.http.controllers;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.services.access.http.ProtobufUtils;
import io.qpointz.mill.services.access.http.components.MessageHelper;
import io.qpointz.mill.utils.JsonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;


@WebAppConfiguration
@SpringBootTest(classes = {AccessServiceController.class})
@EnableAutoConfiguration
@ActiveProfiles("test-cmart")
@ComponentScan("io.qpointz.mill")
@Slf4j
class AccessServiceControllerTest {

    private MockMvc mockMvc;

    public AccessServiceControllerTest(@Autowired WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    void roundTrip(String location, Supplier<Message.Builder> messageBuiler, Function<byte[],Message> parse) throws Exception {
        val payload  = MessageHelper.asJson(messageBuiler.get().build());
        val jsonResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post(location)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .accept(MediaType.APPLICATION_JSON)
        ).andReturn();

        assertEquals(200, jsonResult.getResponse().getStatus());
        log.info("JSON<->JSON: `{}` request with: \n   payload:{}\n   returns:{}",
                location,
                payload,
                jsonResult.getResponse().getContentAsString());


        val content =messageBuiler.get().build().toByteArray();
        val protoResult = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post(location)
                .contentType(MediaType.APPLICATION_PROTOBUF)
                .content(content)
                .accept(MediaType.APPLICATION_PROTOBUF)
        ).andReturn();

        assertEquals(200, protoResult.getResponse().getStatus());

        val parsed = parse.apply(protoResult.getResponse().getContentAsByteArray());
        log.info("PROTOBUF<->PROTOBUF: `{}` request with: \n   payload:{}\n   returns:{}",
                location,
                payload,
                parsed.toString());


    }

    @Test
    void listSchemas() throws Exception {
        roundTrip("/services/jet/ListSchemas",
                ListSchemasRequest::newBuilder,
                k -> {try {
                    return ListSchemasRequest.parseFrom(k);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }});
    }

    @Test
    void handshakeJson() throws Exception {
        roundTrip("/services/jet/Handshake",
                HandshakeRequest::newBuilder,
                k -> { try {
                    return HandshakeResponse.parseFrom(k);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }});
    }

    @Test
    void getSchemaRoundTrip() throws Exception {
        roundTrip("/services/jet/GetSchema",
                () -> GetSchemaRequest.newBuilder().setSchemaName("ts"),
                k -> { try {
                    return GetSchemaResponse.parseFrom(k);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }});
    }

    @Test
    void parseSqlRoundTrip() throws Exception {
        val request = ParseSqlRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder()
                        .setSql("SELECT * FROM `ts`.`CLIENT`")
                        .build());

        roundTrip("/services/jet/ParseSql",
                () -> request,
                k -> { try {
                    return ParseSqlResponse.parseFrom(k);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }});
    }

    @Test
    void submitQueryRoundTrip() throws Exception {
        val request = QueryRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder()
                        .setSql("SELECT * FROM `ts`.`CLIENT`")
                        .build());

        roundTrip("/services/jet/SubmitQuery",
                () -> request,
                k -> { try {
                    return ParseSqlResponse.parseFrom(k);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }});
    }


    @Test
    void fetchQueryResultRoundTrip() throws Exception {
        val request = QueryRequest.newBuilder()
                .setStatement(SQLStatement.newBuilder()
                        .setSql("SELECT * FROM `ts`.`CLIENT`")
                        .build())
                .build();
        val submit = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/services/jet/SubmitQuery")
                .contentType(MediaType.APPLICATION_PROTOBUF)
                .content(request.toByteArray())
                .accept(MediaType.APPLICATION_PROTOBUF)
        ).andReturn();

        QueryResultResponse response = QueryResultResponse
                .parseFrom(submit
                        .getResponse()
                        .getContentAsByteArray());


        val fetchBuilder = QueryResultRequest.newBuilder()
                        .setPagingId(response.getPagingId());

        roundTrip("/services/jet/FetchQueryResult",
                () -> fetchBuilder,
                k -> { try {
                    return ParseSqlResponse.parseFrom(k);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }});
    }


}