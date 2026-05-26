# Neki E-Commerce Microservice

Hệ thống bán hàng trực tuyến theo kiến trúc **Microservice**, sử dụng **RESTful API** (giao tiếp đồng bộ) và **RabbitMQ** (giao tiếp bất đồng bộ).

**Tech Stack:** Java 21 · Spring Boot 3.5.9 · Spring Cloud 2025.0.0 · MySQL · PostgreSQL · Redis · RabbitMQ · Elasticsearch · Docker · Kubernetes

---

## Mục lục

- [Hướng dẫn chạy hệ thống](#hướng-dẫn-chạy-hệ-thống)
  - [Cách 1: Docker Compose](#cách-1-docker-compose)
  - [Cách 2: Kubernetes (Minikube)](#cách-2-kubernetes-minikube)
- [Mô tả bài toán](#1-mô-tả-bài-toán)
- [Kiến trúc hệ thống](#2-kiến-trúc-hệ-thống)
- [Mô tả các Microservice](#3-mô-tả-các-microservice)
- [Luồng Message Queue](#4-luồng-message-queue-rabbitmq)
- [Giao tiếp đồng bộ (Feign Client)](#5-giao-tiếp-đồng-bộ-feign-client)
- [Unit Test](#6-unit-test)
- [Logging (Centralized)](#7-logging-centralized)
- [Monitoring & Tracing](#8-monitoring--distributed-tracing)
- [Cấu trúc thư mục](#9-cấu-trúc-thư-mục)

---

## Hướng dẫn chạy hệ thống

### Yêu cầu hệ thống

| Phần mềm | Phiên bản tối thiểu |
|-----------|---------------------|
| Java (JDK) | 21 |
| Maven | 3.9+ |
| Docker | 24+ |
| Docker Compose | v2+ |
| Git | 2.x |

### Cách 1: Docker Compose

> **Phù hợp cho:** Dev local, demo nhanh, chạy trên 1 máy.

#### Bước 1: Clone project

```bash
git clone git@github.com:thacbaonguyen/neki-be-microservices.git
cd neki-be-microservices
```

#### Bước 2: Chạy infrastructure + application

```bash
# Chạy toàn bộ (13 containers: 5 infrastructure + 8 application)
docker compose -f docker-compose.dev.yml up -d --build
```

Lệnh này sẽ tự động:
- Build 8 service images từ Dockerfile (multi-stage build)
- Khởi tạo MySQL (3 databases: `users_db`, `products_db`, `orders_db`)
- Khởi tạo PostgreSQL (2 databases: `payments_db`, `recommendations_db`)
- Khởi tạo Redis, RabbitMQ, Elasticsearch
- Khởi chạy 8 Spring Boot services

> ⏳ Lần đầu build mất khoảng 5-10 phút. Các lần sau nhanh hơn nhờ Docker cache.

#### Bước 3: Chạy Monitoring (Prometheus + Grafana)

```bash
docker compose -f docker-compose.monitoring.yml up -d
```

#### Bước 4: Chạy Logging + Tracing (Loki + Promtail + Tempo)

```bash
docker compose -f docker-compose.logging.yml up -d
```

#### Bước 5: Kiểm tra

```bash
# Xem tất cả containers
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Test API qua Gateway
curl http://localhost:8080/api/v1/products
```

#### Bảng port mapping

| Service | Port | URL |
|---------|------|-----|
| API Gateway | 8080 | http://localhost:8080 |
| Service Registry (Eureka) | 8761 | http://localhost:8761 |
| User Service | 8081 | |
| Product Service | 8082 | |
| Order Service | 8083 | |
| Payment Service | 8084 | |
| Recommendation Service | 8085 | |
| Notification Service | 8086 | |
| MySQL | 3308 | |
| PostgreSQL | 5432 | |
| Redis | 6379 | |
| RabbitMQ Management | 15672 | http://localhost:15672 (guest/guest) |
| Elasticsearch | 9200 | |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3001 | http://localhost:3001 (admin/admin123) |
| Loki | 3100 | |
| Tempo | 3200 | |

#### Dừng hệ thống

```bash
docker compose -f docker-compose.dev.yml down
docker compose -f docker-compose.monitoring.yml down
docker compose -f docker-compose.logging.yml down
```

---

### Cách 2: Kubernetes (Minikube)

> **Phù hợp cho:** Demo production-like, auto-scaling, zero-downtime deployment.

#### Bước 1: Cài đặt công cụ

```bash
# Cài Minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

# Cài kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install kubectl /usr/local/bin/kubectl
```

#### Bước 2: Khởi tạo cluster

```bash
# Cần tối thiểu 8GB RAM, 4 CPUs
minikube start --memory=8192 --cpus=4 --driver=docker

# Bật Ingress addon
minikube addons enable ingress

# Kiểm tra
kubectl get nodes
```

#### Bước 3: Build & push images lên ghcr.io

```bash
# Tạo GitHub Personal Access Token:
# GitHub → Settings → Developer settings → Personal access tokens
# Permissions cần: write:packages, read:packages

export GITHUB_USERNAME=thacbaonguyen
export GITHUB_TOKEN=ghp_xxxxxxxxxxxx

# Build & push 8 images
./scripts/k8s-build-push.sh
```

#### Bước 4: Deploy từng phase

```bash
# Phase 1: Namespace + Secrets
./scripts/k8s-deploy.sh 1

# Phase 2: Infrastructure (MySQL, PostgreSQL, Redis, RabbitMQ, Elasticsearch)
./scripts/k8s-deploy.sh 2

# Đợi infrastructure pods sẵn sàng
kubectl get pods -n neki -w
# (Ctrl+C khi tất cả pods đều Running)

# Phase 3: Application Services (8 Spring Boot services)
./scripts/k8s-deploy.sh 3

# Phase 4: Observability (Prometheus, Grafana, Loki, Tempo, Promtail)
./scripts/k8s-deploy.sh 4

# Phase 5: Ingress
./scripts/k8s-deploy.sh 5
```

#### Bước 5: Truy cập

```bash
# Thêm hostname vào /etc/hosts
echo "$(minikube ip) neki.local grafana.neki.local" | sudo tee -a /etc/hosts

# Test API
curl http://neki.local/api/v1/products

# Grafana: http://grafana.neki.local (admin/admin123)
# Hoặc: http://$(minikube ip):30001
```

#### Lệnh hữu ích

```bash
# Xem status toàn bộ
./scripts/k8s-deploy.sh status

# Xem logs 1 service
kubectl logs -f deployment/order-service -n neki

# Scale service
kubectl scale deployment/order-service --replicas=3 -n neki

# Xóa toàn bộ cluster
./scripts/k8s-deploy.sh delete
```

---

## 1. Mô tả bài toán

Xây dựng hệ thống **bán hàng trực tuyến** (E-Commerce) theo kiến trúc **Microservice**, trong đó:

- Các dịch vụ được **tách rời**, triển khai **độc lập**
- Giao tiếp **đồng bộ** qua RESTful API (OpenFeign)
- Giao tiếp **bất đồng bộ** qua Message Queue (RabbitMQ)
- Mỗi service có **database riêng** (Database per Service pattern)
- Hỗ trợ **JWT authentication**, **API Gateway**, **Service Discovery**

### Chức năng chính

- Quản lý người dùng (đăng ký, đăng nhập, JWT, phân quyền)
- Quản lý sản phẩm (CRUD, danh mục, thuộc tính, tìm kiếm Elasticsearch)
- Quản lý đơn hàng (giỏ hàng, wishlist, đơn hàng, vận chuyển, mã giảm giá)
- Thanh toán (tích hợp PayOS gateway)
- Gợi ý sản phẩm (recommendation engine)
- Thông báo (email tự động qua RabbitMQ)

---

## 2. Kiến trúc hệ thống

```
                          ┌──────────────┐
                          │   Client     │
                          └──────┬───────┘
                                 │
                          ┌──────▼───────┐
                          │ API Gateway  │ :8080
                          │(Spring Cloud)│
                          └──────┬───────┘
                                 │
                    ┌────────────┼────────────┐
                    │    Eureka Service        │
                    │    Registry :8761        │
                    └────────────┬────────────┘
                                 │
        ┌────────┬───────┬───────┼───────┬──────────┬──────────┐
        │        │       │       │       │          │          │
   ┌────▼───┐┌───▼──┐┌──▼───┐┌──▼───┐┌──▼────┐┌───▼────┐┌───▼────┐
   │ User   ││Prod- ││Order ││Pay-  ││Recom- ││Notifi- ││       │
   │Service ││uct   ││Svc   ││ment  ││menda- ││cation  ││       │
   │ :8081  ││Svc   ││:8083 ││Svc   ││tion   ││Svc     ││       │
   └───┬────┘│:8082 │└──┬───┘│:8084 ││Svc    ││:8086   ││       │
       │     └──┬───┘   │    └──┬───┘│:8085  │└───┬────┘│       │
       │        │       │       │    └──┬────┘    │     │       │
   ┌───▼────────▼───┐ ┌─▼──┐ ┌─▼──┐ ┌──▼──┐  ┌───▼─────▼──┐    │
   │  MySQL         │ │Redis│ │Post│ │Redis│  │ RabbitMQ   │    │
   │users/products/ │ │    │ │gre │ │    │  │            │    │
   │orders_db       │ │    │ │SQL │ │    │  │            │    │
   └────────────────┘ └────┘ └────┘ └────┘  └────────────┘    │
                                                               │
                                                    ┌──────────▼──┐
                                                    │Elasticsearch│
                                                    └─────────────┘
```

### Giao tiếp giữa các service

| Loại | Công nghệ | Sử dụng cho |
|------|-----------|-------------|
| Đồng bộ (Sync) | OpenFeign + Eureka | Gọi API giữa services (kiểm kho, lấy user info) |
| Bất đồng bộ (Async) | RabbitMQ | Gửi event (đặt hàng, thanh toán, thông báo) |

### Database per Service

| Service | Database | Engine |
|---------|----------|--------|
| User Service | `users_db` | MySQL 8 |
| Product Service | `products_db` | MySQL 8 |
| Order Service | `orders_db` | MySQL 8 |
| Payment Service | `payments_db` | PostgreSQL 16 |
| Recommendation Service | `recommendations_db` | PostgreSQL 16 |

---

## 3. Mô tả các Microservice

### 3.1. Service Registry (Eureka)
- **Port:** 8761
- **Vai trò:** Service Discovery — tất cả services đăng ký tại đây để tìm thấy nhau
- **Công nghệ:** Spring Cloud Netflix Eureka Server

### 3.2. API Gateway
- **Port:** 8080
- **Vai trò:** Điểm vào duy nhất, routing request đến đúng service, JWT validation, Rate Limiting
- **Công nghệ:** Spring Cloud Gateway + Redis (rate limiting)
- **Routes:**
  - `/api/v1/auth/**`, `/api/v1/users/**` → User Service
  - `/api/v1/products/**`, `/api/v1/categories/**`, `/api/v1/search/**` → Product Service
  - `/api/v1/orders/**`, `/api/v1/cart/**`, `/api/v1/wishlist/**` → Order Service
  - `/payment/**`, `/api/v1/payment-method/**` → Payment Service
  - `/api/v1/recommendations/**` → Recommendation Service

### 3.3. User Service
- **Port:** 8081 | **DB:** MySQL (`users_db`)
- **Chức năng:** Đăng ký, đăng nhập, JWT token, refresh token, quản lý user, phân quyền RBAC
- **Publish events:** `user.registered`, `user.forgot-password`

### 3.4. Product Service
- **Port:** 8082 | **DB:** MySQL (`products_db`)
- **Chức năng:** CRUD sản phẩm, danh mục, thuộc tính (size, color), banner, tìm kiếm Elasticsearch
- **Listen events:** `order.created` (trừ tồn kho), `order.cancelled` (hoàn tồn kho)
- **Tích hợp:** Cloudinary (upload ảnh), Elasticsearch (full-text search)

### 3.5. Order Service
- **Port:** 8083 | **DB:** MySQL (`orders_db`)
- **Chức năng:** Giỏ hàng, wishlist, đơn hàng, vận chuyển, mã giảm giá, thống kê
- **Publish events:** `order.created`, `order.cancelled`, `order.updated` (qua Outbox Pattern)
- **Listen events:** `payment.completed`
- **Feign clients:** Product Service (kiểm kho), User Service (thông tin user)

### 3.6. Payment Service
- **Port:** 8084 | **DB:** PostgreSQL (`payments_db`)
- **Chức năng:** Tạo link thanh toán, xử lý webhook callback, quản lý payment methods
- **Publish events:** `payment.completed`, `payment.failed`
- **Feign clients:** Order Service (cập nhật trạng thái đơn)
- **Tích hợp:** PayOS (cổng thanh toán)

### 3.7. Recommendation Service
- **Port:** 8085 | **DB:** PostgreSQL (`recommendations_db`)
- **Chức năng:** Gợi ý sản phẩm dựa trên lịch sử mua hàng
- **Listen events:** `order.created` (cập nhật hành vi user)
- **Feign clients:** Product Service, Order Service

### 3.8. Notification Service
- **Port:** 8086 | **DB:** Không có
- **Chức năng:** Gửi email thông báo (đăng ký, đặt hàng, thanh toán, quên mật khẩu)
- **Listen events:** Tất cả events từ User, Order, Payment

---

## 4. Luồng Message Queue (RabbitMQ)

### Exchanges

| Exchange | Type | Mô tả |
|----------|------|-------|
| `neki.order.exchange` | Topic | Sự kiện liên quan đến đơn hàng |
| `neki.payment.exchange` | Topic | Sự kiện thanh toán |
| `neki.user.exchange` | Topic | Sự kiện người dùng |

### Queues & Luồng

```
User Service ──publish──► neki.user.exchange
                              ├──► notification.user.registered     → Notification Service (gửi email chào mừng)
                              └──► notification.user.password       → Notification Service (gửi email reset password)

Order Service ──publish──► neki.order.exchange
                              ├──► product.order.created            → Product Service (trừ tồn kho)
                              ├──► product.order.cancelled          → Product Service (hoàn tồn kho)
                              ├──► notification.order.created       → Notification Service (email xác nhận đơn)
                              ├──► notification.order.cancelled     → Notification Service (email hủy đơn)
                              ├──► notification.order.updated       → Notification Service (email cập nhật đơn)
                              └──► recommendation.order.created     → Recommendation Service (cập nhật gợi ý)

Payment Service ──publish──► neki.payment.exchange
                              ├──► order.payment.completed          → Order Service (cập nhật trạng thái đơn)
                              └──► notification.payment             → Notification Service (email thanh toán)
```

### Outbox Pattern

Order Service sử dụng **Transactional Outbox Pattern** để đảm bảo tính nhất quán giữa database và message queue. Events được lưu vào bảng `outbox_event` trước, sau đó `OutboxEventRelay` (scheduled job) đọc và gửi vào RabbitMQ.

---

## 5. Giao tiếp đồng bộ (Feign Client)

| Caller | Callee | Mục đích | Fallback |
|--------|--------|----------|----------|
| Order Service | Product Service | Kiểm tra tồn kho | FallbackFactory |
| Order Service | User Service | Lấy thông tin user | FallbackFactory |
| Payment Service | Order Service | Cập nhật trạng thái đơn | FallbackFactory |
| Product Service | Recommendation Service | Lấy gợi ý sản phẩm | Fallback class |
| Recommendation Service | Product Service | Lấy thông tin sản phẩm | FallbackFactory |
| Recommendation Service | Order Service | Lấy lịch sử mua | FallbackFactory |

Tất cả Feign clients đều có **Fallback** (Circuit Breaker) để xử lý khi service đích không phản hồi.

---

## 6. Unit Test

Hệ thống có **32 test files** phân bổ như sau:

| Service | Test files | Phạm vi test |
|---------|-----------|--------------|
| User Service | 7 | UserServiceImpl, TokenServiceImpl, JWT, Security |
| Order Service | 7 | CartService, OrderService, WishlistService, Outbox, Listener |
| Product Service | 4 | ProductService, CategoryService, AttributeService, BannerService |
| Payment Service | 4 | PaymentService, PaymentMethodService, Listener |
| Notification Service | 4 | EmailService, Listeners |
| Common Lib | 3 | JwtUtils, GlobalExceptionHandler, AppException |
| API Gateway | 2 | JwtUtil, JwtAuthGatewayFilterFactory |
| Recommendation Service | 1 | RecommendationService |

### Chạy test

```bash
# Chạy toàn bộ tests
mvn test

# Chạy test 1 service cụ thể
mvn test -pl user-service

# Chạy test + JaCoCo coverage report
mvn test jacoco:report
# Report HTML: <service>/target/site/jacoco/index.html
```

---

## 7. Logging (Centralized)

### Stack: Promtail → Loki → Grafana

```
Spring Boot Services ──stdout JSON──► Docker ──► Promtail ──push──► Loki ──query──► Grafana
```

### Structured Logging

- **Local (profile default):** Log dạng text đẹp với màu sắc
  ```
  2026-05-17 14:20:15 INFO  [main] [traceId/spanId] c.t.o.s.OrderServiceImpl - Order created
  ```
- **Docker (profile docker):** Log dạng JSON structured
  ```json
  {"timestamp":"2026-05-17T14:20:15","level":"INFO","message":"Order created","service":"order-service","traceId":"abc123"}
  ```

### Grafana Dashboard "Neki Logs"

- Log Volume by Service (bar chart)
- Error Rate by Service (time series)
- Log Level Distribution (pie chart)
- WARN + ERROR Count (stat panel)
- Live Log Stream (real-time)

### Alert Rules

| Rule | Điều kiện | Kênh thông báo |
|------|-----------|----------------|
| High Error Rate | >10 ERROR/5min | Email + Telegram |
| Service Error Spike | >5 ERROR/5min (1 service) | Email + Telegram |
| No Logs Received | 0 logs/10min từ 1 service | Email + Telegram |

### Config files

| File | Mô tả |
|------|-------|
| `monitoring/loki/loki-config.yml` | Loki server: 15-day retention |
| `monitoring/promtail/promtail-config.yml` | Promtail: Docker socket discovery |
| `common-lib/src/main/resources/logback-spring.xml` | Logback: text (dev) / JSON (docker) |

---

## 8. Monitoring & Distributed Tracing

### Metrics: Prometheus + Grafana

- Mỗi service expose metrics tại `/actuator/prometheus`
- Prometheus scrape mỗi 15s
- Grafana dashboard tự động provision

### Distributed Tracing: Micrometer Tracing + Tempo

```
Request ──► API Gateway ──► Order Service ──► Product Service
                │                │                  │
                └── traceId=abc123 xuyên suốt ──────┘
                                 │
                          Tempo (lưu trace)
                                 │
                          Grafana (hiển thị waterfall)
```

- **Micrometer Tracing** tự động gắn `traceId` vào mọi HTTP request, Feign call, RabbitMQ message
- **Tempo** thu thập traces qua OTLP gRPC (port 4317)
- **Grafana** hiển thị trace waterfall + liên kết trace ↔ log ↔ metric

### 3 Trụ cột Observability

| Trụ cột | Công cụ | Dữ liệu |
|---------|---------|----------|
| **Metrics** | Prometheus → Grafana | CPU, RAM, request count, latency |
| **Logs** | Promtail → Loki → Grafana | Application logs (JSON structured) |
| **Traces** | Micrometer → Tempo → Grafana | Request tracing xuyên services |

---

## 9. Cấu trúc thư mục

```
neki-microservice/
├── common-lib/                      # Shared DTOs, events, exceptions, constants
├── service-registry/                # Eureka Server
├── api-gateway/                     # Spring Cloud Gateway
├── user-service/                    # User management + JWT auth
├── product-service/                 # Product CRUD + Elasticsearch
├── order-service/                   # Cart, Order, Wishlist, Discount
├── payment-service/                 # PayOS integration
├── recommendation-service/          # Product recommendations
├── notification-service/            # Email notifications
├── docker/                          # DB init scripts
│   ├── mysql-init.sql
│   └── postgres-init.sql
├── monitoring/                      # Observability configs
│   ├── loki/
│   ├── promtail/
│   ├── tempo/
│   ├── prometheus.yml
│   └── grafana/
│       ├── dashboards/
│       └── provisioning/
├── k8s/                             # Kubernetes manifests
│   ├── namespace.yml
│   ├── secrets/
│   ├── infrastructure/
│   ├── apps/
│   ├── monitoring/
│   └── ingress.yml
├── scripts/                         # Automation scripts
│   ├── k8s-build-push.sh
│   └── k8s-deploy.sh
├── docker-compose.dev.yml           # Full stack (infrastructure + apps)
├── docker-compose.monitoring.yml    # Prometheus + Grafana
├── docker-compose.logging.yml       # Loki + Promtail + Tempo
├── docker-compose.nobuild.yml       # Infrastructure only (dev local)
└── pom.xml                          # Root Maven POM (multi-module)
```
