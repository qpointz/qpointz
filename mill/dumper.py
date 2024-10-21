import os
import shutil
from pathlib import Path
import yaml

class Dumper:

    def __init__(self, root_dir:str):
        self.root_dir = root_dir

    def move_package(self, *, config=None, src_pkg:str=None, dst_pkg:str=None):
        if config:
            src_pkg = config['from']
            dst_pkg = config['to']
        print(f"==== Moving package {src_pkg} to {dst_pkg}")
        old_sp = src_pkg.replace(".", os.sep)
        new_sp = dst_pkg.replace(".", os.sep)
        for root, subdirs, files in os.walk(self.root_dir):
            for sd in subdirs:
                np = os.path.join(root, sd)
                if np.find(old_sp) >= 0 and os.path.exists(np):
                    new_path = np.replace(old_sp, new_sp)
                    print(f"Copy:{np} -> {new_path}")
                    if (os.path.exists(new_path)):
                        print(f"{new_path} already exists, deleting")
                        shutil.rmtree(new_path)
                    shutil.move(np, new_path)
                    print(f"Deleting {np}")

    def delete_dir(self, * , config = None, dirs:list = None):
        def delete(subdir):
            fp = os.path.join(self.root_dir, subdir)
            if os.path.exists(fp):
                print(f"==== Deleting {fp}")
                shutil.rmtree(fp)
        if config:
            dirs = config['paths']
        for dir in dirs:
            delete(dir)


    def delete_empty_folder(self):
        print("==== Deleting empty folder")
        deleted = set()
        for current_dir, subdirs, files in os.walk(self.root_dir, topdown=False):
            still_has_subdirs = False
            for subdir in subdirs:
                if os.path.join(current_dir, subdir) not in deleted:
                    still_has_subdirs = True
                    break

            if not any(files) and not still_has_subdirs:
                os.rmdir(current_dir)
                print(f"Deleting {current_dir}")
                deleted.add(current_dir)
        return deleted

    def replace_in_files(self, *, config=None, old: str=None, new: str=None, exts: set=None):
        def replace(old:str, new:str, exts:set):
            print(f"==== Replacing in files {old} with {new}, {exts}")
            def replace_content(f):
                print(f"Replacing in:{f}")
                # Read in the file
                with open(f, 'r') as file:
                    filedata = file.read()

                # Replace the target string
                filedata = filedata.replace(old, new)

                # Write the file out again
                with open(f, 'w') as file:
                    file.write(filedata)
            ignored = []
            for current_dir, subdirs, files in os.walk(self.root_dir, topdown=False):
                for f in files:
                    full_path=os.path.join(current_dir, f)
                    ext = Path(full_path).suffix
                    if full_path.find(".venv"+os.sep)>0 or full_path.find("venv"+os.sep)>0:
                        continue
                    if ext in exts:
                        replace_content(full_path)
                    else:
                        ignored.append(ext)
            print(set(ignored))
        if not config:
            replace(old, new, exts)
            return
        exts = config['exts']
        for r in config['replace']:
            replace(r['old'], r['new'], exts)


    def delete_files(self, *, config=None, paths:list=None):
        def delete(path):
            full_path = os.path.join(self.root_dir, path)
            print(f"==== Deleting {full_path}")
            if os.path.exists(full_path):
                os.remove(full_path)
        if config:
            paths = config['paths']
        for p in paths:
            delete(p)


    def run_action(self, action):
        action_key = list(action.keys())[0]
        config = action[action_key]
        match action_key:
            case "move_package":
                self.move_package(config = config)
            case "delete_dir":
                self.delete_dir(config = config)
            case "delete_file":
                self.delete_files(config = config)
            case "delete_empty_folder":
                self.delete_empty_folder()
            case "replace_in_files":
                self.replace_in_files(config = config)
            case _:
                raise ValueError(f"Invalid action: {action}")



def run_actions(rs):
    root_dir = os.path.abspath(rs['dump']['in']['root_dir'])
    d = Dumper(root_dir)
    actions = rs['dump']['actions']
    for action in actions:
        d.run_action(action)

if __name__ == '__main__':
    with open(".dumper.yml", 'r') as stream:
        rs = yaml.safe_load(stream)
        run_actions(rs)


    # root_dir="/home/vm/wip/qpointz/qpointz/mill"
    # old_pkg = "io.qpointz.mill"
    # new_pkg = "com.tras.bas.yuki"
    #
    # t = old_pkg.split(".")
    # t.reverse()
    # new_forward_pkg = ".".join(t)
    #
    # d = Dumper(root_dir)
    # d.move_files(old_pkg, new_pkg)
    # d.delete_empty_folders()
    # d.delete_dir("clients/mill-py/millclient/proto/io")
    # d.delete_dir("clients/mill-spark")
    # d.delete_dir("clients/etc/mill-test")
    #
    # default_exts = {'.java', '.py', '.kt', '.kts', '.Driver', '.proto', '.json'}
    # d.replace_in_file(old = old_pkg, new=new_pkg, exts=default_exts)
    # d.replace_in_file(old = 'io.qpointz', new=new_pkg, exts=default_exts)
    # d.replace_in_file(old = 'qpointz.io', new = new_forward_pkg, exts=default_exts)
    # d.replace_in_file(old='qpointz', new="yuki", exts=default_exts)
    #
    # d.delete_files("clients/mill-py/protoc.txt")
    # d.delete_files("clients/mill-py/gen.sh")
    # d.delete_files(".sdkmanrc")
    # d.delete_files(".gitlab-ci.yml")
    # d.delete_files("clients/.gitlab-ci.yml")
    #
