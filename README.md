# Neki E-Commerce Microservices

Backend e-commerce theo kiến trúc microservices. Hệ thống dùng Spring Boot cho các domain service chính, FastAPI + Celery cho tác vụ export CSV bất đồng bộ, API Gateway làm cổng vào, RabbitMQ cho event-driven flow, Redis cho cache/job state, và Grafana stack cho observability.

## Công Nghệ

| Nhóm | Công nghệ |
|------|-----------|
| Backend services | Java 21, Spring Boot, Spring Cloud Gateway, Eureka, OpenFeign |
| Export service | Python, FastAPI, Celery, Redis |
| Database | MySQL, PostgreSQL |
| Search/cache/queue | Elasticsearch, Redis, RabbitMQ |
| File/email integrations | Cloudinary, Cloudflare R2, SES SMTP, Spring Mail SMTP |
| Observability | Prometheus, Grafana, Loki, Promtail, Tempo, Micrometer Tracing |
| Runtime | Docker Compose, Kubernetes manifests |

## Sơ Đồ Kiến Trúc

![System architecture](system-achitecture-docs/system-architecture.png)

Ghi chú về sơ đồ:

- Sơ đồ mô tả kiến trúc tổng quan, không phải mapping 1:1 của local Docker Compose.
- Nginx/GCP thể hiện hướng triển khai production hoặc reverse proxy phía trước, còn local dev hiện đi trực tiếp qua API Gateway `:8080`.
- Sơ đồ đã thể hiện đúng các mảnh chính hiện tại: API Gateway, các domain service, Export Service, Celery worker, RabbitMQ, Eureka, Redis, Elasticsearch, MySQL, PostgreSQL, R2, SES, PayOS và observability stack.
- Observability trong repo có thêm Tempo cho distributed tracing; ảnh đang thể hiện Grafana, Prometheus, Loki và Promtail nhưng chưa vẽ Tempo.
- Nhãn `RCA Service` trong ảnh đang tương ứng với recommendation/rcm service.

## Môi Trường Chạy Hiện Tại

Docker Compose dev stack hiện có 15 container:

| Container | Port | Vai trò |
|-----------|------|---------|
| `neki-mysql` | `3308:3306` | MySQL cho `users_db`, `products_db`, `orders_db` |
| `neki-postgres` | `5432` | PostgreSQL cho `payments_db`, `recommendations_db` |
| `neki-redis` | `6379` | Cache, rate limit, export job state, Celery broker/backend |
| `neki-rabbitmq` | `5672`, `15672` | Message broker và management UI |
| `neki-elasticsearch` | `9200` | Product search |
| `neki-service-registry` | `8761` | Eureka service discovery |
| `neki-api-gateway` | `8080` | Gateway, JWT auth, routing |
| `neki-user-service` | `8081` | Auth, user, RBAC |
| `neki-product-service` | `8082` | Product, catalog, inventory, export internal API |
| `neki-order-service` | `8083` | Cart, wishlist, order, discount, outbox |
| `neki-payment-service` | `8084` | PayOS payment |
| `neki-recommendation-service` | `8085` | Personalized recommendation |
| `neki-notification-service` | `8086` | Email notification từ RabbitMQ events |
| `neki-export-service` | `8090` | FastAPI API cho product CSV export |
| `neki-export-worker` | none | Celery worker để tạo CSV và gửi mail |

Kubernetes manifests hiện mới cover các Spring services và infrastructure. Docker Compose là cách chạy đầy đủ nhất cho local hiện tại vì đã có `export-service` và `export-worker`.

## Chạy Local

### 1. Clone source

```bash
git clone git@github.com:thacbaonguyen/neki-be-microservices.git
cd neki-be-microservices
```

### 2. Chuẩn bị `.env`

`.env` đã được ignore bởi git. Không commit key thật lên repo.

Các nhóm biến cần có cho full stack:

```env
JWT_SECRETKEY=...

CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...

PAYOS_PAYOS_CLIENT_ID=...
PAYOS_PAYOS_API_KEY=...
PAYOS_PAYOS_CHECKSUM_KEY=...
URL_RETURNURL=...
URL_CANCELURL=...

SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=...
SPRING_MAIL_PASSWORD=...
APP_URL=http://localhost:5173

INTERNAL_API_TOKEN=...

R2_ACCESS_KEY=...
R2_SECRET_KEY=...
R2_ENDPOINT=...
R2_BUCKET=...
R2_REGION=auto

SES_SMTP_HOST=...
SES_SMTP_PORT=587
SES_SMTP_USER=...
SES_SMTP_PASS=...
MAIL_FROM=...
```

Lưu ý:

