#!/bin/bash
set -e

REGISTRY="ghcr.io"
OWNER="${GITHUB_USERNAME:-thacbaonguyen}"
TAG="${1:-latest}"

SERVICES=(
  "service-registry"
  "api-gateway"
  "user-service"
  "product-service"
  "order-service"
  "payment-service"
  "recommendation-service"
  "notification-service"
)

echo "========================================="
echo "  Neki Microservice — Build & Push"
echo "  Registry: ${REGISTRY}/${OWNER}"
echo "  Tag: ${TAG}"
echo "========================================="
echo ""

if ! docker info > /dev/null 2>&1; then
  echo "❌ Docker is not running. Please start Docker first."
  exit 1
fi

echo "📦 Step 1: Logging in to ${REGISTRY}..."
if [ -z "${GITHUB_TOKEN}" ]; then
  echo "⚠️  GITHUB_TOKEN not set. Please run:"
  echo "   export GITHUB_TOKEN=<your-personal-access-token>"
  echo "   Token needs 'write:packages' permission"
  echo ""
  echo "   Or login manually:"
  echo "   echo \$GITHUB_TOKEN | docker login ${REGISTRY} -u ${OWNER} --password-stdin"
  exit 1
fi
echo "${GITHUB_TOKEN}" | docker login ${REGISTRY} -u ${OWNER} --password-stdin
echo "✅ Logged in to ${REGISTRY}"
echo ""

echo "🔨 Step 2: Building & pushing images..."
echo ""

for SERVICE in "${SERVICES[@]}"; do
  IMAGE="${REGISTRY}/${OWNER}/neki-${SERVICE}:${TAG}"
  echo "━━━ Building ${SERVICE} ━━━"
  docker build \
    -t "${IMAGE}" \
    -f "${SERVICE}/Dockerfile" \
    . \
    --progress=plain 2>&1 | tail -5
  
  echo "📤 Pushing ${IMAGE}..."
  docker push "${IMAGE}"
  echo "✅ ${SERVICE} → ${IMAGE}"
  echo ""
done

echo "========================================="
echo "  ✅ All images built and pushed!"
echo "========================================="
echo ""
echo "Images:"
for SERVICE in "${SERVICES[@]}"; do
  echo "  ${REGISTRY}/${OWNER}/neki-${SERVICE}:${TAG}"
done
echo ""
echo "To create K8s pull secret:"
echo "  kubectl create secret docker-registry ghcr-secret \\"
echo "    --namespace=neki \\"
echo "    --docker-server=${REGISTRY} \\"
echo "    --docker-username=${OWNER} \\"
echo "    --docker-password=\${GITHUB_TOKEN}"
