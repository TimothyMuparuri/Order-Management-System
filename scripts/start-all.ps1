# Start all OMS backend services (and optional infra) in the background.
# Usage (from repo root or anywhere):
#   .\scripts\start-all.ps1
#   .\scripts\start-all.ps1 -SkipDocker
#   .\scripts\start-all.ps1 -WithFrontend

param(
    [switch]$SkipDocker,
    [switch]$WithFrontend,
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$env:Path = "$env:USERPROFILE\tools\apache-maven-3.9.9\bin;$env:Path"
$env:OMS_DB_URL = "jdbc:postgresql://localhost:5432/order-ms"
$env:OMS_DB_USER = "postgres"
$env:OMS_DB_PASSWORD = "Sizor.kom21"
$env:OMS_DB_DRIVER = "org.postgresql.Driver"

$LogDir = Join-Path $Root ".run-logs"
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

if (-not $SkipDocker) {
    Write-Host "Starting Docker infra (postgres/kafka/minio if defined)..."
    try {
        docker compose up -d 2>&1 | Out-Host
    } catch {
        Write-Host "Docker compose skipped/failed (OK if you use local PostgreSQL 17 only)."
    }
}

if (-not $SkipBuild) {
    Write-Host "Building modules (skip with -SkipBuild)..."
    mvn -q install -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }
}

$services = @(
    @{ Name = "order-service";          Port = $null },
    @{ Name = "intake-service";         Port = $null },
    @{ Name = "admin-bff-service";      Port = $null },
    @{ Name = "allocation-service";     Port = $null },
    @{ Name = "delivery-service";       Port = $null },
    @{ Name = "notification-service";   Port = $null },
    @{ Name = "image-analysis-service"; Port = $null },
    # Frontend talks to gateway on 8090 (see web-app environment.ts)
    @{ Name = "api-gateway";            Port = 8090 }
)

foreach ($svc in $services) {
    $log = Join-Path $LogDir "$($svc.Name).log"
    # Clear inherited SERVER_PORT so services keep their application.yml ports
    # (except api-gateway, which must listen on 8090 for the web-app).
    $portLine = if ($svc.Port) {
        "`$env:SERVER_PORT = '$($svc.Port)'"
    } else {
        "Remove-Item Env:SERVER_PORT -ErrorAction SilentlyContinue"
    }
    $cmd = @"
`$env:Path = `"$env:USERPROFILE\tools\apache-maven-3.9.9\bin;`$env:Path`"
`$env:OMS_DB_URL = 'jdbc:postgresql://localhost:5432/order-ms'
`$env:OMS_DB_USER = 'postgres'
`$env:OMS_DB_PASSWORD = 'Sizor.kom21'
`$env:OMS_DB_DRIVER = 'org.postgresql.Driver'
$portLine
Set-Location '$Root'
mvn -pl $($svc.Name) spring-boot:run > `"$log`" 2>&1
"@
    Start-Process -FilePath "powershell.exe" -ArgumentList "-NoProfile", "-Command", $cmd -WindowStyle Hidden
    Write-Host "Started $($svc.Name)  ->  $log"
}

if ($WithFrontend) {
    $webLog = Join-Path $LogDir "web-app.log"
    $webCmd = @"
Set-Location '$Root\web-app'
npm start > `"$webLog`" 2>&1
"@
    Start-Process -FilePath "powershell.exe" -ArgumentList "-NoProfile", "-Command", $webCmd -WindowStyle Hidden
    Write-Host "Started web-app  ->  $webLog"
}

Write-Host ""
Write-Host "All backend launches kicked off."
Write-Host "Wait ~30-60s, then check:  .\scripts\health-check.ps1"
Write-Host "Stop everything:         .\scripts\stop-all.ps1"
Write-Host "Site: http://localhost:4200   API: http://localhost:8090"