- `notification-service` dùng `SPRING_MAIL_*` để gửi mail đăng ký, quên mật khẩu, đơn hàng và thanh toán.
- `export-service` dùng `SES_SMTP_*` và `MAIL_FROM` để gửi mail export CSV.
- `INTERNAL_API_TOKEN` phải giống nhau ở `product-service`, `export-service` và `export-worker`.

### 3. Chạy app stack

```bash
docker compose --env-file .env -f docker-compose.dev.yml up -d --build
```

Recreate service sau khi đổi env:

```bash
# Khi đổi mail env cho notification-service
docker compose --env-file .env -f docker-compose.dev.yml up -d --force-recreate notification-service

# Khi đổi R2/SES/internal token cho export
docker compose --env-file .env -f docker-compose.dev.yml up -d --build --force-recreate product-service export-service export-worker api-gateway
```

### 4. Chạy observability

```bash
docker compose -f docker-compose.monitoring.yml up -d
docker compose -f docker-compose.logging.yml up -d
```

URL hay dùng:

| Tool | URL |
|------|-----|
| RabbitMQ Management | http://localhost:15672 |
| Eureka | http://localhost:8761 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |
| Loki | http://localhost:3100 |
| Tempo | http://localhost:3200 |

### 5. Kiểm tra nhanh

```bash
curl http://localhost:8080/api/v1/products
curl http://localhost:8090/health
docker logs neki-export-service --since=5m
docker logs neki-export-worker --since=5m
```

## Kiến Trúc Tổng Quan

```text
Client/Admin Dashboard
        |
        v
API Gateway :8080
        |
        +--> user-service :8081 -------------> MySQL users_db
        +--> product-service :8082 ----------> MySQL products_db + Elasticsearch
        +--> order-service :8083 ------------> MySQL orders_db
        +--> payment-service :8084 ----------> PostgreSQL payments_db
        +--> recommendation-service :8085 ---> PostgreSQL recommendations_db
        +--> notification-service :8086 -----> SMTP
        +--> export-service :8090 -----------> Redis/Celery + R2 + SES

RabbitMQ kết nối các event user/order/payment đến notification, product, order và recommendation consumers.
Redis được dùng cho cache, rate limit và export job state.
```

## API Gateway Routes

| Path | Target |
|------|--------|
| `/api/v1/auth/**`, `/api/v1/users/**`, `/api/v1/admin/users/**`, `/oauth2/**`, `/login/oauth2/**` | `user-service` |
| `/api/v1/products/**`, `/api/v1/categories/**`, `/api/v1/attributes/**`, `/api/v1/banners/**`, `/api/v1/catalog/**`, `/api/v1/search/**`, `/api/v1/settings/**`, `/api/v1/admin/products/**` | `product-service` |
| `/api/v1/admin/exports/**` | `export-service` |
| `/api/v1/orders/**`, `/api/v1/cart/**`, `/api/v1/wishlist/**`, `/api/v1/reviews/**`, `/api/v1/shipping/**`, `/api/v1/discounts/**`, `/api/v1/admin/orders/**`, `/api/v1/admin/discounts/**`, `/api/v1/admin/statistics/**` | `order-service` |
| `/payment/**`, `/api/v1/payment-method/**`, `/api/v1/admin/payment-method/**` | `payment-service` |
| `/api/v1/recommendations/**` | `recommendation-service` |

`JwtAuth` xác thực JWT và forward user context qua các header như `X-User-Id`, `X-User-Email`, `X-User-Roles`.

## Các Service Chính

### Service Registry

- Port: `8761`
- Eureka server để các Spring services đăng ký và tìm nhau.

### API Gateway

- Port: `8080`
- Xử lý JWT auth, CORS, routing và các phần liên quan Redis/rate limit.

### User Service

- Port: `8081`
- Database: MySQL `users_db`
- Chức năng: signup, login, refresh token, profile, RBAC, password reset.
- Publish RabbitMQ events cho đăng ký và quên mật khẩu.

### Product Service

- Port: `8082`
- Database: MySQL `products_db`
- Tích hợp: Elasticsearch, Cloudinary, RabbitMQ.
- Chức năng: product/catalog CRUD, inventory, public product listing, internal export API.
- Internal export endpoints:
  - `POST /internal/products/export/count`
  - `POST /internal/products/export/page`
- Internal endpoints được bảo vệ bằng `X-Internal-Token`.

### Order Service

- Port: `8083`
- Database: MySQL `orders_db`
- Chức năng: cart, wishlist, order, shipping, discount, admin statistics.
- Dùng Feign client để gọi user-service và product-service.
- Publish order events bằng transactional outbox pattern.
- Listen `payment.completed` để confirm order.

