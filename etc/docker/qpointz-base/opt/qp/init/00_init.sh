if [[ -z $QP_USER_NAME ]]; then
    echo '$QP_USER_NAME not set!!!!'
    QP_USER_NAME=qp
fi


if [[ -z $QP_USER_FULL_NAME ]]; then
    echo '$QP_USER_FULL_NAME not set!!!!'
    QP_USER_FULL_NAME='Qp User'
fi

if [[ -z $QP_USER_EMAIL ]]; then
    echo '$QP_USER_EMAIL not set!!!!'
    QP_USER_EMAIL='qp@qpointz.io'
fi

if [[ -z $QP_ORG ]]; then
    echo '$QP_ORG not set!!!!'
    QP_ORG='Qpointz IO'
fi

if [[ -z $QP_ORG_DOMAIN ]]; then
    echo '$QP_ORG_DOMAIN not set!!!!'
    QP_ORG_DOMAIN='qpointz.io'
fi

if [[ -z $QP_HOSTNAME ]]; then
    echo '$QP_HOSTNAME not set!!!!'
    QP_HOSTNAME='basehost'
fi

getent passwd $QP_USER_NAME  > /dev/null
if [[ $? -ne 0 ]]; then
    useradd --create-home $QP_USER_NAME
fi

cat <<EOF > /opt/qp/usr/00_init.sh
export QP_USER_NAME="$QP_USER_NAME"
export QP_USER_FULL_NAME="$QP_USER_FULL_NAME"
export QP_USER_EMAIL="$QP_USER_EMAIL"
export QP_ORG="$QP_ORG"
export QP_ORG_DOMAIN="$QP_ORG_DOMAIN"
export QP_HOSTNAME="$QP_HOSTNAME"
EOF