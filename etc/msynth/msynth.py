import argparse
from faker_airtravel import AirTravelProvider
from faker import Faker

import experiments


def list_providers():
    faker = Faker()
    faker.add_provider(AirTravelProvider)
    p = dir(faker)
    for a in p:
        if a.startswith('_'):
            continue
        print(a)
    pass


if __name__ == '__main__':
    argp = argparse.ArgumentParser(
        prog="Synth model runner")
    argp.add_argument('--input-file', '-i', metavar='input', required=True)
    argp.add_argument('--info', action='store_true')
    argp.add_argument('--list-providers', '-lp', action='store_true')
    argp.add_argument('--experiment', '-e', nargs='+', metavar='experiments')
    parsed = argp.parse_args()
    if (parsed.list_providers):
        list_providers()

    if (len(parsed.experiment)>0):
        experiments.run_experiments(parsed.input_file, *parsed.experiment)