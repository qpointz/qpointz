class MillError(Exception):
    pass


class MillServerError(MillError):
    def __init__(self, message, origin: Exception):
        self.origin = origin

    pass
