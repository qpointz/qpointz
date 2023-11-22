import json 

versions=[]
versions.append((2,"aaa","aaa"))
versions.append((3,"bbb","bbb"))
versions.append((0,"ccc","ccc"))

print(versions)
versions.sort(key=lambda x:x[0])
print(versions)

nv = []
for ta in versions:
    nv.append(f"\"{ta[1]}\":\"{ta[2]}\"")
aa ="{" + ",".join(nv) + "}"
print(aa)



