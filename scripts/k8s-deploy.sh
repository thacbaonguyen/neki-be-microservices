#!/bin/bash
set -e

PHASE="${1:-help}"

echo "========================================="
echo "  Neki Microservice — K8s Deploy"
echo "  Phase: ${PHASE}"
echo "========================================="
echo ""

case "${PHASE}" in

  1|foundation)
    echo "📦 Phase 1: Foundation (Namespace + Secrets)"
    kubectl apply -f k8s/namespace.yml
    kubectl apply -f k8s/secrets/

    if [ -n "${GITHUB_TOKEN}" ] && [ -n "${GITHUB_USERNAME}" ]; then
      echo "🔑 Creating ghcr.io pull secret..."
      kubectl create secret docker-registry ghcr-secret \
        --namespace=neki \
        --docker-server=ghcr.io \
        --docker-username="${GITHUB_USERNAME}" \
        --docker-password="${GITHUB_TOKEN}" \
        --dry-run=client -o yaml | kubectl apply -f -
    else
      echo "⚠️  Skipping ghcr pull secret (set GITHUB_TOKEN and GITHUB_USERNAME)"
    fi

    echo ""
    echo "✅ Phase 1 complete!"
    kubectl get namespaces | grep -E "neki|monitoring"
    kubectl get secrets -n neki
    ;;

  2|infrastructure)
    echo "🗄️ Phase 2: Infrastructure"
    kubectl apply -f k8s/infrastructure/mysql/
    kubectl apply -f k8s/infrastructure/postgres/
    kubectl apply -f k8s/infrastructure/redis/
    kubectl apply -f k8s/infrastructure/rabbitmq/
    kubectl apply -f k8s/infrastructure/elasticsearch/

    echo ""
    echo "✅ Phase 2 applied! Waiting for pods..."
    kubectl get pods -n neki -w
    ;;

  3|apps)
    echo "🚀 Phase 3: Application Services"
    kubectl apply -f k8s/apps/service-registry/
    echo "⏳ Waiting for service-registry to be ready..."
    kubectl rollout status deployment/service-registry -n neki --timeout=120s

    kubectl apply -f k8s/apps/api-gateway/
    kubectl apply -f k8s/apps/user-service/
    kubectl apply -f k8s/apps/product-service/
    kubectl apply -f k8s/apps/order-service/
    kubectl apply -f k8s/apps/payment-service/
    kubectl apply -f k8s/apps/recommendation-service/
    kubectl apply -f k8s/apps/notification-service/

    echo ""
    echo "✅ Phase 3 applied!"
    kubectl get pods -n neki
    ;;

  4|monitoring)
    echo "📊 Phase 4: Observability Stack"
    kubectl apply -f k8s/monitoring/prometheus/
    kubectl apply -f k8s/monitoring/loki/
    kubectl apply -f k8s/monitoring/tempo/
    kubectl apply -f k8s/monitoring/promtail/
    kubectl apply -f k8s/monitoring/grafana/

    echo ""
    echo "✅ Phase 4 applied!"
    kubectl get pods -n monitoring
    ;;

  5|ingress)
    echo "🌐 Phase 5: Ingress"
    kubectl apply -f k8s/ingress.yml

    echo ""
    echo "✅ Phase 5 applied!"
    kubectl get ingress -n neki
    ;;

  all)
    echo "🚀 Deploying ALL phases..."
    $0 1 && $0 2 && $0 3 && $0 4 && $0 5
    ;;

  status)
    echo "📋 Cluster Status:"
    echo ""
    echo "=== Namespace: neki ==="
    kubectl get pods -n neki -o wide
    echo ""
    echo "=== Namespace: monitoring ==="
    kubectl get pods -n monitoring -o wide
    echo ""
    echo "=== Services ==="
    kubectl get svc -n neki
    echo ""
    echo "=== Ingress ==="
    kubectl get ingress -n neki 2>/dev/null || echo "No ingress configured"
    ;;

  delete)
    echo "🗑️ Deleting everything..."
    read -p "Are you sure? (y/N): " confirm
    if [ "$confirm" = "y" ]; then
      kubectl delete namespace neki monitoring --ignore-not-found
      echo "✅ Deleted"
    fi
    ;;

  *)
    echo "Usage: $0 <phase>"
    echo ""
    echo "Phases:"
    echo "  1 | foundation    — Namespaces + Secrets"
    echo "  2 | infrastructure — MySQL, PostgreSQL, Redis, RabbitMQ, ES"
    echo "  3 | apps          — 8 Spring Boot services"
    echo "  4 | monitoring    — Prometheus, Grafana, Loki, Tempo, Promtail"
    echo "  5 | ingress       — NGINX Ingress"
    echo "  all               — Deploy all phases"
    echo "  status            — Show cluster status"
    echo "  delete            — Delete everything"
    ;;

esac
