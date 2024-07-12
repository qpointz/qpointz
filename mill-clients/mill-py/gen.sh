#!/usr/bin/env sh

#mkdir -p qpointz_mill/libs
#mkdir -p stubs/libs

#python -m grpc_tools.protoc -I../../mill/proto \
#    --pyi_out=./stubs \
#    --python_out=./qpointz_mill \
#    --grpc_python_out=./qpointz_mill  \
#    --python_betterproto_out=./qpointz_mill/libs \
#    ../../mill/proto/*.proto ../../mill/proto/substrait/*.proto ../../mill/proto/substrait/extensions/*.proto

python -m grpc_tools.protoc -I../../mill/proto \
    --python_betterproto_out=./qpointz_mill/libs \
    ../../mill/proto/*.proto ../../mill/proto/substrait/*.proto ../../mill/proto/substrait/extensions/*.proto

python -m grpc_tools.protoc -I../../mill/proto \
    --python_betterproto_out=./bp_out/libs \
    ../../mill/proto/*.proto ../../mill/proto/substrait/*.proto ../../mill/proto/substrait/extensions/*.proto

mkdir -p ./qpmill/proto && rm -Rf ./qpmill/proto && mkdir -p ./qpmill/proto && \
mkdir -p ./stubs && rm -Rf ./stubs && mkdir -p ./stubs && \
python -m grpc_tools.protoc -I../../mill/proto \
    --pyi_out=./stubs \
    --python_out=./qpmill/proto \
    --grpc_python_out=./qpmill/proto  \
    ../../mill/proto/*.proto ../../mill/proto/substrait/*.proto ../../mill/proto/substrait/extensions/*.proto


docker run --rm -ti \
  -p 9099:9099  -e QP_MILL_BACKEND_SECURITY_ENABLED=false \
  registry.qpointz.io/qpointz/qpointz/mill-calcite-backend:v0.2.0-dev

  docker run --rm -ti -p 9099:9099  -e QP_MILL_BACKEND_SECURITY_ENABLED=false registry.qpointz.io/qpointz/qpointz/mill-calcite-backend:v0.2.0-dev