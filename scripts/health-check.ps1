# Quick health check for local OMS ports.
# Usage: .\scripts\health-check.ps1

$ports = @{
    8081 = "order-service"
    8082 = "intake-service"
    8083 = "admin-bff-service"
    8084 = "allocation-service"
    8085 = "delivery-service"
    8086 = "notification-service"
    8087 = "image-analysis-service"
    8090 = "api-gateway"
}

foreach ($port in ($ports.Keys | Sort-Object)) {
    $name = $ports[$port]
    try {
        $h = Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -TimeoutSec 2
        Write-Host ("{0,-5} {1,-24} {2}" -f $port, $name, $h.status)
    } catch {
        Write-Host ("{0,-5} {1,-24} down" -f $port, $name)
    }
}
