# Prueba de humo E2E para Gaply (emulador + uiautomator).
# Uso: powershell -ExecutionPolicy Bypass -File scripts\smoke-test.ps1
$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$apk = Join-Path $PSScriptRoot "..\app\build\outputs\apk\debug\app-debug.apk"
$pkg = "com.gaply.app"

# Credenciales unicas por corrida: Firebase Auth persiste en la nube y
# no se limpia con pm clear.
$suffix = Get-Date -Format "yyMMddHHmmss"
$testUser = "ana$suffix"
$testEmail = "$testUser@gaply.co"
$testPass = "Secreta12"
$missingUser = "noexiste$suffix"

function Wait-DeviceBoot {
    & $adb wait-for-device | Out-Null
    $deadline = (Get-Date).AddMinutes(6)
    while ((Get-Date) -lt $deadline) {
        $boot = (& $adb shell getprop sys.boot_completed | Out-String).Trim()
        if ($boot -eq "1") { Write-Host "Boot completado"; return }
        Start-Sleep -Seconds 5
    }
    throw "El emulador no arranco en 6 minutos"
}

function Get-UiXml {
    for ($i = 0; $i -lt 8; $i++) {
        & $adb shell rm -f /sdcard/ui.xml 2>$null | Out-Null
        $out = (& $adb shell "timeout 20 uiautomator dump /sdcard/ui.xml" 2>&1 | Out-String)
        if ($out -match "dumped to") {
            $raw = (& $adb shell cat /sdcard/ui.xml 2>$null | Out-String).Trim()
            if ($raw -match "<hierarchy") {
                try { return [xml]$raw } catch { }
            }
        }
        Start-Sleep -Milliseconds 800
    }
    throw "No se pudo obtener la jerarquia UI"
}

function Find-Node($xml, [string]$text) {
    $nodes = $xml.SelectNodes("//*[@text!='']")
    foreach ($n in $nodes) {
        if ($n.GetAttribute("text") -eq $text) { return $n }
    }
    foreach ($n in $nodes) {
        if ($n.GetAttribute("text").Contains($text)) { return $n }
    }
    return $null
}

function Get-Center($node) {
    $b = $node.GetAttribute("bounds")
    if ($b -notmatch "\[(\d+),(\d+)\]\[(\d+),(\d+)\]") { throw "bounds invalidos: $b" }
    $x1 = [int]$Matches[1]; $y1 = [int]$Matches[2]
    $x2 = [int]$Matches[3]; $y2 = [int]$Matches[4]
    return @([int](($x1 + $x2) / 2), [int](($y1 + $y2) / 2))
}

function Assert-Text([string]$text, [int]$retries = 5) {
    for ($i = 0; $i -lt $retries; $i++) {
        try { $xml = Get-UiXml } catch { Start-Sleep -Milliseconds 700; continue }
        if (Find-Node $xml $text) { Write-Host "  OK  '$text'"; return }
        Start-Sleep -Milliseconds 700
    }
    try {
        $xml = Get-UiXml
        $all = ($xml.SelectNodes("//*[@text!='']") | ForEach-Object { $_.GetAttribute("text") }) -join " | "
    } catch { $all = "(dump no disponible)" }
    throw "No se encontro el texto: '$text'. Pantalla actual: $all"
}

function Tap-Text([string]$text, [int]$retries = 5) {
    Hide-Keyboard
    for ($i = 0; $i -lt $retries; $i++) {
        try { $xml = Get-UiXml } catch { Start-Sleep -Milliseconds 700; continue }
        $node = Find-Node $xml $text
        if ($node) {
            $c = Get-Center $node
            & $adb shell input tap $c[0] $c[1] 2>$null | Out-Null
            Start-Sleep -Milliseconds 800
            Write-Host "  TAP '$text'"
            return
        }
        Start-Sleep -Milliseconds 500
    }
    throw "No se pudo tocar: '$text'"
}

function Get-FocusedNode($xml) {
    foreach ($n in $xml.SelectNodes("//*[@focused='true']")) { return $n }
    return $null
}

function Type-Text([string]$value) {
    Use-Gboard
    $plain = $value.Replace("%s", " ")
    $adbValue = $value.Replace(" ", "%s")
    for ($attempt = 0; $attempt -lt 4; $attempt++) {
        Start-Sleep -Milliseconds 900
        $fc = $null
        try {
            $xml = Get-UiXml
            $f = Get-FocusedNode $xml
            if ($f) { $fc = Get-Center $f }
        } catch { }
        for ($impulse = 0; $impulse -lt 3; $impulse++) {
            try {
                $xml = Get-UiXml
                $f = Get-FocusedNode $xml
                if ($f) {
                    $len = $f.GetAttribute("text").Length
                    if ($len -gt 0) {
                        for ($k = 0; $k -lt $len + 2; $k++) {
                            & $adb shell input keyevent 67 2>$null | Out-Null
                        }
                        Start-Sleep -Milliseconds 400
                    }
                }
            } catch { }
            & $adb shell "timeout 10 input text $adbValue" 2>$null | Out-Null
            Start-Sleep -Milliseconds 900
            try { $xml = Get-UiXml } catch { continue }
            $focused = Get-FocusedNode $xml
            if (-not $focused) { continue }
            $current = $focused.GetAttribute("text")
            $isPassword = $focused.GetAttribute("password") -eq "true"
            if (($current -eq $plain) -or ($isPassword -and $current.Length -eq $plain.Length)) { return }
        }
        Write-Host "    type[$attempt]: texto actual='$current' (esperado '$plain')"
        if ($fc) { & $adb shell input tap $fc[0] $fc[1] 2>$null | Out-Null }
    }
    throw "No se pudo escribir: '$value'"
}

