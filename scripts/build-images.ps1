$Root = Split-Path -Parent $PSScriptRoot
$Modules = @(
  "order-service", "intake-service", "admin-bff-service", "allocation-service",
  "delivery-service", "notification-service", "image-analysis-service", "api-gateway"
)
foreach ($m in $Modules) {
  docker build -f "$Root\docker\Dockerfile.service" --build-arg MODULE=$m -t "oms/${m}:0.1.0" $Root
}
docker build -f "$Root\docker\Dockerfile.web" -t oms/web-app:0.1.0 $Root
Write-Host "Built all OMS images"
