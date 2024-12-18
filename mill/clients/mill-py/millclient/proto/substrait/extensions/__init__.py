# Generated by the protocol buffer compiler.  DO NOT EDIT!
# sources: substrait/extensions/extensions.proto
# plugin: python-betterproto
# This file has been @generated

from dataclasses import dataclass
from typing import List

import betterproto
import betterproto.lib.google.protobuf as betterproto_lib_google_protobuf


@dataclass(eq=False, repr=False)
class SimpleExtensionUri(betterproto.Message):
    extension_uri_anchor: int = betterproto.uint32_field(1)
    """
    A surrogate key used in the context of a single plan used to reference the
     URI associated with an extension.
    """

    uri: str = betterproto.string_field(2)
    """
    The URI where this extension YAML can be retrieved. This is the "namespace"
     of this extension.
    """


@dataclass(eq=False, repr=False)
class SimpleExtensionDeclaration(betterproto.Message):
    """
    Describes a mapping between a specific extension entity and the uri where
     that extension can be found.
    """

    extension_type: "SimpleExtensionDeclarationExtensionType" = (
        betterproto.message_field(1, group="mapping_type")
    )
    extension_type_variation: "SimpleExtensionDeclarationExtensionTypeVariation" = (
        betterproto.message_field(2, group="mapping_type")
    )
    extension_function: "SimpleExtensionDeclarationExtensionFunction" = (
        betterproto.message_field(3, group="mapping_type")
    )


@dataclass(eq=False, repr=False)
class SimpleExtensionDeclarationExtensionType(betterproto.Message):
    """Describes a Type"""

    extension_uri_reference: int = betterproto.uint32_field(1)
    """
    references the extension_uri_anchor defined for a specific extension URI.
    """

    type_anchor: int = betterproto.uint32_field(2)
    """
    A surrogate key used in the context of a single plan to reference a
     specific extension type
    """

    name: str = betterproto.string_field(3)
    """the name of the type in the defined extension YAML."""


@dataclass(eq=False, repr=False)
class SimpleExtensionDeclarationExtensionTypeVariation(betterproto.Message):
    extension_uri_reference: int = betterproto.uint32_field(1)
    """
    references the extension_uri_anchor defined for a specific extension URI.
    """

    type_variation_anchor: int = betterproto.uint32_field(2)
    """
    A surrogate key used in the context of a single plan to reference a
     specific type variation
    """

    name: str = betterproto.string_field(3)
    """the name of the type in the defined extension YAML."""


@dataclass(eq=False, repr=False)
class SimpleExtensionDeclarationExtensionFunction(betterproto.Message):
    extension_uri_reference: int = betterproto.uint32_field(1)
    """
    references the extension_uri_anchor defined for a specific extension URI.
    """

    function_anchor: int = betterproto.uint32_field(2)
    """
    A surrogate key used in the context of a single plan to reference a
     specific function
    """

    name: str = betterproto.string_field(3)
    """A function signature compound name"""


@dataclass(eq=False, repr=False)
class AdvancedExtension(betterproto.Message):
    """
    A generic object that can be used to embed additional extension information
     into the serialized substrait plan.
    """

    optimization: List["betterproto_lib_google_protobuf.Any"] = (
        betterproto.message_field(1)
    )
    """
    An optimization is helpful information that don't influence semantics. May
     be ignored by a consumer.
    """

    enhancement: "betterproto_lib_google_protobuf.Any" = betterproto.message_field(2)
    """An enhancement alter semantics. Cannot be ignored by a consumer."""
