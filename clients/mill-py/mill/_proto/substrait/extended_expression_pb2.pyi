from substrait import algebra_pb2 as _algebra_pb2
from substrait.extensions import extensions_pb2 as _extensions_pb2
from substrait import plan_pb2 as _plan_pb2
from substrait import type_pb2 as _type_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class ExpressionReference(_message.Message):
    __slots__ = ("expression", "measure", "output_names")
    EXPRESSION_FIELD_NUMBER: _ClassVar[int]
    MEASURE_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_NAMES_FIELD_NUMBER: _ClassVar[int]
    expression: _algebra_pb2.Expression
    measure: _algebra_pb2.AggregateFunction
    output_names: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, expression: _Optional[_Union[_algebra_pb2.Expression, _Mapping]] = ..., measure: _Optional[_Union[_algebra_pb2.AggregateFunction, _Mapping]] = ..., output_names: _Optional[_Iterable[str]] = ...) -> None: ...

class ExtendedExpression(_message.Message):
    __slots__ = ("version", "extension_uris", "extensions", "referred_expr", "base_schema", "advanced_extensions", "expected_type_urls")
    VERSION_FIELD_NUMBER: _ClassVar[int]
    EXTENSION_URIS_FIELD_NUMBER: _ClassVar[int]
    EXTENSIONS_FIELD_NUMBER: _ClassVar[int]
    REFERRED_EXPR_FIELD_NUMBER: _ClassVar[int]
    BASE_SCHEMA_FIELD_NUMBER: _ClassVar[int]
    ADVANCED_EXTENSIONS_FIELD_NUMBER: _ClassVar[int]
    EXPECTED_TYPE_URLS_FIELD_NUMBER: _ClassVar[int]
    version: _plan_pb2.Version
    extension_uris: _containers.RepeatedCompositeFieldContainer[_extensions_pb2.SimpleExtensionURI]
    extensions: _containers.RepeatedCompositeFieldContainer[_extensions_pb2.SimpleExtensionDeclaration]
    referred_expr: _containers.RepeatedCompositeFieldContainer[ExpressionReference]
    base_schema: _type_pb2.NamedStruct
    advanced_extensions: _extensions_pb2.AdvancedExtension
    expected_type_urls: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, version: _Optional[_Union[_plan_pb2.Version, _Mapping]] = ..., extension_uris: _Optional[_Iterable[_Union[_extensions_pb2.SimpleExtensionURI, _Mapping]]] = ..., extensions: _Optional[_Iterable[_Union[_extensions_pb2.SimpleExtensionDeclaration, _Mapping]]] = ..., referred_expr: _Optional[_Iterable[_Union[ExpressionReference, _Mapping]]] = ..., base_schema: _Optional[_Union[_type_pb2.NamedStruct, _Mapping]] = ..., advanced_extensions: _Optional[_Union[_extensions_pb2.AdvancedExtension, _Mapping]] = ..., expected_type_urls: _Optional[_Iterable[str]] = ...) -> None: ...
