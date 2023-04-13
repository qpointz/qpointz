import os.path
import sys

from croniter import croniter
from datetime import datetime

import yaml
from pathlib import Path
from pandas import DataFrame
import models
import writers


class Experiment:
    pass
class CronFeedExperiment(Experiment):
    def __init__(self, p, models):
        self.models = models
        self.p = p

    def run(self):
        datesp = self.p['dates']
        from_date = datetime.now()
        to_date = datetime.max
        count = sys.maxsize
        if 'to' not in datesp and 'count' not in datesp:
            raise Exception("One of parameters 'to' or 'count' must be present")

        if 'from' in datesp:
            from_date = datetime.strptime(datesp['from'], '%Y-%m-%d')
        if 'to' in datesp:
            to_date = datetime.strptime(datesp['to'], '%Y-%m-%d')
        if 'count' in datesp:
            count = datesp['count']
        if from_date >= to_date:
            raise Exception("'from_date' can't be greater as 'to_date'")

        i = 0
        cur_date = from_date
        iter = croniter(self.p['cron'], from_date)
        path_template = str(self.p['path'])
        writer = writers.get_writer(self.p['writer']['name'])
        writeparams = self.p['writer']['params']
        while i < count and cur_date <= to_date:
            cur_date = iter.next(datetime)
            mmf = models.MultiModelsFaker(self.models)
            mmf.generate_all()
            for mn , m in mmf.models.items():
                for gn , g in m.generated.items():
                    p = {'model-name':mn, 'dataset-name':gn, 'cron-date' : cur_date}
                    ap = Path(str(path_template).format(**p)).absolute()
                    writers.ensure_path(ap)
                    writer.write(ap, g, writeparams)




class WriteExperiment:
    def __init__(self, p, models):
        self.path = p['path']
        self.models = models
        if 'params' not in p:
            self.writeParams = {}
        else:
            self.writeParams = p['params']

    def run(self):
        mmf = models.MultiModelsFaker(self.models)
        mmf.generate_all()
        w = self.writer()
        for mn , m in mmf.models.items():
            for gn, g in m.generated.items():
                p = {'model-name':mn, 'dataset-name':gn}
                ap = Path(str(self.path).format(**p)).absolute()
                writers.ensure_path(ap)
                w.write(ap, g, self.writeParams)

    def writer(self) -> writers.Writer:
        pass

class CsvWriteExperiment(WriteExperiment):
    def writer(self) -> writers.Writer:
        return writers.CsvWriter()

class ParquetWriteExperiment(WriteExperiment):
    def writer(self) -> writers.Writer:
        return writers.ParquetWriter()

class Experiments:
    def __init__(self, exps, models):
        self.models = models
        self.exps = exps
    def run(self, name):
        e = self.exps[name]
        def get_by_type():
            if 'type' not in e:
                raise Exception(f"Experiment {name} type is missing")
            et = e['type']
            if et =='csv':
                return CsvWriteExperiment(e, self.models)
                pass
            if et =='parquet':
                return ParquetWriteExperiment(e, self.models)
                pass
            if et == 'cron_feed':
                return CronFeedExperiment(e, self.models)
                pass
            raise Exception(f"Unknown experiment type {et}")
        inst = get_by_type()
        inst.run()


def load(p):
    with open(p, 'r') as yamlstream:
        mp = yaml.safe_load(yamlstream)
        return Experiments(mp['experiments'], mp['models'])


def run_experiments(path, *args):
    input = load(Path(path).absolute())
    for experiment in args:
        input.run(experiment)

