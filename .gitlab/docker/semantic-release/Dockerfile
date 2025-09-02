 FROM node:24-alpine

RUN apk add git jq && \
    npm install -g semantic-release semantic-release-unsquash @semantic-release/exec \
        conventional-changelog-conventionalcommits \
        @semantic-release/gitlab-config @semantic-release/gitlab \
        @semantic-release/changelog 