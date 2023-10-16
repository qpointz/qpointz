# Rapids Simple

Simple rapids example runing Rapids service using trivial CSV file

## Data

Local file system 

## Services

| Service                               |                                   |
|---------------------------------------|-----------------------------------|
| :white_square_button: JDBC            |                                   |
| :white_square_button: ODATA           | http://localhost:18080            |
| :black_square_button: Authentication  |                                   |
| :black_square_button: TLS/SSL         |                                   | 

To run example 

**Bash:** 
``` bash
docker run --rm -ti \
-p 18200:18200 -p 18080:18080 \
-v $(pwd):/config -v $(pwd)/../../data:/data \
qpointz/rapids-worker:latest
```

**Powershell:** 
``` powershell
docker run --rm -ti \
-p 18200:18200 -p 18080:18080 \
-v ${pwd}:/config -v ${pwd}\..\..\data:/data \
qpointz/rapids-worker:latest
```