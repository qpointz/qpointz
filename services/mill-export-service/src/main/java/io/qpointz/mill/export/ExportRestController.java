package io.qpointz.mill.export;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.proto.Schema;
import io.qpointz.mill.proto.Table;
import io.qpointz.mill.source.export.ExportFormatProvider;
import io.qpointz.mill.source.export.ExportFormatRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST surface for streaming exports under {@code /services/export}.
 */
@RestController
@RequestMapping("/services/export")
@ConditionalOnService(value = "export", group = "data")
@Tag(name = "export", description = "Streaming SQL and table exports")
public class ExportRestController {

    private final ExportFacility exportFacility;
    private final SchemaProvider schemaProvider;
    private final ExportBaseUrlResolver baseUrlResolver;
    private final EffectiveExportFormats effectiveExportFormats;
    private final ExportFormatRegistry formatRegistry;

    /**
     * @param exportFacility          export orchestration
     * @param schemaProvider          physical catalog
     * @param baseUrlResolver         public URL hints
     * @param effectiveExportFormats  format allowlist helper
     * @param formatRegistry          SPI registry
     */
    public ExportRestController(
            ExportFacility exportFacility,
            SchemaProvider schemaProvider,
            ExportBaseUrlResolver baseUrlResolver,
            EffectiveExportFormats effectiveExportFormats,
            ExportFormatRegistry formatRegistry) {
        this.exportFacility = exportFacility;
        this.schemaProvider = schemaProvider;
        this.baseUrlResolver = baseUrlResolver;
        this.effectiveExportFormats = effectiveExportFormats;
        this.formatRegistry = formatRegistry;
    }

    @GetMapping("/formats")
    @Operation(summary = "List export formats available on this deployment (after allowlist)")
    public List<Map<String, String>> formats() {
        return exportFacility.providersForHttp().stream()
                .map(ExportRestController::formatEntry)
                .collect(Collectors.toList());
    }

    private static Map<String, String> formatEntry(ExportFormatProvider p) {
        var m = p.metadata();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("mediaType", m.getMediaType());
        map.put("fileExtension", m.getFileExtension());
        return map;
    }

    @GetMapping("/catalog")
    @Operation(summary = "Full catalog: formats and per-table export URLs")
    public Map<String, Object> catalog(HttpServletRequest request) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("formats", formats());
        root.put("schemas", schemaEntries(request));
        return root;
    }

    @GetMapping("/schemas")
    @Operation(summary = "List physical schemas")
    public List<Map<String, Object>> listSchemas(HttpServletRequest request) {
        String origin = baseUrlResolver.origin(request);
        List<Map<String, Object>> out = new ArrayList<>();
        for (String name : schemaProvider.getSchemaNames()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", name);
            row.put("href", origin + "/services/export/schemas/" + encPath(name));
            Schema schema = schemaProvider.getSchema(name);
            row.put("tableCount", schema.getTablesCount());
            out.add(row);
        }
        return out;
    }

    @GetMapping("/schemas/{schema}")
    @Operation(summary = "Tables in a schema with download URLs per allowed format")
    public Map<String, Object> schemaDetail(
            HttpServletRequest request,
            @Parameter(description = "Physical schema name") @PathVariable("schema") String schema) {
        if (!schemaProvider.isSchemaExists(schema)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown schema: " + schema);
        }
        String origin = baseUrlResolver.origin(request);
        Schema s = schemaProvider.getSchema(schema);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schema", schema);
        List<Map<String, Object>> tables = new ArrayList<>();
        for (Table t : s.getTablesList()) {
            tables.add(tableEntry(origin, schema, t.getName()));
        }
        root.put("tables", tables);
        return root;
    }

    private Map<String, Object> tableEntry(String origin, String schema, String table) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", table);
        List<Map<String, String>> exports = new ArrayList<>();
        for (ExportFormatProvider p : exportFacility.providersForHttp()) {
            String id = p.metadata().getId();
            String url = origin + "/services/export/schemas/" + encPath(schema)
                    + "/tables/" + encPath(table) + "?format=" + encQuery(id);
            exports.add(Map.of("format", id, "url", url));
        }
        row.put("exports", exports);
        return row;
    }

    private List<Map<String, Object>> schemaEntries(HttpServletRequest request) {
        List<Map<String, Object>> list = new ArrayList<>();
        String origin = baseUrlResolver.origin(request);
        for (String name : schemaProvider.getSchemaNames()) {
            Map<String, Object> sch = new LinkedHashMap<>();
            sch.put("name", name);
            Schema s = schemaProvider.getSchema(name);
            List<Map<String, Object>> tables = new ArrayList<>();
            for (Table t : s.getTablesList()) {
                tables.add(tableEntry(origin, name, t.getName()));
            }
            sch.put("tables", tables);
            list.add(sch);
        }
        return list;
    }

    @PostMapping(value = "/sql", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Export ad-hoc SQL as a streamed file")
    public ResponseEntity<StreamingResponseBody> exportSql(
            HttpServletRequest request,
            @RequestBody String sql,
            @RequestParam("format") String format,
            @RequestParam(value = "filename", required = false) String filename) {
        validateFormat(format);
        ExportFormatProvider provider = resolveProvider(format);
        StreamingResponseBody body = out -> exportFacility.exportSql(sql, format, out);
        return streamResponse(body, provider, filename);
    }

    @GetMapping("/schemas/{schema}/tables/{table}")
    @Operation(summary = "Export a physical table (plan-native scan)")
    public ResponseEntity<StreamingResponseBody> exportTable(
            @PathVariable("schema") String schema,
            @PathVariable("table") String table,
            @RequestParam("format") String format,
            @RequestParam(value = "filename", required = false) String filename) {
        if (schemaProvider.getTable(schema, table) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found");
        }
        validateFormat(format);
        ExportFormatProvider provider = resolveProvider(format);
        StreamingResponseBody body = out -> exportFacility.exportTable(schema, table, format, out);
        return streamResponse(body, provider, filename);
    }

    private void validateFormat(String format) {
        List<String> registered = formatRegistry.allProviders().stream()
                .map(p -> p.metadata().getId().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        List<String> allowed = effectiveExportFormats.effectiveFormatIds(registered);
        if (!effectiveExportFormats.isAllowed(format, allowed)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown or disallowed format: " + format);
        }
    }

    private ExportFormatProvider resolveProvider(String format) {
        List<ExportFormatProvider> visible = exportFacility.providersForHttp();
        String id = format.trim().toLowerCase(Locale.ROOT);
        return visible.stream()
                .filter(p -> p.metadata().getId().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown format: " + format));
    }

    private ResponseEntity<StreamingResponseBody> streamResponse(
            StreamingResponseBody body,
            ExportFormatProvider provider,
            String filename) {
        String safeName = ExportFilenameSanitizer.sanitize(filename, provider.metadata().getFileExtension());
        MediaType mediaType = MediaType.parseMediaType(provider.metadata().getMediaType());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(safeName, StandardCharsets.UTF_8)
                .build();
        // Expose header for fetch() clients that are cross-origin; harmless for same-origin.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(mediaType)
                .body(body);
    }

    private static String encPath(String segment) {
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }

    private static String encQuery(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
