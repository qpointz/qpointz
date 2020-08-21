#!/usr/bin/env bash

if [[ ! -f $HOME/.aws/credentials ]]; then

    echo "AWS credentials file doesn't exists"
    mkdir -p $HOME/.aws

    cat <<EOF >> $HOME/.aws/credentials
[default]
aws_access_key_id=$AWS_ACCESS_KEY_ID
aws_secret_access_key=$AWS_SECRET_ACCESS_KEY
EOF

fi

export PATH=/usr/local/aws-cli/v2/current/bin:$PATH

complete -C '/usr/local/aws-cli/v2/current/bin/aws_completer' aws
