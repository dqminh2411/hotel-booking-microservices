# ============================================================
# booking-service Dockerfile
# Multi-stage build: builder → runtime
# ============================================================

# ── Stage 1: Builder ─────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk add --no-cache dumb-init

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/target/extracted/dependencies ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/spring-boot-loader ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/snapshot-dependencies ./
COPY --from=builder --chown=appuser:appgroup /build/target/extracted/application ./

USER appuser

EXPOSE 5000

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:5000/health | grep -q '"status":"ok"' || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "org.springframework.boot.loader.launch.JarLauncher"]
