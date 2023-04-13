import numbers
from faker import Faker
from faker_airtravel import AirTravelProvider
import random
import yaml
from pathlib import Path
from collections import namedtuple
import pandas as pd

AttributeG = namedtuple("AttributeG", "key gen params")

class MultiModelsFaker:

    def __init__(self, models):
        self.models = {}
        for m in models:
            name = m['name']
            self.models.update({name: MultiModelsFaker.ModelFaker(m)})
        pass

    def explain(self):
        print(self.models)
        pass

    def generate_all(self):
        for k, m in self.models.items():
            m.generate()


    class ModelFaker:
        def __init__(self, model):
            if 'locales' not in model:
                model.update({'locales': 'en-US'})
            locales = model['locales']
            if 'rows' not in model:
                model.update({'rows': {'min': 1, 'max': 100}})
            if 'schemas' not in model:
                model.update({'schemas', []})
            self.model = model
            self.generated = {}


        def generate(self):
            self.generated = {}
            faker = Faker(locale=self.model['locales'])
            faker.add_provider(AirTravelProvider)
            for schema in self.model['schemas']:
                r = self.__generate_schema(faker, schema)
                key = schema['name']
                self.generated.update({key: r})


        def __resolve_gen(self, f, c):
            gn = c['type']
            if gn == "${ref}":
                ds = c['params']['dataset']
                col = c['params']['attribute']
                pd = self.generated[ds]
                def g(*args, **kwargs):
                    return random.choice(list(pd[col].values))
                return g
            elif hasattr(f, gn):
                g = getattr(f, gn)
                return g
            else:
                raise Exception(f"Unknown generator {gn}")


        def __generate_schema(self, fake, schema):
            gens=[]
            for col in schema['attributes']:
                if 'params' not in col:
                    params = {}
                else:
                    params = col['params']
                gens.append(AttributeG(col['name'],self.__resolve_gen(fake,col),params))
            headers = [g.key for g in gens]
            rows = []

            rowobj = schema['rows']
            rowstogen = 0
            if isinstance(rowobj, numbers.Number):
                rowstogen = rowobj
            else:
                if type(rowobj) is dict:
                    minrows=0
                    maxrows=0
                    if 'min' not in rowobj:
                        minrows = 0
                    else:
                        minrows = int(rowobj['min'])
                    if 'max' not in rowobj:
                        maxrows = 10000
                    else:
                        maxrows = int(rowobj['max'])
                    rowstogen = fake.random.randint(minrows, maxrows)
                else:
                    raise Exception("rows spec not supported")

            for index in range(1, rowstogen +1):
                row = [g.gen(**g.params) for g in gens]
                rows.append(row)
            df = pd.DataFrame(rows, columns=headers)
            return df


def from_model_file(p):
    with open(p, 'r') as yamlstream:
        mp = yaml.safe_load(yamlstream)
        return MultiModelsFaker(mp['models'])


if __name__ == '__main__':
    absPath = (Path(__file__).parent / "models.yaml").absolute()
    df = from_model_file(absPath)
    df.generate_all()