### Payment Service

- Port: `8084`
- Database: PostgreSQL `payments_db`
- Chức năng: tạo PayOS payment link, xử lý webhook, quản lý payment method.
- Gọi order-service qua Feign.
- Publish payment events lên RabbitMQ.

### Recommendation Service

- Port: `8085`
- Database: PostgreSQL `recommendations_db`
- Chức năng: gợi ý sản phẩm cá nhân hóa và invalidation cache.
- Listen order events.

### Notification Service

- Port: `8086`
- Không có database riêng.
- Dùng Spring Mail qua `SPRING_MAIL_*`.
- Listen user, order và payment events để gửi email.

### Export Service

- Port: `8090`
- Runtime: FastAPI.
- Redis mặc định: `redis://redis:6379/1`.
- External endpoints:
  - `POST /api/v1/admin/exports/products`
  - `GET /api/v1/admin/exports/{jobId}`
- Lấy email người export từ `X-User-Email`.
- Chỉ cho phép role `ADMIN`.
- Gọi Product Service internal API để lấy product count/page.
- Dùng R2 để lưu CSV và SES SMTP để gửi mail export.

### Export Worker

- Runtime: Celery worker.
- Command: `celery -A app.celery_app worker -Q exports -l info --concurrency=1`
- Queue: `exports`
- Tasks:
  - `export_csv`
  - `send_mail`
- Retry policy: retry 3 lần, mỗi lần cách 5 giây.

## Product CSV Export

API bên ngoài:

```http
POST /api/v1/admin/exports/products
```

Request mẫu:

```json
{
  "filters": {
    "categoryId": 1,
    "brandId": 2,
    "keyword": "shirt",
    "isActive": true
  },
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

Các filter hỗ trợ lấy từ `ProductFilterRequest`: `keyword`, `categoryId`, `subCategoryId`, `brandId`, `collectionId`, `topicId`, `gender`, `minPrice`, `maxPrice`, `colorIds`, `sizeIds`, `isFeatured`, `isNew`, `isOnSale`, `isActive`, `inStock`.

Luồng xử lý:

1. Admin bấm export trong dashboard.
2. Frontend gọi `POST /api/v1/admin/exports/products`.
3. API Gateway xác thực JWT và chuyển request đến Export Service.
4. Export Service kiểm tra `X-User-Email` và `X-User-Roles`.
5. Export Service gọi Product Service internal count API với `X-Internal-Token`.
6. Nếu `rowCount <= 100`, Export Service lấy một page nhỏ và trả `text/csv` trực tiếp cho browser download.
7. Nếu `rowCount > 100`, Export Service tạo `export_job:{jobId}` trong Redis và enqueue Celery task `export_csv`.
8. `export_csv` chuyển job sang `PROCESSING`, gọi Product Service theo page size mặc định 500 và ghi từng batch vào file CSV tạm.
9. `export_csv` upload CSV lên R2 theo key `exports/products/YYYY/MM/{jobId}.csv`.
10. `export_csv` tạo presigned URL, chuyển job sang `DONE`, set `mailStatus=PENDING` và enqueue `send_mail`.
11. `send_mail` gửi presigned URL đến email của account đang login qua SES SMTP.
12. Frontend có thể xem trạng thái qua `GET /api/v1/admin/exports/{jobId}`.

CSV dùng encoding UTF-8 có BOM (`utf-8-sig`) và escape các cell bắt đầu bằng `=`, `+`, `-`, `@` để giảm rủi ro CSV formula injection.

Redis job:

```text
key: export_job:{jobId}
ttl: 8 ngày
status: QUEUED | PROCESSING | DONE | FAILED
mailStatus: PENDING | SENT | FAILED
```

## RabbitMQ Event Flow

Exchanges:

| Exchange | Type | Mục đích |
|----------|------|----------|
| `neki.user.exchange` | topic | User registration và password reset |
| `neki.order.exchange` | topic | Order lifecycle |
| `neki.payment.exchange` | topic | Payment lifecycle |

Queues chính:

```text
User Service
  -> neki.user.exchange
     -> notification.user.registered
     -> notification.user.password

Order Service
  -> neki.order.exchange
     -> product.order.created
     -> product.order.cancelled
     -> notification.order.created
     -> notification.order.cancelled
     -> notification.order.updated
     -> recommendation.order.created

Payment Service
  -> neki.payment.exchange
     -> order.payment.completed
     -> notification.payment
