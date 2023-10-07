from jinja2 import Template
import json
from types import SimpleNamespace
import os
from deepmerge import Merger
import collections

my_merger = Merger(
    # pass in a list of tuple, with the
    # strategies you are looking to apply
    # to each type.
    [
        (list, ["append"]),
        (dict, ["merge"]),
        (set, ["union"])
    ],
    # next, choose the fallback strategies,
    # applied to all other types:
    ["override"],
    # finally, choose the strategies in
    # the case where the types conflict:
    ["override"]
)

def gen_all(filename):
    f = open(filename)
    data = json.load(f) #, object_hook=lambda d: SimpleNamespace(**d))
    #data = json.load(f)
    types = data['types']

    for x in types:
            n = x #.copy()
            if 'templates' in x:
                for tk in x['templates']:
                    n = my_merger.merge(n, data['templates'][tk])
            print(n)
            object_name = collections.namedtuple("ObjectName", n.keys())(*n.values())
            gen(object_name)

def gen(data):
    tc = None
    with open('templates/template.html') as tf:
        tc = tf.read()

    t = Template(tc)
    o = t.render(data=data)

    if not os.path.exists("out"):
        os.mkdir("out")

    outfilename = 'out/' + data.output + data.type + ".scala"
    dirc = os.path.dirname(outfilename)
    if not os.path.exists(dirc):
        os.makedirs(dirc)

    with open(outfilename, 'w') as of:
        of.write(o)


if __name__ == '__main__':
    gen_all('types.json')


