param([string]$BaseUrl = "http://localhost:8080")
$ErrorActionPreference = "Stop"
$tokens = @{}

function Login([string]$Username) {
    $body = @{ username=$Username; password="password" } | ConvertTo-Json
    $session = Invoke-RestMethod -Method POST -Uri "$BaseUrl/api/auth/login" -ContentType "application/json" -Body $body
    $tokens[$Username] = $session.accessToken
    return $session
}

function Call-Api([string]$Username,[string]$Method,[string]$Path,[string]$Body=$null) {
    if (-not $tokens.ContainsKey($Username)) { Login $Username | Out-Null }
    $p=@{Uri="$BaseUrl$Path";Method=$Method;Headers=@{Authorization="Bearer $($tokens[$Username])"}}
    if($Body){$p.ContentType="application/json";$p.Body=$Body}
    Invoke-RestMethod @p
}

function Assert-Equal($Expected,$Actual,[string]$Message){if($Expected-ne$Actual){throw "$Message - attendu: $Expected, obtenu: $Actual"}}

Write-Host "1/5 - Login JWT et identité des trois clients..."
Login "client-a"|Out-Null;Login "client-b"|Out-Null;Login "client-c"|Out-Null
Assert-Equal "tenant-a" (Call-Api "client-a" GET "/api/me").tenantId "Tenant A incorrect"
Assert-Equal "tenant-b" (Call-Api "client-b" GET "/api/me").tenantId "Tenant B incorrect"
Assert-Equal "tenant-b" (Call-Api "client-c" GET "/api/me").tenantId "Tenant C incorrect"

Write-Host "2/5 - Deux produits initiaux par tenant..."
Assert-Equal 2 (Call-Api "client-a" GET "/api/products").Count "Produits A"
Assert-Equal 2 (Call-Api "client-b" GET "/api/products").Count "Produits B"
Assert-Equal 2 (Call-Api "client-c" GET "/api/products").Count "Produits C"

Write-Host "3/5 - Produit étranger caché..."
try{Call-Api "client-a" GET "/api/products/20000000-0000-0000-0000-000000000003";throw "Fuite inter-tenant"}catch{Assert-Equal 404 ([int]$_.Exception.Response.StatusCode) "Statut attendu"}

Write-Host "4/5 - Client B crée un produit, invisible pour le tenant A..."
$category=(Call-Api "client-b" GET "/api/categories")[0]
$body=@{name="Produit temporaire";price=10.00;stock=1;imageUrl="https://placehold.co/600x400?text=Test";categoryId=$category.id}|ConvertTo-Json
$created=Call-Api "client-b" POST "/api/products" $body
try{
    Assert-Equal 3 (Call-Api "client-b" GET "/api/products").Count "Création B"
    Assert-Equal 2 (Call-Api "client-a" GET "/api/products").Count "Isolation A"
    try{Call-Api "client-a" GET "/api/products/$($created.id)";throw "Fuite inter-tenant après création"}catch{Assert-Equal 404 ([int]$_.Exception.Response.StatusCode) "Produit B visible par A"}
}finally{Call-Api "client-b" DELETE "/api/products/$($created.id)"|Out-Null}

Write-Host "5/5 - Refresh token avec rotation..."
$session=Login "client-a"
$renewed=Invoke-RestMethod -Method POST -Uri "$BaseUrl/api/auth/refresh" -ContentType "application/json" -Body (@{refreshToken=$session.refreshToken}|ConvertTo-Json)
if(-not $renewed.accessToken -or $renewed.refreshToken -eq $session.refreshToken){throw "Rotation refresh token invalide"}
Write-Host "SUCCÈS - JWT, refresh token, catalogue et isolation vérifiés." -ForegroundColor Green
