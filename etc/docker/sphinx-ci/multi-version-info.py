#!/usr/bin/python3
import os
import re
import sys

def vertoint(ver):
    major, minor, build = map(int, ver.split("."))
    major = (major << 24) & 0xFF000000
    minor = (minor << 16) & 0x00FF0000
    build = build & 0x00000FFF
    return 1667432547 - int (major | minor | build)

def scan(indir, outfilepath):
    pattern=r'v(er)*(?P<fullversion>(?P<version>\d+\.\d+\.\d+)(-(?P<milestone>\w[\w-]+))*)'
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
            versions.append(("44444444444444444-", "development","dev"))    
            continue

        if 'rc' == a:
            print("rc version. skipping")            
            continue

        if 'main' == a:
            print("stable version")
            versions.append(("11111111111111111-", "latest" , "latest"))
            continue

        match = re.match(pattern, a)

        if (match and match.groupdict().get('fullversion')):
            path=a
            fversion=match.group('fullversion')
            sv = vertoint(match.groupdict().get('version'))
            ms = match.groupdict().get('milestone') or ""
            print(f"version:{fversion} => path:{path}")            
            versions.append((f"33333333333333333-{sv}-{ms}", f"v{fversion}" , f"{path}"))
        else:
            versions.append((f"55555555555555555-{a}", f"{a}" , f"{a}" ))
            print(f"version:{a} => path:{a}")

    if (os.path.exists(outfilepath)):
        print(f"{outfilepath} exists.deleting")
        os.remove(outfilepath)
    
    versions.sort(key=lambda x:x[0])
    nv = []
    for ta in versions:
        nv.append(f"\"{ta[1]}\":\"{ta[2]}\"")
        print(f"{ta}")
    json ="{" + ",".join(nv) + "}"
    with open(outfilepath, "w") as outfile:
        outfile.write(json)


if __name__ == '__main__':
    if len(sys.argv) < 3:
        sys.exit(f"expects {sys.argv[0]} INPUTDIR OUTPUTFILE")
        
    indir=sys.argv[1]
    outfile=sys.argv[2]
    scan(indir, outfile)