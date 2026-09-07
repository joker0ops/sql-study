# ---- 构建阶段 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 容器内 Maven 配置：使用阿里云镜像（适配国内服务器）
COPY docker/maven-settings.xml /app/settings.xml
COPY pom.xml .
RUN mvn -B -s /app/settings.xml dependency:go-offline

COPY src ./src
RUN mvn -B -s /app/settings.xml -DskipTests package

# ---- 运行阶段 ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/sql-learn-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
