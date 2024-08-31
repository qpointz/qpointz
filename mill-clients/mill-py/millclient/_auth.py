import base64
from abc import abstractmethod


class MillCallCredentials(object):

    @abstractmethod
    def get_metadata(self):
        pass
    @abstractmethod
    def creds_type(self):
        pass

class BasicAuthCredentials(MillCallCredentials):

    def creds_type(self):
        return "USERNAME_PASSWORD"

    def __init__(self, username, password):
        self.username = username
        self.password = password

    def get_metadata(self):
        auth = base64.b64encode(f"{self.username}:{self.password}".encode()).decode()
        return [("authorization",f"Basic {auth}")]

    def __str__(self):
        return f"<{self.creds_type().capitalize()}_Credentials Username='{self.username}'>"

class BearerTokenCredentials(MillCallCredentials):

    def creds_type(self):
        return "BEARER_TOKEN"

    def __init__(self, token):
        self.token = token

    def get_metadata(self):
        return [("authorization",f"Bearer {self.token}")]

    def __str__(self):
        return f"<{self.creds_type().capitalize()}_Credentials Token='HIDDEN'>"