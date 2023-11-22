package io.qpointz.rapids.testing;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.h2.tools.RunScript;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
@Builder(access = AccessLevel.PRIVATE)
public class H2Db implements java.io.Closeable {

    @Getter
    private String userName;

    @Getter
    private String password;

    @Getter
    private String url;

    @Getter
    private String driver;

    @Getter
    private String schemaName;

    public static H2Db create(String schemaName, Reader scriptReader) throws ClassNotFoundException {
        final var driverName = "org.h2.Driver";
        Class.forName(driverName);
        Class.forName("org.apache.calcite.jdbc.Driver");
        final var url = String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1", schemaName);
        final var username = "test";
        final var password = "test";
        final var db = H2Db.builder()
                .schemaName(schemaName)
                .userName(username)
                .password(password)
                .driver(driverName)
                .url(url)
                .build();
        db.execScript(scriptReader);
        return db;
    }

    public static Reader scriptFromResource(String path) {
        final var stream = H2Db.class.getClassLoader().getResourceAsStream(path);
        return new InputStreamReader(stream);
    }

    @SneakyThrows
    public Connection connect() {
        return DriverManager.getConnection(this.getUrl() , this.getUserName(), this.getPassword());
    }

    @SneakyThrows
    public void execScript(Reader reader) {
        RunScript.execute(connect(), reader);
    }

    public SchemaFactory schemaFactory() {
        return JdbcSchema.Factory.INSTANCE;
    }

    public Map<String, Object> schemaOperand() {
        return Map.of(
                "jdbcUrl", this.getUrl(),
                "jdbcUser", this.getUserName(),
                "jdbcPassword", this.getPassword(),
                "jdbcDriver", this.getDriver()
        );
    }

    public Schema createSchema(SchemaPlus parentSchema) {
        return schemaFactory().create(parentSchema, this.getSchemaName(), this.schemaOperand());
    }

    @Override
    public void close() throws IOException {
        try {
            connect()
                    .prepareStatement("SHUTDOWN")
                    .execute();
        } catch (SQLException e) {
            throw new IOException("Can't be closed");
        }
    }
}
