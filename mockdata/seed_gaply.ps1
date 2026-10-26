<#
.SYNOPSIS
  Gaply · Crea las cuentas y actividades del set de prueba DIRECTAMENTE en el
  proyecto Firebase (Authentication + Firestore), listas para entrar a la app.

.DESCUE
  1. Por cada usuario del JSON: accounts:signUp (o signIn si ya existe) -> uid real.
  2. PATCH users/{uid} con perfil + schedule (misma forma que escribe la app).
  3. PATCH activities/{id} con ownerId/attendeeIds reasignados al uid real.

  Todo con la Web API Key del proyecto y el ID token de cada usuario, así las
  reglas se cumplen solas (cada quien escribe SOLO lo suyo).

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File mockdata\seed_gaply.ps1
#>
param(
    # La API key NO se pega aquí: se lee del google-services.json del proyecto
    # (es la misma que ya viaja en la app y está trackeada en el repo).
    [string]$ApiKey = "",
    [string]$ProjectId = "gaply-ccb85",
    [string]$Password = "Gaply2026!",
    [string]$JsonPath = "$PSScriptRoot\gaply_mock_data.json"
)

$ErrorActionPreference = 'Stop'
$script:ArrayFieldNames = @('interests', 'attendees', 'attendeeIds', 'participants', 'values')

if (-not $ApiKey) {
    $googleServices = Join-Path $PSScriptRoot "..\app\google-services.json"
    if (-not (Test-Path $googleServices)) { throw "No se encuentra app/google-services.json (para leer la API key)" }
    $ApiKey = (Get-Content $googleServices -Raw -Encoding UTF8 | ConvertFrom-Json).client[0].api_key[0].current_key
}

# ---------------------------------------------------------------- conversión
function ConvertTo-Field($value, [switch]$forceArray) {
    if ($null -eq $value) { return $null }

    if ($forceArray) {
        $lista = New-Object System.Collections.ArrayList
        foreach ($item in @($value)) {
            $f = ConvertTo-Field $item
            if ($null -ne $f) { [void]$lista.Add($f) }
        }
        return @{ arrayValue = @{ values = $lista } }
    }

    if ($value -is [bool]) { return @{ booleanValue = [bool]$value } }
    if ($value -is [int] -or $value -is [long]) { return @{ integerValue = "$value" } }
    if ($value -is [double] -or $value -is [decimal]) { return @{ doubleValue = [double]$value } }
    if ($value -is [string]) { return @{ stringValue = $value } }

    if ($value -is [System.Collections.IEnumerable]) {
        $lista = New-Object System.Collections.ArrayList
        foreach ($item in $value) {
            $f = ConvertTo-Field $item
            if ($null -ne $f) { [void]$lista.Add($f) }
        }
        return @{ arrayValue = @{ values = $lista } }
    }

    # objeto (PSCustomObject del JSON)
    $campos = @{}
    foreach ($prop in $value.PSObject.Properties) {
        if ($null -eq $prop.Value) { continue }   # null -> campo ausente (la app lo lee como null)
        if ($prop.Name -in $script:ArrayFieldNames) {
            $campos[$prop.Name] = ConvertTo-Field $prop.Value -forceArray
        } else {
            $campos[$prop.Name] = ConvertTo-Field $prop.Value
        }
    }
    return @{ mapValue = @{ fields = $campos } }
}

function Invoke-GaplyApi {
    param([string]$Uri, [string]$Method = 'POST', [string]$Json, [hashtable]$Headers)
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($Json)
    $peticion = @{
        Uri         = $Uri
        Method      = $Method
        Body        = $bytes
        ContentType = 'application/json; charset=utf-8'
    }
    if ($Headers) { $peticion.Headers = $Headers }
    try {
        return Invoke-RestMethod @peticion
    } catch {
        # En PS 5.1 ErrorDetails a veces viene vacío: se lee el cuerpo del 4xx
        # para poder detectar EMAIL_EXISTS y demás códigos.
        $detalle = $_.ErrorDetails.Message
        if (-not $detalle) {
            try {
                $respuesta = $_.Exception.Response
                if ($respuesta) {
                    $lector = New-Object System.IO.StreamReader($respuesta.GetResponseStream(), [System.Text.Encoding]::UTF8)
                    $detalle = $lector.ReadToEnd()
                    $lector.Dispose()
                }
            } catch { }
        }
        if (-not $detalle) { $detalle = $_.Exception.Message }
        # Contexto en el error: sin él no se sabe cuál de las llamadas falló.
        throw "[$Method] $Uri -> $detalle"
    }
}

function Get-AuthTokens {
    param([string]$Email)
    $cuerpo = @{ email = $Email; password = $Password; returnSecureToken = $true } | ConvertTo-Json -Compress
    try {
        return Invoke-GaplyApi -Uri "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$ApiKey" -Json $cuerpo
    } catch {
        if ("$_" -notlike '*EMAIL_EXISTS*') { throw }
        return Invoke-GaplyApi -Uri "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$ApiKey" -Json $cuerpo
    }
}

