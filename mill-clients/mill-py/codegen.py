import glob
import os
import shutil
from pathlib import Path

import betterproto
from grpc_tools import protoc

if __name__ == "__main__":
    cwd = os.getcwd()
    print(f"current dir {cwd}")
    abspath = os.path.abspath(__file__)
    dname = os.path.dirname(abspath)
    print(f"change to {dname}")
    os.chdir(dname)
    out_dit = "./millclient/proto"

    if os.path.exists(out_dit) and os.path.isdir(out_dit):
        shutil.rmtree(out_dit)
    os.makedirs(out_dit)

    args = [
        "",
        # "--python_out=./qpointz_delta",
        "-I../../mill/proto",
        # "--pyi_out=./stubs",
        # "--grpc_python_out=./qpointz_delta",
        "--python_betterproto_out="+out_dit
    ]

    args = args + glob.glob("../../mill/proto/**/*.proto") + glob.glob("../../mill/proto/*.proto")
    print(f"Arguments:{args}")
    protoc.main(command_arguments=args)
    print(f"change to {cwd}")
    os.chdir(cwd)