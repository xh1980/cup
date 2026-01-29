# ---------- 构建阶段 ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# 拷贝构建产物
COPY --from=builder /build/target/app.jar app.jar

# JVM参数（可根据需要调整）
ENV JAVA_OPTS="-Xms128m -Xmx512m"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
