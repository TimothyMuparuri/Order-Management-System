# Kubernetes manifests

Apply in order:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/services.yaml
kubectl apply -f k8s/deployments.yaml
kubectl apply -f k8s/hpa.yaml
```

Build images first with `scripts/build-images.ps1` (or `.sh`) and push to your registry, then retarget image names in `deployments.yaml`.

Probes use Spring Boot Actuator liveness/readiness. HPA scales on CPU; Kafka consumer lag can be added via a Prometheus adapter custom metric.
