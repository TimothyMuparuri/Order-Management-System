# Stop OMS backend services listening on known ports.
# Usage: .\scripts\stop-all.ps1

$ports = 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8090
foreach ($port in $ports) {
    $procIds = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($procId in $procIds) {
        $name = (Get-Process -Id $procId -ErrorAction SilentlyContinue).ProcessName
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        Write-Host "Stopped PID $procId ($name) on port $port"
    }
    if (-not $procIds) {
        Write-Host "Port $port already free"
    }
}

Write-Host "Done. (Port 8080 left alone — often Apache/httpd, not this project.)"
