#!/usr/bin/python3
import os
import re
import sys

def scan(indir, outfilepath):
    pattern=r'(?P<type>^[\w-]+)-v(er)*(?P<fullversion>(?P<version>\d+\.\d+\.\d+)(-(?P<milestone>\w[\w-]+))*)'
    versions=[]
    va=[]
    for a in os.listdir(indir):
        fullpath=os.path.join(indir,a)
        if os.path.isfile(fullpath):
            print(f"skipping file {fullpath}")
            continue

        va.append(a)
        if 'dev' == a:
            print("development version")
            versions.append((1, "development","dev"))    
            continue
        if 'master' == a or 'main' == a:
            print("stable version")
            versions.append((0, "stable" , "stable"))
            continue

        match = re.match(pattern, a)

        if (match and match.groupdict().get('fullversion')):
            path=a
            fversion=match.group('fullversion')
            print(f"version:{fversion} => path:{path}")
            versions.append((2, f"v{fversion}" , f"{path}"))
        else:
            versions.append((3, f"branch-{a}" , f"{a}" ))
            print(f"version:branch-{a} => path:{a}")

    if (os.path.exists(outfilepath)):
        print(f"{outfilepath} exists.deleting")
        os.remove(outfilepath)
    
    versions.sort(key=lambda x:x[0])
    nv = []
    for ta in versions:
        nv.append(f"\"{ta[1]}\":\"{ta[2]}\"")
    json ="{" + ",".join(nv) + "}"
    with open(outfilepath, "w") as outfile:
        outfile.write(json)


if __name__ == '__main__':
    if len(sys.argv) < 3:
        sys.exit(f"expects {sys.argv[0]} INPUTDIR OUTPUTFILE")
        
    indir=sys.argv[1]
    outfile=sys.argv[2]
    scan(indir, outfile)