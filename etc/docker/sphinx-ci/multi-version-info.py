#!/usr/bin/python3
import os
import re
import json
import sys

def scan(indir, outfilepath):
    pattern=r'(?P<type>^[\w-]+)-v(er)*(?P<fullversion>(?P<version>\d+\.\d+\.\d+)(-(?P<milestone>\w[\w-]+))*)'
    versions={}
    va=[]
    for a in os.listdir(indir):
        fullpath=os.path.join(indir,a)
        if os.path.isfile(fullpath):
            print(f"skipping file {fullpath}")
            continue

        print(f"matching {a}")
        va.append(a)
        if 'dev' == a:
            print("development version")
            versions.update({"development" : "dev/"})    

        match = re.match(pattern, a)
        path=a
        fversion=match.group('fullversion')
        print(f"version:{fversion} => path:{path}")
        versions.update({f"v{fversion}" : f"{path}/"})
    if (os.path.exists(outfilepath)):
        print(f"{outfilepath} exists.deleting")
        os.remove(outfilepath)
    with open(outfilepath, "w") as outfile:
        json.dump(versions, outfile)


if __name__ == '__main__':
    if len(sys.argv) < 3:
        sys.exit(f"expects {sys.argv[0]} INPUTDIR OUTPUTFILE")
        
    indir=sys.argv[1]
    outfile=sys.argv[2]
    scan(indir, outfile)