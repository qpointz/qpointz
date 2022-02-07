Running behind reverse proxy
===========================

While running behind proxy it is important to ensure extra headers which is required to
resolve external host name and scheme being passed by proxy to enzyme


NGINX
-----

Below configuration is redirecting any http traffic to https
and redirects all incoming traffic to http enzyme port

.. code-block:: text

    upstream enzyme-dev {
        #set enzyme http port
        server localhost:17080;
    }

    ## Redirects all HTTP traffic to the HTTPS host
    server {
        listen 0.0.0.0:80;
        server_name enzyme-dev.qpointz.io;
        server_tokens off;
        return 301 https://$http_host$request_uri;
        access_log  /var/log/nginx/enzyme_dev_access.log;
        error_log   /var/log/nginx/enzyme_dev_error.log;
    }

    ## HTTPS host
    server {
        listen 0.0.0.0:443 ssl;
        server_name enzyme-dev.qpointz.io;
        server_tokens off;

        ## Strong SSL Security
        ssl on;
        ssl_certificate /etc/nginx/ssl/qpointz.crt;
        ssl_certificate_key /etc/nginx/ssl/qpointz.key;

        ## Individual nginx logs for this GitLab vhost
        access_log  /var/log/nginx/enzyme_dev_access.log;
        error_log   /var/log/nginx/enzyme_dev_error.log;

        location / {
            client_max_body_size 0;
            gzip on;

            # Some requests take more than 30 seconds.
            proxy_read_timeout      300;
            proxy_connect_timeout   300;
            proxy_redirect          off;

            proxy_http_version 1.1;

            #Headers required to resolve external url
            proxy_set_header    Host                $http_host;
            proxy_set_header    X-Real-IP           $remote_addr;
            proxy_set_header    X-Forwarded-Ssl     on;
            proxy_set_header    X-Forwarded-For     $proxy_add_x_forwarded_for;
            proxy_set_header    X-Forwarded-Proto   $scheme;
            proxy_set_header    X-Frame-Options     SAMEORIGIN;

            proxy_pass http://enzyme-dev;
        }
    }