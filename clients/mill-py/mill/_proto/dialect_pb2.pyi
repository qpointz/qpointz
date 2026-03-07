from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class GetDialectRequest(_message.Message):
    __slots__ = ("dialectId",)
    DIALECTID_FIELD_NUMBER: _ClassVar[int]
    dialectId: str
    def __init__(self, dialectId: _Optional[str] = ...) -> None: ...

class GetDialectResponse(_message.Message):
    __slots__ = ("dialect", "schemaVersion", "contentHash")
    DIALECT_FIELD_NUMBER: _ClassVar[int]
    SCHEMAVERSION_FIELD_NUMBER: _ClassVar[int]
    CONTENTHASH_FIELD_NUMBER: _ClassVar[int]
    dialect: DialectDescriptor
    schemaVersion: str
    contentHash: str
    def __init__(self, dialect: _Optional[_Union[DialectDescriptor, _Mapping]] = ..., schemaVersion: _Optional[str] = ..., contentHash: _Optional[str] = ...) -> None: ...

class DialectDescriptor(_message.Message):
    __slots__ = ("id", "name", "readOnly", "paramstyle", "notes", "identifiers", "catalogSchema", "transactions", "limits", "nullSorting", "resultSet", "featureFlags", "stringProperties", "literals", "joins", "paging", "operators", "functions", "typeInfo")
    class QuotePair(_message.Message):
        __slots__ = ("start", "end")
        START_FIELD_NUMBER: _ClassVar[int]
        END_FIELD_NUMBER: _ClassVar[int]
        start: str
        end: str
        def __init__(self, start: _Optional[str] = ..., end: _Optional[str] = ...) -> None: ...
    class Identifiers(_message.Message):
        __slots__ = ("quote", "aliasQuote", "escapeQuote", "unquotedStorage", "quotedStorage", "supportsMixedCase", "supportsMixedCaseQuoted", "maxLength", "extraNameCharacters", "useFullyQualifiedNames")
        QUOTE_FIELD_NUMBER: _ClassVar[int]
        ALIASQUOTE_FIELD_NUMBER: _ClassVar[int]
        ESCAPEQUOTE_FIELD_NUMBER: _ClassVar[int]
        UNQUOTEDSTORAGE_FIELD_NUMBER: _ClassVar[int]
        QUOTEDSTORAGE_FIELD_NUMBER: _ClassVar[int]
        SUPPORTSMIXEDCASE_FIELD_NUMBER: _ClassVar[int]
        SUPPORTSMIXEDCASEQUOTED_FIELD_NUMBER: _ClassVar[int]
        MAXLENGTH_FIELD_NUMBER: _ClassVar[int]
        EXTRANAMECHARACTERS_FIELD_NUMBER: _ClassVar[int]
        USEFULLYQUALIFIEDNAMES_FIELD_NUMBER: _ClassVar[int]
        quote: DialectDescriptor.QuotePair
        aliasQuote: DialectDescriptor.QuotePair
        escapeQuote: str
        unquotedStorage: str
        quotedStorage: str
        supportsMixedCase: bool
        supportsMixedCaseQuoted: bool
        maxLength: int
        extraNameCharacters: str
        useFullyQualifiedNames: bool
        def __init__(self, quote: _Optional[_Union[DialectDescriptor.QuotePair, _Mapping]] = ..., aliasQuote: _Optional[_Union[DialectDescriptor.QuotePair, _Mapping]] = ..., escapeQuote: _Optional[str] = ..., unquotedStorage: _Optional[str] = ..., quotedStorage: _Optional[str] = ..., supportsMixedCase: bool = ..., supportsMixedCaseQuoted: bool = ..., maxLength: _Optional[int] = ..., extraNameCharacters: _Optional[str] = ..., useFullyQualifiedNames: bool = ...) -> None: ...
    class CatalogSchema(_message.Message):
        __slots__ = ("supportsSchemas", "supportsCatalogs", "catalogSeparator", "catalogAtStart", "schemaTerm", "catalogTerm", "procedureTerm", "schemasInDml", "schemasInProcedureCalls", "schemasInTableDefinitions", "schemasInIndexDefinitions", "schemasInPrivilegeDefinitions", "catalogsInDml", "catalogsInProcedureCalls", "catalogsInTableDefinitions", "catalogsInIndexDefinitions", "catalogsInPrivilegeDefinitions")
        SUPPORTSSCHEMAS_FIELD_NUMBER: _ClassVar[int]
        SUPPORTSCATALOGS_FIELD_NUMBER: _ClassVar[int]
        CATALOGSEPARATOR_FIELD_NUMBER: _ClassVar[int]
        CATALOGATSTART_FIELD_NUMBER: _ClassVar[int]
        SCHEMATERM_FIELD_NUMBER: _ClassVar[int]
        CATALOGTERM_FIELD_NUMBER: _ClassVar[int]
        PROCEDURETERM_FIELD_NUMBER: _ClassVar[int]
        SCHEMASINDML_FIELD_NUMBER: _ClassVar[int]
        SCHEMASINPROCEDURECALLS_FIELD_NUMBER: _ClassVar[int]
        SCHEMASINTABLEDEFINITIONS_FIELD_NUMBER: _ClassVar[int]
        SCHEMASININDEXDEFINITIONS_FIELD_NUMBER: _ClassVar[int]
        SCHEMASINPRIVILEGEDEFINITIONS_FIELD_NUMBER: _ClassVar[int]
        CATALOGSINDML_FIELD_NUMBER: _ClassVar[int]
        CATALOGSINPROCEDURECALLS_FIELD_NUMBER: _ClassVar[int]
        CATALOGSINTABLEDEFINITIONS_FIELD_NUMBER: _ClassVar[int]
        CATALOGSININDEXDEFINITIONS_FIELD_NUMBER: _ClassVar[int]
        CATALOGSINPRIVILEGEDEFINITIONS_FIELD_NUMBER: _ClassVar[int]
        supportsSchemas: bool
        supportsCatalogs: bool
        catalogSeparator: str
        catalogAtStart: bool
        schemaTerm: str
        catalogTerm: str
        procedureTerm: str
        schemasInDml: bool
        schemasInProcedureCalls: bool
        schemasInTableDefinitions: bool
        schemasInIndexDefinitions: bool
        schemasInPrivilegeDefinitions: bool
        catalogsInDml: bool
        catalogsInProcedureCalls: bool
        catalogsInTableDefinitions: bool
        catalogsInIndexDefinitions: bool
        catalogsInPrivilegeDefinitions: bool
        def __init__(self, supportsSchemas: bool = ..., supportsCatalogs: bool = ..., catalogSeparator: _Optional[str] = ..., catalogAtStart: bool = ..., schemaTerm: _Optional[str] = ..., catalogTerm: _Optional[str] = ..., procedureTerm: _Optional[str] = ..., schemasInDml: bool = ..., schemasInProcedureCalls: bool = ..., schemasInTableDefinitions: bool = ..., schemasInIndexDefinitions: bool = ..., schemasInPrivilegeDefinitions: bool = ..., catalogsInDml: bool = ..., catalogsInProcedureCalls: bool = ..., catalogsInTableDefinitions: bool = ..., catalogsInIndexDefinitions: bool = ..., catalogsInPrivilegeDefinitions: bool = ...) -> None: ...
    class Transactions(_message.Message):
        __slots__ = ("supported", "defaultIsolation", "supportsMultiple", "supportsDdlAndDml", "supportsDmlOnly", "ddlCausesCommit", "ddlIgnoredInTransactions")
        SUPPORTED_FIELD_NUMBER: _ClassVar[int]
        DEFAULTISOLATION_FIELD_NUMBER: _ClassVar[int]
        SUPPORTSMULTIPLE_FIELD_NUMBER: _ClassVar[int]
        SUPPORTSDDLANDDML_FIELD_NUMBER: _ClassVar[int]
        SUPPORTSDMLONLY_FIELD_NUMBER: _ClassVar[int]
        DDLCAUSESCOMMIT_FIELD_NUMBER: _ClassVar[int]
        DDLIGNOREDINTRANSACTIONS_FIELD_NUMBER: _ClassVar[int]
        supported: bool
        defaultIsolation: str
        supportsMultiple: bool
        supportsDdlAndDml: bool
        supportsDmlOnly: bool
        ddlCausesCommit: bool
        ddlIgnoredInTransactions: bool
        def __init__(self, supported: bool = ..., defaultIsolation: _Optional[str] = ..., supportsMultiple: bool = ..., supportsDdlAndDml: bool = ..., supportsDmlOnly: bool = ..., ddlCausesCommit: bool = ..., ddlIgnoredInTransactions: bool = ...) -> None: ...
    class Limits(_message.Message):
        __slots__ = ("maxBinaryLiteralLength", "maxCharLiteralLength", "maxColumnNameLength", "maxColumnsInGroupBy", "maxColumnsInIndex", "maxColumnsInOrderBy", "maxColumnsInSelect", "maxColumnsInTable", "maxConnections", "maxIndexLength", "maxSchemaNameLength", "maxCatalogNameLength", "maxRowSize", "maxRowSizeIncludesBlobs", "maxStatementLength", "maxStatements", "maxTableNameLength", "maxTablesInSelect")
        MAXBINARYLITERALLENGTH_FIELD_NUMBER: _ClassVar[int]
        MAXCHARLITERALLENGTH_FIELD_NUMBER: _ClassVar[int]
        MAXCOLUMNNAMELENGTH_FIELD_NUMBER: _ClassVar[int]
        MAXCOLUMNSINGROUPBY_FIELD_NUMBER: _ClassVar[int]
        MAXCOLUMNSININDEX_FIELD_NUMBER: _ClassVar[int]
        MAXCOLUMNSINORDERBY_FIELD_NUMBER: _ClassVar[int]
        MAXCOLUMNSINSELECT_FIELD_NUMBER: _ClassVar[int]
        MAXCOLUMNSINTABLE_FIELD_NUMBER: _ClassVar[int]
        MAXCONNECTIONS_FIELD_NUMBER: _ClassVar[int]
        MAXINDEXLENGTH_FIELD_NUMBER: _ClassVar[int]
        MAXSCHEMANAMELENGTH_FIELD_NUMBER: _ClassVar[int]
        MAXCATALOGNAMELENGTH_FIELD_NUMBER: _ClassVar[int]
        MAXROWSIZE_FIELD_NUMBER: _ClassVar[int]
        MAXROWSIZEINCLUDESBLOBS_FIELD_NUMBER: _ClassVar[int]
        MAXSTATEMENTLENGTH_FIELD_NUMBER: _ClassVar[int]
        MAXSTATEMENTS_FIELD_NUMBER: _ClassVar[int]
        MAXTABLENAMELENGTH_FIELD_NUMBER: _ClassVar[int]
        MAXTABLESINSELECT_FIELD_NUMBER: _ClassVar[int]
        maxBinaryLiteralLength: int
        maxCharLiteralLength: int
        maxColumnNameLength: int
        maxColumnsInGroupBy: int
        maxColumnsInIndex: int
        maxColumnsInOrderBy: int
        maxColumnsInSelect: int
        maxColumnsInTable: int
        maxConnections: int
        maxIndexLength: int
        maxSchemaNameLength: int
        maxCatalogNameLength: int
        maxRowSize: int
        maxRowSizeIncludesBlobs: bool
        maxStatementLength: int
        maxStatements: int
        maxTableNameLength: int
        maxTablesInSelect: int
        def __init__(self, maxBinaryLiteralLength: _Optional[int] = ..., maxCharLiteralLength: _Optional[int] = ..., maxColumnNameLength: _Optional[int] = ..., maxColumnsInGroupBy: _Optional[int] = ..., maxColumnsInIndex: _Optional[int] = ..., maxColumnsInOrderBy: _Optional[int] = ..., maxColumnsInSelect: _Optional[int] = ..., maxColumnsInTable: _Optional[int] = ..., maxConnections: _Optional[int] = ..., maxIndexLength: _Optional[int] = ..., maxSchemaNameLength: _Optional[int] = ..., maxCatalogNameLength: _Optional[int] = ..., maxRowSize: _Optional[int] = ..., maxRowSizeIncludesBlobs: bool = ..., maxStatementLength: _Optional[int] = ..., maxStatements: _Optional[int] = ..., maxTableNameLength: _Optional[int] = ..., maxTablesInSelect: _Optional[int] = ...) -> None: ...
    class NullSorting(_message.Message):
        __slots__ = ("nullsSortedHigh", "nullsSortedLow", "nullsSortedAtStart", "nullsSortedAtEnd", "supportsNullsFirst", "supportsNullsLast")
        NULLSSORTEDHIGH_FIELD_NUMBER: _ClassVar[int]
        NULLSSORTEDLOW_FIELD_NUMBER: _ClassVar[int]
        NULLSSORTEDATSTART_FIELD_NUMBER: _ClassVar[int]
        NULLSSORTEDATEND_FIELD_NUMBER: _ClassVar[int]
        SUPPORTSNULLSFIRST_FIELD_NUMBER: _ClassVar[int]
        SUPPORTSNULLSLAST_FIELD_NUMBER: _ClassVar[int]
        nullsSortedHigh: bool
        nullsSortedLow: bool
        nullsSortedAtStart: bool
        nullsSortedAtEnd: bool
        supportsNullsFirst: bool
        supportsNullsLast: bool
        def __init__(self, nullsSortedHigh: bool = ..., nullsSortedLow: bool = ..., nullsSortedAtStart: bool = ..., nullsSortedAtEnd: bool = ..., supportsNullsFirst: bool = ..., supportsNullsLast: bool = ...) -> None: ...
    class ResultSetCaps(_message.Message):
        __slots__ = ("forwardOnly", "scrollInsensitive", "scrollSensitive", "concurrencyReadOnly", "concurrencyUpdatable")
        FORWARDONLY_FIELD_NUMBER: _ClassVar[int]
        SCROLLINSENSITIVE_FIELD_NUMBER: _ClassVar[int]
        SCROLLSENSITIVE_FIELD_NUMBER: _ClassVar[int]
        CONCURRENCYREADONLY_FIELD_NUMBER: _ClassVar[int]
        CONCURRENCYUPDATABLE_FIELD_NUMBER: _ClassVar[int]
        forwardOnly: bool
        scrollInsensitive: bool
        scrollSensitive: bool
        concurrencyReadOnly: bool
        concurrencyUpdatable: bool
        def __init__(self, forwardOnly: bool = ..., scrollInsensitive: bool = ..., scrollSensitive: bool = ..., concurrencyReadOnly: bool = ..., concurrencyUpdatable: bool = ...) -> None: ...
    class StringProperties(_message.Message):
        __slots__ = ("searchStringEscape", "sqlKeywords", "systemFunctions")
        SEARCHSTRINGESCAPE_FIELD_NUMBER: _ClassVar[int]
        SQLKEYWORDS_FIELD_NUMBER: _ClassVar[int]
        SYSTEMFUNCTIONS_FIELD_NUMBER: _ClassVar[int]
        searchStringEscape: str
        sqlKeywords: str
        systemFunctions: str
        def __init__(self, searchStringEscape: _Optional[str] = ..., sqlKeywords: _Optional[str] = ..., systemFunctions: _Optional[str] = ...) -> None: ...
    class StringLiterals(_message.Message):
        __slots__ = ("quote", "concat", "escape", "note")
        QUOTE_FIELD_NUMBER: _ClassVar[int]
        CONCAT_FIELD_NUMBER: _ClassVar[int]
        ESCAPE_FIELD_NUMBER: _ClassVar[int]
        NOTE_FIELD_NUMBER: _ClassVar[int]
        quote: str
        concat: str
        escape: str
        note: str
        def __init__(self, quote: _Optional[str] = ..., concat: _Optional[str] = ..., escape: _Optional[str] = ..., note: _Optional[str] = ...) -> None: ...
    class DateTimeLiteral(_message.Message):
        __slots__ = ("syntax", "quote", "pattern", "notes")
        SYNTAX_FIELD_NUMBER: _ClassVar[int]
        QUOTE_FIELD_NUMBER: _ClassVar[int]
        PATTERN_FIELD_NUMBER: _ClassVar[int]
        NOTES_FIELD_NUMBER: _ClassVar[int]
        syntax: str
        quote: str
        pattern: str
        notes: _containers.RepeatedScalarFieldContainer[str]
        def __init__(self, syntax: _Optional[str] = ..., quote: _Optional[str] = ..., pattern: _Optional[str] = ..., notes: _Optional[_Iterable[str]] = ...) -> None: ...
    class IntervalLiteral(_message.Message):
        __slots__ = ("supported", "style", "notes")
        SUPPORTED_FIELD_NUMBER: _ClassVar[int]
        STYLE_FIELD_NUMBER: _ClassVar[int]
        NOTES_FIELD_NUMBER: _ClassVar[int]
        supported: bool
        style: str
        notes: _containers.RepeatedScalarFieldContainer[str]
        def __init__(self, supported: bool = ..., style: _Optional[str] = ..., notes: _Optional[_Iterable[str]] = ...) -> None: ...
    class DatesTimesLiterals(_message.Message):
        __slots__ = ("date", "time", "timestamp", "interval")
        DATE_FIELD_NUMBER: _ClassVar[int]
        TIME_FIELD_NUMBER: _ClassVar[int]
        TIMESTAMP_FIELD_NUMBER: _ClassVar[int]
        INTERVAL_FIELD_NUMBER: _ClassVar[int]
        date: DialectDescriptor.DateTimeLiteral
        time: DialectDescriptor.DateTimeLiteral
        timestamp: DialectDescriptor.DateTimeLiteral
        interval: DialectDescriptor.IntervalLiteral
        def __init__(self, date: _Optional[_Union[DialectDescriptor.DateTimeLiteral, _Mapping]] = ..., time: _Optional[_Union[DialectDescriptor.DateTimeLiteral, _Mapping]] = ..., timestamp: _Optional[_Union[DialectDescriptor.DateTimeLiteral, _Mapping]] = ..., interval: _Optional[_Union[DialectDescriptor.IntervalLiteral, _Mapping]] = ...) -> None: ...
    class Literals(_message.Message):
        __slots__ = ("strings", "booleans", "nullLiteral", "datesTimes")
        STRINGS_FIELD_NUMBER: _ClassVar[int]
        BOOLEANS_FIELD_NUMBER: _ClassVar[int]
        NULLLITERAL_FIELD_NUMBER: _ClassVar[int]
        DATESTIMES_FIELD_NUMBER: _ClassVar[int]
        strings: DialectDescriptor.StringLiterals
        booleans: _containers.RepeatedScalarFieldContainer[str]
        nullLiteral: str
        datesTimes: DialectDescriptor.DatesTimesLiterals
        def __init__(self, strings: _Optional[_Union[DialectDescriptor.StringLiterals, _Mapping]] = ..., booleans: _Optional[_Iterable[str]] = ..., nullLiteral: _Optional[str] = ..., datesTimes: _Optional[_Union[DialectDescriptor.DatesTimesLiterals, _Mapping]] = ...) -> None: ...
    class JoinType(_message.Message):
        __slots__ = ("enabled", "keyword", "requireOn", "nullSafe", "notes")
        ENABLED_FIELD_NUMBER: _ClassVar[int]
        KEYWORD_FIELD_NUMBER: _ClassVar[int]
        REQUIREON_FIELD_NUMBER: _ClassVar[int]
        NULLSAFE_FIELD_NUMBER: _ClassVar[int]
        NOTES_FIELD_NUMBER: _ClassVar[int]
        enabled: bool
        keyword: str
        requireOn: bool
        nullSafe: bool
        notes: str
        def __init__(self, enabled: bool = ..., keyword: _Optional[str] = ..., requireOn: bool = ..., nullSafe: bool = ..., notes: _Optional[str] = ...) -> None: ...
    class OnClause(_message.Message):
        __slots__ = ("keyword", "requireCondition")
        KEYWORD_FIELD_NUMBER: _ClassVar[int]
        REQUIRECONDITION_FIELD_NUMBER: _ClassVar[int]
        keyword: str
        requireCondition: bool
        def __init__(self, keyword: _Optional[str] = ..., requireCondition: bool = ...) -> None: ...
    class Joins(_message.Message):
        __slots__ = ("style", "crossJoin", "innerJoin", "leftJoin", "rightJoin", "fullJoin", "onClause")
        STYLE_FIELD_NUMBER: _ClassVar[int]
        CROSSJOIN_FIELD_NUMBER: _ClassVar[int]
        INNERJOIN_FIELD_NUMBER: _ClassVar[int]
        LEFTJOIN_FIELD_NUMBER: _ClassVar[int]
        RIGHTJOIN_FIELD_NUMBER: _ClassVar[int]
        FULLJOIN_FIELD_NUMBER: _ClassVar[int]
        ONCLAUSE_FIELD_NUMBER: _ClassVar[int]
        style: str
        crossJoin: DialectDescriptor.JoinType
        innerJoin: DialectDescriptor.JoinType
        leftJoin: DialectDescriptor.JoinType
        rightJoin: DialectDescriptor.JoinType
        fullJoin: DialectDescriptor.JoinType
        onClause: DialectDescriptor.OnClause
        def __init__(self, style: _Optional[str] = ..., crossJoin: _Optional[_Union[DialectDescriptor.JoinType, _Mapping]] = ..., innerJoin: _Optional[_Union[DialectDescriptor.JoinType, _Mapping]] = ..., leftJoin: _Optional[_Union[DialectDescriptor.JoinType, _Mapping]] = ..., rightJoin: _Optional[_Union[DialectDescriptor.JoinType, _Mapping]] = ..., fullJoin: _Optional[_Union[DialectDescriptor.JoinType, _Mapping]] = ..., onClause: _Optional[_Union[DialectDescriptor.OnClause, _Mapping]] = ...) -> None: ...
    class PagingStyle(_message.Message):
        __slots__ = ("syntax", "type", "deprecated")
        SYNTAX_FIELD_NUMBER: _ClassVar[int]
        TYPE_FIELD_NUMBER: _ClassVar[int]
        DEPRECATED_FIELD_NUMBER: _ClassVar[int]
        syntax: str
        type: str
        deprecated: bool
        def __init__(self, syntax: _Optional[str] = ..., type: _Optional[str] = ..., deprecated: bool = ...) -> None: ...
    class Paging(_message.Message):
        __slots__ = ("styles", "offset", "noLimitValue")
        STYLES_FIELD_NUMBER: _ClassVar[int]
        OFFSET_FIELD_NUMBER: _ClassVar[int]
        NOLIMITVALUE_FIELD_NUMBER: _ClassVar[int]
        styles: _containers.RepeatedCompositeFieldContainer[DialectDescriptor.PagingStyle]
        offset: str
        noLimitValue: str
        def __init__(self, styles: _Optional[_Iterable[_Union[DialectDescriptor.PagingStyle, _Mapping]]] = ..., offset: _Optional[str] = ..., noLimitValue: _Optional[str] = ...) -> None: ...
    class OperatorEntry(_message.Message):
        __slots__ = ("symbol", "syntax", "description", "supported", "deprecated")
        SYMBOL_FIELD_NUMBER: _ClassVar[int]
        SYNTAX_FIELD_NUMBER: _ClassVar[int]
        DESCRIPTION_FIELD_NUMBER: _ClassVar[int]
        SUPPORTED_FIELD_NUMBER: _ClassVar[int]
        DEPRECATED_FIELD_NUMBER: _ClassVar[int]
        symbol: str
        syntax: str
        description: str
        supported: bool
        deprecated: bool
        def __init__(self, symbol: _Optional[str] = ..., syntax: _Optional[str] = ..., description: _Optional[str] = ..., supported: bool = ..., deprecated: bool = ...) -> None: ...
    class ReturnType(_message.Message):
        __slots__ = ("type", "nullable")
        TYPE_FIELD_NUMBER: _ClassVar[int]
        NULLABLE_FIELD_NUMBER: _ClassVar[int]
        type: str
        nullable: bool
        def __init__(self, type: _Optional[str] = ..., nullable: bool = ...) -> None: ...
    class FunctionArg(_message.Message):
        __slots__ = ("name", "type", "required", "variadic", "multi", "min", "max", "enumValues", "defaultValue", "notes")
        NAME_FIELD_NUMBER: _ClassVar[int]
        TYPE_FIELD_NUMBER: _ClassVar[int]
        REQUIRED_FIELD_NUMBER: _ClassVar[int]
        VARIADIC_FIELD_NUMBER: _ClassVar[int]
        MULTI_FIELD_NUMBER: _ClassVar[int]
        MIN_FIELD_NUMBER: _ClassVar[int]
        MAX_FIELD_NUMBER: _ClassVar[int]
        ENUMVALUES_FIELD_NUMBER: _ClassVar[int]
        DEFAULTVALUE_FIELD_NUMBER: _ClassVar[int]
        NOTES_FIELD_NUMBER: _ClassVar[int]
        name: str
        type: str
        required: bool
        variadic: bool
        multi: bool
        min: int
        max: int
        enumValues: _containers.RepeatedScalarFieldContainer[str]
        defaultValue: str
        notes: str
        def __init__(self, name: _Optional[str] = ..., type: _Optional[str] = ..., required: bool = ..., variadic: bool = ..., multi: bool = ..., min: _Optional[int] = ..., max: _Optional[int] = ..., enumValues: _Optional[_Iterable[str]] = ..., defaultValue: _Optional[str] = ..., notes: _Optional[str] = ...) -> None: ...
    class FunctionEntry(_message.Message):
        __slots__ = ("name", "synonyms", "returnType", "syntax", "args", "notes")
        NAME_FIELD_NUMBER: _ClassVar[int]
        SYNONYMS_FIELD_NUMBER: _ClassVar[int]
        RETURNTYPE_FIELD_NUMBER: _ClassVar[int]
        SYNTAX_FIELD_NUMBER: _ClassVar[int]
        ARGS_FIELD_NUMBER: _ClassVar[int]
        NOTES_FIELD_NUMBER: _ClassVar[int]
        name: str
        synonyms: _containers.RepeatedScalarFieldContainer[str]
        returnType: DialectDescriptor.ReturnType
        syntax: str
        args: _containers.RepeatedCompositeFieldContainer[DialectDescriptor.FunctionArg]
        notes: _containers.RepeatedScalarFieldContainer[str]
        def __init__(self, name: _Optional[str] = ..., synonyms: _Optional[_Iterable[str]] = ..., returnType: _Optional[_Union[DialectDescriptor.ReturnType, _Mapping]] = ..., syntax: _Optional[str] = ..., args: _Optional[_Iterable[_Union[DialectDescriptor.FunctionArg, _Mapping]]] = ..., notes: _Optional[_Iterable[str]] = ...) -> None: ...
    class TypeInfo(_message.Message):
        __slots__ = ("sqlName", "jdbcTypeCode", "precision", "literalPrefix", "literalSuffix", "caseSensitive", "searchable", "unsigned", "fixedPrecScale", "autoIncrement", "minimumScale", "maximumScale", "numPrecRadix")
        SQLNAME_FIELD_NUMBER: _ClassVar[int]
        JDBCTYPECODE_FIELD_NUMBER: _ClassVar[int]
        PRECISION_FIELD_NUMBER: _ClassVar[int]
        LITERALPREFIX_FIELD_NUMBER: _ClassVar[int]
        LITERALSUFFIX_FIELD_NUMBER: _ClassVar[int]
        CASESENSITIVE_FIELD_NUMBER: _ClassVar[int]
        SEARCHABLE_FIELD_NUMBER: _ClassVar[int]
        UNSIGNED_FIELD_NUMBER: _ClassVar[int]
        FIXEDPRECSCALE_FIELD_NUMBER: _ClassVar[int]
        AUTOINCREMENT_FIELD_NUMBER: _ClassVar[int]
        MINIMUMSCALE_FIELD_NUMBER: _ClassVar[int]
        MAXIMUMSCALE_FIELD_NUMBER: _ClassVar[int]
        NUMPRECRADIX_FIELD_NUMBER: _ClassVar[int]
        sqlName: str
        jdbcTypeCode: int
        precision: int
        literalPrefix: str
        literalSuffix: str
        caseSensitive: bool
        searchable: int
        unsigned: bool
        fixedPrecScale: bool
        autoIncrement: bool
        minimumScale: int
        maximumScale: int
        numPrecRadix: int
        def __init__(self, sqlName: _Optional[str] = ..., jdbcTypeCode: _Optional[int] = ..., precision: _Optional[int] = ..., literalPrefix: _Optional[str] = ..., literalSuffix: _Optional[str] = ..., caseSensitive: bool = ..., searchable: _Optional[int] = ..., unsigned: bool = ..., fixedPrecScale: bool = ..., autoIncrement: bool = ..., minimumScale: _Optional[int] = ..., maximumScale: _Optional[int] = ..., numPrecRadix: _Optional[int] = ...) -> None: ...
    class OperatorCategory(_message.Message):
        __slots__ = ("entries",)
        ENTRIES_FIELD_NUMBER: _ClassVar[int]
        entries: _containers.RepeatedCompositeFieldContainer[DialectDescriptor.OperatorEntry]
        def __init__(self, entries: _Optional[_Iterable[_Union[DialectDescriptor.OperatorEntry, _Mapping]]] = ...) -> None: ...
    class FunctionCategory(_message.Message):
        __slots__ = ("entries",)
        ENTRIES_FIELD_NUMBER: _ClassVar[int]
        entries: _containers.RepeatedCompositeFieldContainer[DialectDescriptor.FunctionEntry]
        def __init__(self, entries: _Optional[_Iterable[_Union[DialectDescriptor.FunctionEntry, _Mapping]]] = ...) -> None: ...
    class FeatureFlagsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: bool
        def __init__(self, key: _Optional[str] = ..., value: bool = ...) -> None: ...
    class OperatorsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: DialectDescriptor.OperatorCategory
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[DialectDescriptor.OperatorCategory, _Mapping]] = ...) -> None: ...
    class FunctionsEntry(_message.Message):
        __slots__ = ("key", "value")
        KEY_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        key: str
        value: DialectDescriptor.FunctionCategory
        def __init__(self, key: _Optional[str] = ..., value: _Optional[_Union[DialectDescriptor.FunctionCategory, _Mapping]] = ...) -> None: ...
    ID_FIELD_NUMBER: _ClassVar[int]
    NAME_FIELD_NUMBER: _ClassVar[int]
    READONLY_FIELD_NUMBER: _ClassVar[int]
    PARAMSTYLE_FIELD_NUMBER: _ClassVar[int]
    NOTES_FIELD_NUMBER: _ClassVar[int]
    IDENTIFIERS_FIELD_NUMBER: _ClassVar[int]
    CATALOGSCHEMA_FIELD_NUMBER: _ClassVar[int]
    TRANSACTIONS_FIELD_NUMBER: _ClassVar[int]
    LIMITS_FIELD_NUMBER: _ClassVar[int]
    NULLSORTING_FIELD_NUMBER: _ClassVar[int]
    RESULTSET_FIELD_NUMBER: _ClassVar[int]
    FEATUREFLAGS_FIELD_NUMBER: _ClassVar[int]
    STRINGPROPERTIES_FIELD_NUMBER: _ClassVar[int]
    LITERALS_FIELD_NUMBER: _ClassVar[int]
    JOINS_FIELD_NUMBER: _ClassVar[int]
    PAGING_FIELD_NUMBER: _ClassVar[int]
    OPERATORS_FIELD_NUMBER: _ClassVar[int]
    FUNCTIONS_FIELD_NUMBER: _ClassVar[int]
    TYPEINFO_FIELD_NUMBER: _ClassVar[int]
    id: str
    name: str
    readOnly: bool
    paramstyle: str
    notes: _containers.RepeatedScalarFieldContainer[str]
    identifiers: DialectDescriptor.Identifiers
    catalogSchema: DialectDescriptor.CatalogSchema
    transactions: DialectDescriptor.Transactions
    limits: DialectDescriptor.Limits
    nullSorting: DialectDescriptor.NullSorting
    resultSet: DialectDescriptor.ResultSetCaps
    featureFlags: _containers.ScalarMap[str, bool]
    stringProperties: DialectDescriptor.StringProperties
    literals: DialectDescriptor.Literals
    joins: DialectDescriptor.Joins
    paging: DialectDescriptor.Paging
    operators: _containers.MessageMap[str, DialectDescriptor.OperatorCategory]
    functions: _containers.MessageMap[str, DialectDescriptor.FunctionCategory]
    typeInfo: _containers.RepeatedCompositeFieldContainer[DialectDescriptor.TypeInfo]
    def __init__(self, id: _Optional[str] = ..., name: _Optional[str] = ..., readOnly: bool = ..., paramstyle: _Optional[str] = ..., notes: _Optional[_Iterable[str]] = ..., identifiers: _Optional[_Union[DialectDescriptor.Identifiers, _Mapping]] = ..., catalogSchema: _Optional[_Union[DialectDescriptor.CatalogSchema, _Mapping]] = ..., transactions: _Optional[_Union[DialectDescriptor.Transactions, _Mapping]] = ..., limits: _Optional[_Union[DialectDescriptor.Limits, _Mapping]] = ..., nullSorting: _Optional[_Union[DialectDescriptor.NullSorting, _Mapping]] = ..., resultSet: _Optional[_Union[DialectDescriptor.ResultSetCaps, _Mapping]] = ..., featureFlags: _Optional[_Mapping[str, bool]] = ..., stringProperties: _Optional[_Union[DialectDescriptor.StringProperties, _Mapping]] = ..., literals: _Optional[_Union[DialectDescriptor.Literals, _Mapping]] = ..., joins: _Optional[_Union[DialectDescriptor.Joins, _Mapping]] = ..., paging: _Optional[_Union[DialectDescriptor.Paging, _Mapping]] = ..., operators: _Optional[_Mapping[str, DialectDescriptor.OperatorCategory]] = ..., functions: _Optional[_Mapping[str, DialectDescriptor.FunctionCategory]] = ..., typeInfo: _Optional[_Iterable[_Union[DialectDescriptor.TypeInfo, _Mapping]]] = ...) -> None: ...
