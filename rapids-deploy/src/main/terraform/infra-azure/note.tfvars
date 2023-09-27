#------------------------------------------------------------------------------
#   BACKEND PARAMETERS
#------------------------------------------------------------------------------

terraform \
-chdir="rapids-deploy/src/main/terraform/infra-azure" \
init \
-backend-config="resource_group_name=qpointz-shared-rg" \
-backend-config="storage_account_name=qpioappsharedsa" \
-backend-config="container_name=tfstate" \
-backend-config="access_key=BKMQQLA2lpH3cciIikGwOAGRI5HjjkpjXwkBIxIj9i35Lrl+cMM6gHXeFf9SjCqEBVGGuZI791vy+ASth2OKSw==" \
-backend-config="subscription_id=e9ba7608-e8ee-4473-ae07-238ca0d43a08" \
-backend-config="client_id=a2dbc00d-7a29-453b-8e32-81a54c43d5e8" \
-backend-config="client_secret=Niy8Q~Bz70h0sAODU~eaNUc6UpmLMBiSOUeSRdlq" \
-backend-config="tenant_id=ded84692-aa79-4570-9369-8830771dc3ec" \
-backend-config="key=rapids.app.deploy_name.branch.tfstate"

terraform \
-chdir="rapids-deploy/src/main/terraform/infra-azure" \
apply \
-auto-approve \
-var="sp_client_id=a2dbc00d-7a29-453b-8e32-81a54c43d5e8" \
-var="sp_client_secret=Niy8Q~Bz70h0sAODU~eaNUc6UpmLMBiSOUeSRdlq" \
-var="sp_tenant_id=ded84692-aa79-4570-9369-8830771dc3ec" \
-var="deploy_subscription=e9ba7608-e8ee-4473-ae07-238ca0d43a08" \
-var="deploy_id=no_id" \
-var="deploy-name=test" \
-var="deploy-by=manual" \
-var="app-version=0.0.1-SNAPSHOT" \
-var="app-branch=dev"

terraform \
-chdir="rapids-deploy/src/main/terraform/infra-azure" \
destroy \
-auto-approve \
-var="sp_client_id=a2dbc00d-7a29-453b-8e32-81a54c43d5e8" \
-var="sp_client_secret=Niy8Q~Bz70h0sAODU~eaNUc6UpmLMBiSOUeSRdlq" \
-var="sp_tenant_id=ded84692-aa79-4570-9369-8830771dc3ec" \
-var="deploy_subscription=e9ba7608-e8ee-4473-ae07-238ca0d43a08" \
-var="deploy_id=no_id" \
-var="deploy-name=test" \
-var="deploy-by=manual" \
-var="app-version=0.0.1-SNAPSHOT" \
-var="app-branch=dev"