# ------------------------------------------------------------------ datos
if (-not (Test-Path $JsonPath)) { throw "No se encuentra $JsonPath" }
$datos = Get-Content $JsonPath -Raw -Encoding UTF8 | ConvertFrom-Json

Write-Host "== Gaply · seed de prueba (proyecto $ProjectId) ==" -ForegroundColor Cyan
Write-Host "Contrasena compartida para todas las cuentas: $Password`n"

$uidPorId = @{}   # usr_01 -> uid real en Firebase Auth

# --------------------------------------------------- 1 · usuarios + perfiles
foreach ($u in $datos.users) {
    $auth = Get-AuthTokens -Email $u.email
    $uid = $auth.localId
    $uidPorId[$u.userId] = $uid

    $perfil = @{
        userId              = $uid
        username            = $u.username
        email               = $u.email
        firstName           = $u.firstName
        lastName            = $u.lastName
        birthDate           = $u.birthDate
        university          = $u.university
        gender              = $u.gender
        goal                = $u.goal
        interests           = $u.interests
        bio                 = $u.bio
        profilePictureUrl   = $u.profilePictureUrl
        receiveMarketing    = [bool]$u.receiveMarketing
        schedule            = $u.schedule
    }

    # IMPORTANTE: recorrer .Keys (no .PSObject.Properties: en un hashtable de
    # PowerShell eso expone Keys/Values y termina en "nested arrays").
    $campos = @{}
    foreach ($k in $perfil.Keys) {
        if ($null -eq $perfil[$k]) { continue }
        if ($k -in $script:ArrayFieldNames) {
            $campos[$k] = ConvertTo-Field $perfil[$k] -forceArray
        } else {
            $campos[$k] = ConvertTo-Field $perfil[$k]
        }
    }
    $cuerpo = @{ fields = $campos } | ConvertTo-Json -Depth 30
    Invoke-GaplyApi `
        -Method PATCH `
        -Uri "https://firestore.googleapis.com/v1/projects/$ProjectId/databases/(default)/documents/users/$uid`?key=$ApiKey" `
        -Json $cuerpo `
        -Headers @{ Authorization = "Bearer $($auth.idToken)" } | Out-Null

    Write-Host ("  [users] {0,-28} uid={1}" -f $u.email, $uid) -ForegroundColor Green
}

# --------------------------------------------------- 2 · actividades
foreach ($a in $datos.activities) {
    $uidDueno = $uidPorId[$a.ownerId]
    if (-not $uidDueno) { throw "Sin uid para el creador $($a.ownerId)" }

    # El token debe ser DEL DUEÑO: las reglas exigen ownerId == auth.uid al crear.
    $authDueno = Get-AuthTokens -Email ($datos.users | Where-Object { $_.userId -eq $a.ownerId }).email

    $campos = @{}
    foreach ($prop in $a.PSObject.Properties) {
        if ($prop.Name -eq 'id') { continue }             # el id va en la URL, no como campo
        if ($null -eq $prop.Value) { continue }
        if ($prop.Name -in @('ownerId', 'attendeeIds')) {
            # reasignar al uid real de Auth
            $nuevo = if ($prop.Name -eq 'ownerId') { $uidDueno } else { @($uidDueno) }
            $campos[$prop.Name] = ConvertTo-Field $nuevo -forceArray:($prop.Name -eq 'attendeeIds')
        } elseif ($prop.Name -in $script:ArrayFieldNames) {
            $campos[$prop.Name] = ConvertTo-Field $prop.Value -forceArray
        } else {
            $campos[$prop.Name] = ConvertTo-Field $prop.Value
        }
    }

    $cuerpo = @{ fields = $campos } | ConvertTo-Json -Depth 30
    Invoke-GaplyApi `
        -Method PATCH `
        -Uri "https://firestore.googleapis.com/v1/projects/$ProjectId/databases/(default)/documents/activities/$($a.id)?key=$ApiKey" `
        -Json $cuerpo `
        -Headers @{ Authorization = "Bearer $($authDueno.idToken)" } | Out-Null

    Write-Host ("  [activities] {0,-9} {1}" -f $a.id, $a.title) -ForegroundColor DarkCyan
}

# --------------------------------------------------- 3 · resumen
Write-Host "`n== Cuentas creadas (entra con email + contrasena) ==" -ForegroundColor Cyan
$datos.users | ForEach-Object {
    [PSCustomObject]@{
        id_origen = $_.userId
        uid_real  = $uidPorId[$_.userId]
        email     = $_.email
        password  = $Password
    }
} | Format-Table -AutoSize

Write-Host "Listo: $($datos.users.Count) perfiles y $($datos.activities.Count) actividades escritos." -ForegroundColor Cyan
