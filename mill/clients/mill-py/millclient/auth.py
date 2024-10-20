import base64
from abc import abstractmethod


class MillCallCredentials(object):

    @abstractmethod
    def get_metadata(self):
        pass

    @abstractmethod
    def get_headers(self):
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

    def __as_headers(self, name):
        auth = base64.b64encode(f"{self.username}:{self.password}".encode()).decode()
        return [(name, f"Basic {auth}")]

    def get_metadata(self):
        return self.__as_headers("authorization")

    def get_headers(self):
        return self.__as_headers("Authorization")

    def __str__(self):
        return f"<{self.creds_type().capitalize()}_Credentials Username='{self.username}'>"

class BearerTokenCredentials(MillCallCredentials):

    def creds_type(self):
        return "BEARER_TOKEN"

    def __init__(self, token):
        self.token = token

    def __as_headers(self, name):
        return [(name, f"Bearer {self.token}")]

    def get_metadata(self):
        return self.__as_headers("authorization")

    def get_headers(self):
        return self.__as_headers("Authorization")

    def __str__(self):
        return f"<{self.creds_type().capitalize()}_Credentials Token='HIDDEN'>"
