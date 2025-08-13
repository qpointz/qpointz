#!/usr/local/bin/python
import re
import sys

from semver import Version


def sorttags(tags):
    pattern = r'v(?P<fullv>(?P<v>\d+\.\d+\.\d+)(-(?P<ms>\w[\w-]+)\.(?P<msid>\d+))*)'
    vers= []
    for tag in tags:
        m = re.match(pattern, tag)
        if not m:
            continue
        v = Version.parse(m.group('v'))
        v = Version(v.major, v.minor, v.patch, m.group('ms'), m.group('msid'))
        vers.append([v,tag])
    vers.sort(key=lambda x:x[0], reverse=True)
    def cv(vrs):
        return Version(vrs.major, vrs.minor, vrs.patch)
    topver = cv(vers[0][0])
    topisreleased = len(list(filter(lambda x:cv(x[0]).compare(topver) == 0 and not x[0].prerelease, vers)))>0
    cleanver = []
    for v in vers:
        cver = v[0]
        def vlabel(v):
            r = str(cv(v))
            if not v.prerelease:
                return f"{r}"
            return f"{r} {str(v.prerelease).upper()}{v.build}"
        def apcl():
            cleanver.append([vlabel(v[0]), v[1]])
        if (topver.compare(cver)==0):
            apcl()
            continue
        if (topver.compare(cv(cver))==0 and str(cver.prerelease or "").upper() == "RC" and not topisreleased):
            apcl()
            continue
        if (not cver.prerelease):
            apcl()
    return cleanver
    pass

def whitelist(tags, idx = 1):
    ptrns=[]
    for tag in tags:
        t = tag[idx]
        t= t.replace('\\', '\\\\').replace('/', '\\/').replace('.', '\\.')
        ptrns.append(t)
    print(f"^({'|'.join(ptrns)})$")

def sortbranches(branches):
    res=[]
    if 'main' in branches:
        res.append(['latest', 'latest', 'main', 0])

    if 'dev' in branches:
        res.append(['develop', 'dev', 'dev', 1])

    branches.sort()
    for branch in branches:
        if branch in ['main', 'dev','rc']:
            continue
        res.append([branch, branch, branch, 2])
    res.sort(key=lambda x:x[3])
    return res

def versions(branches, tags):
    vrs = []
    def apnd(label, path):
        np = path.replace('/','-')
        vrs.append(f"\"{label}\":\"{np}\"")
    for branch in enumerate(filter(lambda x: x[3] == 0, branches)):
        apnd(branch[1][0], branch[1][1])
    for tag in tags:
        apnd(tag[0], tag[1])
    for branch in enumerate(filter(lambda x: x[3] != 0, branches)):
        apnd(branch[1][0], branch[1][1])
    return "{"+','.join(vrs)+"}"


if __name__ == '__main__':
    if len(sys.argv) == 1:
        filename = ".input"
    else:
        filename = sys.argv[1]
    lines = open(filename,'r').readlines()
    tags = []
    branches = []
    for line in lines:
        if line.startswith('t:'):
            tags.append(line[2:-1])
        if line.startswith('b:'):
            branches.append(line[2:-1])
    seltags = sorttags(tags)
    whitelist(seltags, 1)
    selbranches = sortbranches(branches)
    whitelist(selbranches, 2)
    print(versions(selbranches, seltags))