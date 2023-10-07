#!/usr/bin/env python3
import sys
import requests
import os


def upload(rootDir, projectGroup, project, branch, folder):
    for root, dirs, files in os.walk(rootDir, topdown=True):
        for name in files:
            path = os.path.join(root, name)
            relpath = os.path.relpath(path, args[1])
            abspath = os.path.abspath(path)
            url = "https://nexus.qpointz.io/repository/publish-ci/" + projectGroup + "/" + project + "/" + branch + "/" + folder + "/" + relpath
            resp = requests.put(url, auth=('vm', 'zanoza1024'), files={name: open(abspath, 'rb')})
            print(resp.status_code)
            print(resp.content)
            pass


if __name__ == "__main__":
    args = sys.argv
    upload(args[1], "qpointz", "testprj", "dev-branch", "checkstyle")
