package io.qpointz.mill.services.calcite.istmus;

import com.google.common.annotations.VisibleForTesting;
import io.substrait.extension.SimpleExtension;
import io.substrait.isthmus.FeatureBoard;
import io.substrait.isthmus.SubstraitRelVisitor;
import io.substrait.plan.ImmutablePlan.Builder;
import io.substrait.plan.ImmutableVersion;
import io.substrait.plan.Plan.Version;
import io.substrait.plan.PlanProtoConverter;
import io.substrait.proto.Plan;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.sql.SqlDialects;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.AnsiSqlDialect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;


@Slf4j
/** Take a SQL statement and a set of table definitions and return a substrait plan. */
public class SqlToSubstrait extends SqlConverterBase {

  private final SimpleExtension.ExtensionCollection extensionCollection;

  public SqlToSubstrait(FeatureBoard features, RelDataTypeFactory relDataTypeFactory, CalciteConnectionConfig calciteConnectionConfig, SqlParser.Config parserConfig, SimpleExtension.ExtensionCollection extensionCollection) {
    super(features, relDataTypeFactory, calciteConnectionConfig, parserConfig);
    this.extensionCollection = extensionCollection;
  }

  public Plan execute(String sql, CalciteSchema schema) throws SqlParseException {
    var pair = registerSchema(schema);
    return executeInner(sql, pair.left, pair.right);
  }

  private Plan executeInner(String sql, SqlValidator validator, Prepare.CatalogReader catalogReader)
      throws SqlParseException {
    Builder builder = io.substrait.plan.Plan.builder();
    builder.version(
        ImmutableVersion.builder().from(Version.DEFAULT_VERSION).producer("isthmus").build());

    // TODO: consider case in which one sql passes conversion while others don't
    sqlToRelNode(sql, validator, catalogReader).stream()
        .map(root -> SubstraitRelVisitor.convert(root, EXTENSION_COLLECTION, featureBoard))
        .forEach(root -> builder.addRoots(root));

    PlanProtoConverter planToProto = new PlanProtoConverter();

    return planToProto.toProto(builder.build());
  }

  private List<RelRoot> sqlToRelNode(
      String sql, SqlValidator validator, Prepare.CatalogReader catalogReader)
      throws SqlParseException {
    SqlParser parser = SqlParser.create(sql, parserConfig);
    var parsedList = parser.parseStmtList();
    SqlToRelConverter converter = createSqlToRelConverter(validator, catalogReader);
    List<RelRoot> roots =
        parsedList.stream()
            .map(parsed -> getBestExpRelRoot(converter, parsed))
            .collect(java.util.stream.Collectors.toList());
    return roots;
  }

  @VisibleForTesting
  SqlToRelConverter createSqlToRelConverter(
      SqlValidator validator, Prepare.CatalogReader catalogReader) {
    SqlToRelConverter converter =
        new SqlToRelConverter(
            null,
            validator,
            catalogReader,
            relOptCluster,
            StandardConvertletTable.INSTANCE,
            converterConfig);
    return converter;
  }

  @VisibleForTesting
  static RelRoot getBestExpRelRoot(SqlToRelConverter converter, SqlNode parsed) {
    if (log.isDebugEnabled()) {
      log.debug("Parsed SQL: {}", parsed.toSqlString(AnsiSqlDialect.DEFAULT));
    }
    RelRoot root = converter.convertQuery(parsed, true, true);
    { //NOSONAR
      var program = HepProgram.builder().build();
      HepPlanner hepPlanner = new HepPlanner(program);
      hepPlanner.setRoot(root.rel);
      root = root.withRel(hepPlanner.findBestExp());
    }
    return root;
  }
}
