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

Write-Host "2/5 - Deux ouvrages initiaux par tenant..."
Assert-Equal 2 (Call-Api "client-a" GET "/api/ouvrages").Count "Ouvrages A"
Assert-Equal 2 (Call-Api "client-b" GET "/api/ouvrages").Count "Ouvrages B"
Assert-Equal 2 (Call-Api "client-c" GET "/api/ouvrages").Count "Ouvrages C"

Write-Host "3/5 - Ouvrage étranger caché..."
try{Call-Api "client-a" GET "/api/ouvrages/20000000-0000-0000-0000-000000000003";throw "Fuite inter-tenant"}catch{Assert-Equal 404 ([int]$_.Exception.Response.StatusCode) "Statut attendu"}

Write-Host "4/5 - Client B crée un ouvrage, invisible pour le tenant A..."
$filiere=(Call-Api "client-b" GET "/api/filieres")[0]
$niveau=(Call-Api "client-b" GET "/api/niveaux")[0]
$body=@{titre="Ouvrage temporaire";auteur="Auteur test";resume="Résumé de test";filiereId=$filiere.id;niveauId=$niveau.id}|ConvertTo-Json
$created=Call-Api "client-b" POST "/api/ouvrages" $body
try{
    Assert-Equal 3 (Call-Api "client-b" GET "/api/ouvrages").Count "Création B"
    Assert-Equal 2 (Call-Api "client-a" GET "/api/ouvrages").Count "Isolation A"
    try{Call-Api "client-a" GET "/api/ouvrages/$($created.id)";throw "Fuite inter-tenant après création"}catch{Assert-Equal 404 ([int]$_.Exception.Response.StatusCode) "Ouvrage B visible par A"}
}finally{Call-Api "client-b" DELETE "/api/ouvrages/$($created.id)"|Out-Null}

Write-Host "5/5 - Refresh token avec rotation..."
$session=Login "client-a"
$renewed=Invoke-RestMethod -Method POST -Uri "$BaseUrl/api/auth/refresh" -ContentType "application/json" -Body (@{refreshToken=$session.refreshToken}|ConvertTo-Json)
if(-not $renewed.accessToken -or $renewed.refreshToken -eq $session.refreshToken){throw "Rotation refresh token invalide"}
Write-Host "SUCCÈS - JWT, refresh token, ouvrages et isolation vérifiés." -ForegroundColor Green