function Hide-Keyboard {
    # ADBKeyboard no tiene vista de teclado: cambiar a ella deja la pantalla
    # libre para que los toques lleguen a los botones de la app.
    & $adb shell ime set com.android.adbkeyboard/.AdbIME 2>$null | Out-Null
    Start-Sleep -Milliseconds 700
}

function Use-Gboard {
    & $adb shell ime set com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME 2>$null | Out-Null
    Start-Sleep -Milliseconds 700
}

Write-Host "== Esperando emulador =="
Wait-DeviceBoot

Write-Host "== Instalando APK =="
& $adb install -r $apk | Out-Null

Write-Host "== Lanzando Gaply (fresh) =="
& $adb shell am force-stop $pkg | Out-Null
& $adb shell pm clear $pkg | Out-Null
Start-Sleep -Seconds 1
& $adb shell am start -n "$pkg/.MainActivity" | Out-Null
Start-Sleep -Seconds 4

Write-Host "== 1. Pantalla inicial =="
Assert-Text "Encuentra algo que hacer entre tus clases"
Assert-Text "Inicia Sesi"
Assert-Text "Crear Cuenta"

Write-Host "== 2. Login =="
Tap-Text "Inicia"
Assert-Text "Usuario"
Assert-Text "Ingresar"

Write-Host "== 3. Validacion de campos vacios =="
Tap-Text "Ingresar"
Assert-Text "Este campo es obligatorio"

Write-Host "== 4. Usuario inexistente (username) =="
Tap-Text "Usuario"
Type-Text $missingUser
Hide-Keyboard
Tap-Text "Contrase"
Type-Text "123456"
Hide-Keyboard
Tap-Text "Ingresar"
Assert-Text "Cuenta no encontrada"
Tap-Text "Aceptar"

Write-Host "== 5. Ir a Registro =="
Tap-Text "Reg"
Assert-Text "Paso 1 de 5"

Write-Host "== 6. Registro paso 1 =="
Tap-Text "Usuario (@username)"
Type-Text $testUser
Hide-Keyboard
Assert-Text $testUser
Tap-Text "Correo"
Type-Text $testEmail
Hide-Keyboard
Assert-Text $testEmail
Tap-Text "Contrase"
Type-Text $testPass
Hide-Keyboard
Tap-Text "Confirmar Contrase"
Type-Text $testPass
Hide-Keyboard
Tap-Text "Siguiente"
Assert-Text "Paso 2 de 5"

Write-Host "== 7. Registro paso 2 =="
Tap-Text "Nombres *"
Type-Text "Ana"
Hide-Keyboard
Tap-Text "Apellidos"
Type-Text "Perez"
Hide-Keyboard
Tap-Text "Día"
Start-Sleep -Milliseconds 600
Tap-Text "1"
Tap-Text "Mes"
Start-Sleep -Milliseconds 600
Tap-Text "Enero"
Tap-Text "Año"
Start-Sleep -Milliseconds 600
Tap-Text "2010"
Tap-Text "Universidad *"
Start-Sleep -Milliseconds 600
Tap-Text "Universidad de los Andes"
Tap-Text "Elige tu g"
Start-Sleep -Milliseconds 600
Tap-Text "Femenino"
Tap-Text "Siguiente"
Assert-Text "Paso 3 de 5"

Write-Host "== 8. Registro paso 3 (proposito) =="
Tap-Text "Necesito estudiar"
Tap-Text "Siguiente"
Assert-Text "Paso 4 de 5"

Write-Host "== 9. Registro paso 4 (intereses) =="
Tap-Text "Deportes"
Start-Sleep -Milliseconds 400
Tap-Text "Música"
Start-Sleep -Milliseconds 400
Tap-Text "Estudio"
Start-Sleep -Milliseconds 400
Assert-Text "3/3"
Tap-Text "Siguiente"
Assert-Text "Paso 5 de 5"

Write-Host "== 10. Registro paso 5 (perfil) =="
Tap-Text "Guardar perfil"
Assert-Text "Tu perfil se ha guardado"
Tap-Text "Aceptar"
Tap-Text "Siguiente"
Assert-Text "Bienvenido"
Assert-Text "Descubrir"
Assert-Text "Tienes 2 horas libres"
Assert-Text "Sugerencias para hoy"
Assert-Text "Agenda cultural"

Write-Host "== 11. Tabs inferiores =="
Tap-Text "Perfil"
Assert-Text "Cerrar sesi"
Assert-Text "@$testUser"
Assert-Text $testEmail

Write-Host "== 12. Cerrar sesion =="
Tap-Text "Cerrar sesi"
Assert-Text "Inicia Sesi"

Write-Host "== 13. Login con password incorrecta =="
Tap-Text "Inicia"
Assert-Text "Usuario"
Tap-Text "Usuario"
Type-Text $testUser
Hide-Keyboard
Tap-Text "Contrase"
Type-Text "PasswordErronea1"
Hide-Keyboard
Tap-Text "Ingresar"
Assert-Text "Usuario o contrase"

Write-Host "== 14. Login correcto con la cuenta creada =="
Tap-Text "Contrase"
Type-Text $testPass
Hide-Keyboard
Tap-Text "Ingresar"
Assert-Text "Bienvenido"
Assert-Text "Ana"

Write-Host ""
Write-Host "SMOKE TEST: TODAS LAS VERIFICACIONES PASARON"

Write-Host "== Restaurando teclado Gboard =="
& $adb shell ime set com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME | Out-Null
