import base64

from grpc import AuthMetadataPlugin, AuthMetadataContext, AuthMetadataPluginCallback


class UsernamePasswordAuthMetadataPlugin(AuthMetadataPlugin):
    """Metadata wrapper for username/password credentials."""

    _auth: bytes

    def __init__(self, username: str, password: str):
        self._auth = base64.b64encode(f"{username}:{password}".encode())

    def __call__(
            self,
            context: AuthMetadataContext,
            callback: AuthMetadataPluginCallback,
    ):
        metadata = (("authorization", self._auth),)
        callback(metadata, None)

def username_password_call_credentials(username, password):
    """Construct CallCredentials from an username and password.

    Args:
      username: A username user in 'authorization' header
      password: A password user in 'authorization' header

    Returns:
      A CallCredentials.
    """
    from grpc import _plugin_wrapping  # pylint: disable=cyclic-import

    return _plugin_wrapping.metadata_plugin_call_credentials(
        UsernamePasswordAuthMetadataPlugin(username, password), None
    )