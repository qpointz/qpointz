import glob
import os
import shutil
from pathlib import Path
import site 
from grpc_tools import protoc

if __name__ == "__main__":
    print(f"script path {os.path.realpath(__file__)}")
    cwd = os.getcwd()
    print(f"current dir {cwd}")
    abspath = os.path.abspath(__file__)
    dname = os.path.dirname(abspath)
    print(f"change to {dname}")
    os.chdir(dname)
    out_dir = Path("./millclient/proto").resolve()
    print(f"Out dir: {out_dir}")
    in_dir = Path("../../proto").resolve()
    print(f"In dir: {in_dir}")

    if os.path.exists(out_dir) and os.path.isdir(out_dir):
        shutil.rmtree(out_dir)
    os.makedirs(out_dir)

    args = [
        "",
        # "--python_out=./qpointz_delta",
        f"-I{in_dir}",
        "-I/usr/include/",
        # "--pyi_out=./stubs",
        # "--grpc_python_out=./qpointz_delta",
        f"--python_betterproto_out={out_dir}"
    ]

    sitePckPath = os.path.join(site.getsitepackages()[0], "grpc_tools/_proto")
    if os.path.exists(sitePckPath):
        print (f"add proto inport {sitePckPath}")
        args.append(f"-I{sitePckPath}")

    args = args + glob.glob(f"{in_dir}/**/*.proto") + glob.glob(f"{in_dir}/*.proto")
    print(f"Arguments:{args}")
    protoc.main(command_arguments=args)
    print(f"change to {cwd}")
    os.chdir(cwd)
