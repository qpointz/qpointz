                    FROM node:20 AS build

WORKDIR /src

# ChromaDB Admin is community-maintained; we clone at build time so a clean checkout can rebuild.
RUN apt-get update \
  && apt-get install -y --no-install-recommends git ca-certificates \
  && rm -rf /var/lib/apt/lists/*

ARG CHROMADB_ADMIN_REF=efe867c86c78683d90b0eb74b88b351fc08f0b5f
RUN git clone https://github.com/flanker/chromadb-admin.git /src/app \
  && cd /src/app \
  && git checkout "${CHROMADB_ADMIN_REF}"

WORKDIR /src/app
RUN npm ci
RUN npm run build

FROM node:20-slim

ENV NODE_ENV=production
WORKDIR /app

COPY --from=build /src/app ./

EXPOSE 3001
CMD ["npm", "run", "start", "--", "-p", "3001"]
