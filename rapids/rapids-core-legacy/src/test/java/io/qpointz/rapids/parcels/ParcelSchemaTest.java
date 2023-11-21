package io.qpointz.rapids.parcels;

import org.apache.calcite.DataContext;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.type.SqlTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParcelSchemaTest {


    private CalciteConnection calciteConnection;
    @BeforeEach
    void updateConnection() {
        this.calciteConnection = createConnection();
    }

    private static CalciteConnection createConnection() {
        CalciteConnection result;
        try {
            Class.forName("org.apache.calcite.jdbc.Driver");
            var props = new Properties();
            props.put("quoting","BACK_TICK");
            result = DriverManager.getConnection("jdbc:calcite:", props).unwrap(CalciteConnection.class);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private ParcelSchema mockSchema() {
        var mockTable = new ParcelTable() {

            @Override
            public RelDataType getRowType(RelDataTypeFactory typeFactory) {
                var names = List.of("id", "val1", "val2");
                var types = Stream.of(ParcelDataTypes.integerType(), ParcelDataTypes.stringType(), ParcelDataTypes.stringType())
                        .map(k-> k.asRelDataType(typeFactory))
                        .toList();
                return typeFactory.createStructType(types, names);
            }

            @Override
            public Enumerable<Object[]> scan(DataContext root, List<RexNode> filters) {
                return Linq4j.asEnumerable(List.of(
                        new Object[]{1, "foo", "bar"},
                        new Object[]{2, "john", "dow"}
                ));
            }
        };

        Map<String, ParcelTable> tables = Map.of("mockTable", mockTable);
        var parcel = new Parcel() {
            @Override
            public String getName() {
                return "testParcel";
            }

            @Override
            public void init() {}

            @Override
            public void refresh() {}

            @Override
            public Map<String, ParcelTable> getTables() {
                return tables;
            }
        };
        return new ParcelSchema(this.calciteConnection.getRootSchema(), parcel);
    }
    @Test
    void hasTables() {
        var schema = mockSchema();
        assertEquals(1, schema.getTableMap().entrySet().size());
        assertNotNull(mockSchema().getTableMap().get("mockTable"));
    }

    @Test
    void typesMapped() {
        var typeFactory = this.calciteConnection.getTypeFactory();
        var rowType = mockSchema()
                .getTable("mockTable")
                .getRowType(typeFactory);
        assertEquals(typeFactory.createSqlType(SqlTypeName.INTEGER), rowType.getField("id", true, false).getType());

    }

    @Test
    void queryData() throws SQLException {
        var typeFactory = this.calciteConnection.getTypeFactory();
        this.calciteConnection.getRootSchema().add("test", mockSchema());
        var stmt = this.calciteConnection.prepareStatement("SELECT COUNT(*) from `test`.`mockTable`");
        var rs = stmt.executeQuery();
        rs.next();
        assertEquals(true, rs.getInt(1)==2);
    }





}