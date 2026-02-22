package io.qpointz.mill.data.backend.jdbc.providers;

import org.apache.calcite.sql.*;
import org.apache.calcite.sql.type.*;
import org.apache.calcite.sql.validate.SqlNameMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class JdbcPermissiveOperatorTable implements SqlOperatorTable {
    private final SqlOperatorTable delegate;

    public JdbcPermissiveOperatorTable(SqlOperatorTable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void lookupOperatorOverloads(SqlIdentifier opName, @Nullable SqlFunctionCategory category, SqlSyntax syntax, List<SqlOperator> operatorList, SqlNameMatcher nameMatcher) {
        if (delegate!=null) {
            delegate.lookupOperatorOverloads(opName, category, syntax, operatorList, nameMatcher);
            if (!operatorList.isEmpty()) return;
        }

        if (category == null) {
            return;
        }

        SqlReturnTypeInference ret = ReturnTypes.VARCHAR_NULLABLE; // можно ReturnTypes.VARCHAR_2000
        SqlOperandTypeInference infer = InferTypes.ANY_NULLABLE;
        SqlOperandTypeChecker checker = OperandTypes.VARIADIC; // принимает любые аргументы

        SqlFunction fn = new SqlFunction(
                opName.getSimple(),                             // имя функции
                SqlKind.OTHER_FUNCTION,
                ret,
                infer,
                checker,
                category
        );

        operatorList.add(fn);
    }

    @Override
    public List<SqlOperator> getOperatorList() {
        return delegate.getOperatorList();
    }
}
