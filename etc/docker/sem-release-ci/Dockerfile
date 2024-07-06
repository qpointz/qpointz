FROM node:21.7-alpine3.18

RUN apk add git jq && \
    npm install -g semantic-release @semantic-release/exec \
        conventional-changelog-conventionalcommits \
        @semantic-release/gitlab-config @semantic-release/gitlab \
        @semantic-release/changelog