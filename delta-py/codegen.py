import glob
import os
import betterproto
from grpc_tools import protoc

if __name__ == "__main__":
    cwd = os.getcwd()
    print(f"current dir {cwd}")
    abspath = os.path.abspath(__file__)
    dname = os.path.dirname(abspath)
    print(f"change to {dname}")
    os.chdir(dname)

    os.listdir()

    args = [
     "",
     # "--python_out=./qpointz_delta",
     "-I../delta/proto",
     # "--pyi_out=./stubs",
     # "--grpc_python_out=./qpointz_delta",
     "--python_betterproto_out=./qpointz_delta/libs"
    ]

    args = args + glob.glob("../delta/proto/**/*.proto") + glob.glob("../delta/proto/*.proto")
    print(f"Arguments:{args}")
    protoc.main(command_arguments=args)
    print(f"change to {cwd}")
    os.chdir(cwd)
