CREATE DATABASE authentik;

CREATE USER authentik WITH PASSWORD 'authentik';

GRANT ALL PRIVILEGES ON DATABASE authentik TO authentik;

ALTER DATABASE authentik OWNER TO authentik;