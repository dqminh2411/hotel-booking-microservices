# ============================================================
# user-service Dockerfile
# Multi-stage build: builder → runtime
# ============================================================

# ── Stage 1: Builder ─────────────────────────────────────────
# Dùng full JDK để compile và package JAR
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper và pom.xml TRƯỚC — Docker cache layer này
# sẽ không bị invalidate khi chỉ thay đổi source code
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cache layer riêng — chỉ re-run khi pom.xml thay đổi)
RUN ./mvnw -Dmaven.wagon.http.retryHandler.count=3 \
           -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
           -Dmaven.wagon.http.connectionTimeout=120000 \
           -Dmaven.wagon.http.readTimeout=120000 \
           dependency:go-offline -q

# Copy source code và build JAR (bỏ qua tests — chạy tests ở CI pipeline)
COPY src src
RUN ./mvnw package -DskipTests -q

# Extract layers từ JAR để tối ưu Docker layer caching ở runtime stage
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ── Stage 2: Runtime ─────────────────────────────────────────
# Chỉ dùng JRE (nhẹ hơn JDK ~200MB), không có compiler
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install dumb-init: tránh zombie processes, xử lý signal đúng cách
# (quan trọng khi Docker gửi SIGTERM để graceful shutdown)
RUN apk add --no-cache dumb-init

# Tạo non-root user — không chạy process với quyền root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy các layers theo thứ tự từ ít thay đổi đến nhiều thay đổi nhất
# → Docker cache hiệu quả hơn: dependencies layer hiếm khi thay đổi
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/dependencies ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/spring-boot-loader ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/snapshot-dependencies ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/application ./

# Chuyển sang non-root user
USER appuser

# Port của user-service
EXPOSE 5000

# Health check: Docker tự kiểm tra service có hoạt động không
# --interval: kiểm tra mỗi 30s
# --timeout: timeout mỗi lần check sau 5s
# --start-period: chờ 60s trước khi bắt đầu check (Spring Boot cần thời gian khởi động)
# --retries: thử lại 3 lần trước khi đánh dấu unhealthy
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:5000/health | grep -q '"status":"ok"' || exit 1

# dumb-init làm PID 1, chạy JVM với layered launcher
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "org.springframework.boot.loader.launch.JarLauncher"]