```

Order Service dùng transactional outbox:

1. Business transaction lưu order data và một outbox row.
2. `OutboxEventRelay` đọc các event `PENDING`.
3. Relay publish event lên RabbitMQ.
4. Relay chuyển event sang `PROCESSED` sau khi publish thành công.

## Giao Tiếp Đồng Bộ/Internal

| Caller | Callee | Cơ chế | Mục đích |
|--------|--------|--------|----------|
| Order Service | User Service | OpenFeign | Validate/lấy thông tin user |
| Order Service | Product Service | OpenFeign | Product info và inventory |
| Payment Service | Order Service | OpenFeign | Đồng bộ payment/order status |
| Recommendation Service | Product Service | OpenFeign | Product details |
| Recommendation Service | Order Service | OpenFeign | Purchase history |
| Export Service | Product Service | HTTPX internal API | Product export count/page |

Các Spring Feign clients có fallback/fallback factory ở những nơi đã cấu hình. Export Service internal calls được bảo vệ bằng `X-Internal-Token`.

## Observability

### Metrics

- Spring services expose `/actuator/prometheus`.
- Prometheus scrape Spring services và Grafana hiển thị dashboard.
- Trace `/actuator/prometheus` là infrastructure noise khi đọc Tempo, không phải business request.

### Logs

```text
Docker stdout -> Promtail -> Loki -> Grafana
```

Spring services dùng logback. Docker profile xuất structured logs có trace ID.

### Tracing

```text
Client -> API Gateway -> Spring services -> Tempo -> Grafana
```

Spring services dùng Micrometer Tracing và export trace sang Tempo qua OTLP HTTP endpoint `MANAGEMENT_OTLP_TRACING_ENDPOINT`.

Khi đọc trace:

- Filter theo service/path nghiệp vụ, ví dụ `api-gateway` + `exports`.
- Bỏ qua `/actuator/prometheus`, `/actuator/health` và Eureka noise.
- Async work như RabbitMQ consumer, scheduled outbox relay và Celery job có thể xuất hiện thành trace riêng nếu chưa propagate trace context qua async boundary.
- FastAPI Export Service và Celery worker hiện chưa được instrument OpenTelemetry trong repo.

## Lệnh Hữu Ích

```bash
# Xem container hiện tại
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Gateway logs
docker logs neki-api-gateway --since=10m

# Export logs
docker logs neki-export-service --since=10m
docker logs neki-export-worker --since=10m

# Product internal/export logs
docker logs neki-product-service --since=10m

# Notification mail logs
docker logs neki-notification-service --since=10m | grep -Ei "mail|smtp|auth|Failed|sent"

# RabbitMQ UI
open http://localhost:15672
```

Validation:

```bash
# Validate compose config
docker compose --env-file .env -f docker-compose.dev.yml config

# Chạy Java tests
./mvnw test

# Kiểm tra syntax Python export-service
python3 -m compileall -q export-service/app
```

## Kubernetes

Kubernetes files nằm trong `k8s/`, scripts nằm trong `scripts/`.

```bash
minikube start --memory=8192 --cpus=4 --driver=docker
minikube addons enable ingress

./scripts/k8s-build-push.sh
./scripts/k8s-deploy.sh 1
./scripts/k8s-deploy.sh 2
./scripts/k8s-deploy.sh 3
./scripts/k8s-deploy.sh 4
./scripts/k8s-deploy.sh 5
```

Giới hạn hiện tại: Kubernetes manifests và scripts chưa deploy `export-service` và `export-worker`. Dùng Docker Compose nếu cần chạy đầy đủ stack hiện tại ở local.

## Cấu Trúc Thư Mục

```text
neki-microservice/
├── api-gateway/                     # Spring Cloud Gateway
├── common-lib/                      # DTO, event, exception, constant dùng chung
├── export-service/                  # FastAPI + Celery cho product CSV export
├── notification-service/            # Consumer gửi email từ RabbitMQ events
├── order-service/                   # Cart, wishlist, order, discount, outbox
├── payment-service/                 # Tích hợp thanh toán PayOS
├── product-service/                 # Product/catalog/inventory/search
├── recommendation-service/          # Gợi ý sản phẩm
├── service-registry/                # Eureka server
├── user-service/                    # Auth, users, RBAC
├── docker/                          # MySQL/Postgres init scripts
├── k8s/                             # Kubernetes manifests
├── monitoring/                      # Prometheus, Grafana, Loki, Tempo configs
├── scripts/                         # K8s helper scripts
├── system-achitecture-docs/         # Ảnh kiến trúc hệ thống
├── docker-compose.dev.yml           # Stack local đầy đủ
├── docker-compose.monitoring.yml    # Prometheus + Grafana
├── docker-compose.logging.yml       # Loki + Promtail + Tempo
└── pom.xml                          # Root Maven multi-module POM
```
