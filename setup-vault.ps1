# =============================================================================
# HashiCorp Vault AppRole & Secret Setup Script (Sales Document Portal)
# =============================================================================

Write-Host "Starting HashiCorp Vault Initialization..." -ForegroundColor Cyan

$VAULT_ADDR = "http://127.0.0.1:8200"
$ROOT_TOKEN = "root_dev_token"
$CONTAINER = "sales_portal_vault"

# 1. Enable KV Secret Engine v2 at path 'secret'
Write-Host "1. Enabling KV-V2 Secrets Engine..." -ForegroundColor Yellow
docker exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$ROOT_TOKEN $CONTAINER vault secrets enable -path=secret kv-v2

# 2. Store DB Credentials & Master Key into Vault
Write-Host "2. Writing Dynamic Secrets to Vault..." -ForegroundColor Yellow
docker exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$ROOT_TOKEN $CONTAINER vault kv put secret/sales-document-portal db_password="sales_password_secret" master_key_hex="4e616d655365637572654d61737465724b657953616c6573506f7274616c3132"

# 3. Create Vault Access Policy for Spring Boot AppRole
Write-Host "3. Creating ACL Policy 'sales-portal-policy'..." -ForegroundColor Yellow
'path "secret/data/sales-document-portal" { capabilities = ["read"] }' | docker exec -i -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$ROOT_TOKEN $CONTAINER vault policy write sales-portal-policy -

# 4. Enable AppRole Auth Method
Write-Host "4. Enabling AppRole Authentication Method..." -ForegroundColor Yellow
docker exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$ROOT_TOKEN $CONTAINER vault auth enable approle

# 5. Bind Policy to AppRole 'sales-portal-role'
Write-Host "5. Binding Policy to AppRole 'sales-portal-role'..." -ForegroundColor Yellow
docker exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$ROOT_TOKEN $CONTAINER vault write auth/approle/role/sales-portal-role secret_id_ttl=0 token_num_uses=0 token_ttl=1h token_max_ttl=4h policies="sales-portal-policy"

# 6. Generate Role ID & Secret ID
Write-Host "6. Fetching Role ID & Secret ID..." -ForegroundColor Green
$ROLE_ID_RAW = docker exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$ROOT_TOKEN $CONTAINER vault read -format=json auth/approle/role/sales-portal-role/role-id
$ROLE_ID = ($ROLE_ID_RAW | ConvertFrom-Json).data.role_id

$SECRET_ID_RAW = docker exec -e VAULT_ADDR=$VAULT_ADDR -e VAULT_TOKEN=$ROOT_TOKEN $CONTAINER vault write -f -format=json auth/approle/role/sales-portal-role/secret-id
$SECRET_ID = ($SECRET_ID_RAW | ConvertFrom-Json).data.secret_id

Write-Host "=================================================================" -ForegroundColor Green
Write-Host "Vault Setup Completed Successfully!" -ForegroundColor Green
Write-Host "AppRole ROLE_ID   : $ROLE_ID" -ForegroundColor Cyan
Write-Host "AppRole SECRET_ID : $SECRET_ID" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Green
 