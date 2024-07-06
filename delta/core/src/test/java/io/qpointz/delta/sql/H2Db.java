package io.qpointz.delta.sql;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.RunScript;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static H2Db createFromResource(String path) throws ClassNotFoundException {
        val schemaName = "db-" + path
                .replace(".sql", "")
                .replace("/", "-");

        val reader = scriptFromResource(path);
        return create(schemaName, reader);
    }

    public static H2Db create(String schemaName, Reader scriptReader) throws ClassNotFoundException {
        final var driverName = "org.h2.Driver";
        Class.forName(driverName);
        //Class.forName("org.apache.calcite.jdbc.Driver");
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

    @SneakyThrows
    public ResultSet query(String sql) {
        val con = connect();
        val stmt = con.prepareStatement(sql);
        return stmt.executeQuery();
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
