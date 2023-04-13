import os

from pandas import DataFrame


class Writer:
    def write(self, path, pd: DataFrame, writeparams={}):
        pass


class CsvWriter(Writer):
    def write(self, path, pd: DataFrame, writeparams={}):
        pd.to_csv(path, **writeparams)


class ParquetWriter(Writer):
    def write(self, path, pd: DataFrame, writeparams={}):
        pd.to_parquet(path, **writeparams)


def ensure_path(ap):
    if os.path.exists(ap):
        os.remove(ap)
    else:
        if not os.path.exists(ap.parent):
            os.makedirs(ap.parent)


def get_writer(name) -> Writer:
    if name == 'csv':
        return CsvWriter()

    if name == 'parquet':
        return ParquetWriter()

    raise Exception(f"Unknown writer '{name}'")