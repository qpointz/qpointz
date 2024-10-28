#!/usr/local/bin/python
import re
import sys
import json


from semver import Version

min_version= Version(0, 2, 0)

class DocsVersion(object):
    def __init__(self, *,  version:Version = Version(0,0,0), source:str, is_tag:bool, label:str = None , order:int = 9999):
        self.version = version
        self.source = source
        self.is_tag = is_tag
        self.label = source if not label else label
        self.order = order

    def __repr__(self):
        return f"[{self.version}-{'Tag' if self.is_tag else 'Branch' }({self.source}) sort order:{self.order} as '{self.label}']"

class Versioner(object):
    def parse_versions(self, tags):
        max_version = Version(0,0,0)
        pattern = r'v(?P<fullv>(?P<v>\d+\.\d+\.\d+)(-(?P<ms>\w[\w-]+)\.(?P<msid>\d+))*)'
        vers= []
        for tag in tags:
            m = re.match(pattern, tag)
            if not m:
                continue
            v = Version.parse(m.group('v'))
            v = Version(v.major, v.minor, v.patch, m.group('ms'), m.group('msid'))

            if v.compare(max_version)>0:
                max_version = v

            vers.append(DocsVersion(version= v, source=tag, is_tag=True))
        vers.sort(key=lambda x:x.version, reverse=True)
        return vers, max_version

    def __init__(self, *, tags = [], branches = []):
        self.tags = tags
        self.branches = branches

    def strip_minimal_version(self, versions, min_version:Version):
        nv = []
        for v in versions:
            if min_version.compare(v.version) <= 0:
                nv.append(v)
        return nv


    def is_released(self, versions, version):
        rv = Version(version.major, version.minor, version.patch)
        for v in versions:
            if rv.compare(v.version) == 0:
                return True
        return False

    def strip_pre_releases(self, versions):
        nv = []
        for v in versions:
            prerelease = v.version.prerelease
            if prerelease and self.is_released(versions, v.version):
                continue
            if prerelease and str(prerelease).upper() != "RC":
                continue
            nv.append(v)
        return nv

    def select_versions(self, *, min_version = Version(0,1,0), add_dev_branches:bool = True):
        versions, max_version = self.parse_versions(self.tags)
        versions = self.strip_minimal_version(versions, min_version)
        versions = self.strip_pre_releases(versions)

        versions.sort(key=lambda x: x.version, reverse=True)

        sort_order = 100
        for v in versions:
            v.order = sort_order
            sort_order += 1

        if 'main' in branches:
            versions.append(DocsVersion(source= 'main', label ="latest", is_tag=False, order=0))

        for branch in branches:
            if branch in ['main', 'rc']:
                continue

            if not add_dev_branches:
                continue

            versions.append(DocsVersion(source= branch, is_tag=False, order=sort_order))
            sort_order += 1

        versions.sort(key=lambda x: x.order, reverse=False)
        return versions

    def whitelist(self, versions, is_tag):
        ptrns = []
        for v in versions:
            if v.is_tag != is_tag:
                continue
            t = v.source.replace('\\', '\\\\').replace('/', '\\/').replace('.', '\\.')
            ptrns.append(t)
        return f"^({'|'.join(ptrns)})$"

    def versions(self, versions):
        vrs = []
        def apnd(label, path):
            np = path.replace('/', '-')
            vrs.append(f"\"{label}\":\"{np}\"")
        for v in versions:
            apnd(v.label, v.source)
        return "{" + ','.join(vrs) + "}"

if __name__ == '__main__':
    if len(sys.argv) == 1:
        filename = ".input"
    else:
        filename = sys.argv[1]
    include_dev = False
    if len(sys.argv) == 2:
        include_dev = False
    else:
        include_dev = sys.argv[2] == "yes"

    lines = open(filename,'r').readlines()
    tags = []
    branches = []
    for line in lines:
        if line.startswith('t:'):
            tags.append(line[2:-1])
        if line.startswith('b:'):
            branches.append(line[2:-1])
    versioner = Versioner(tags = tags, branches = branches)
    versions = versioner.select_versions(min_version = Version(0,2,0), add_dev_branches=include_dev)
    print(versioner.whitelist(versions, is_tag = True))
    print(versioner.whitelist(versions, is_tag = False))
    print(versioner.versions(versions))
