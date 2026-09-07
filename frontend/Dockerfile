# ---- 构建阶段 ----
FROM node:20-alpine AS build
WORKDIR /app

# 国内服务器可覆盖为 npmmirror，默认已是国内镜像
ARG NPM_REGISTRY=https://registry.npmmirror.com

COPY package.json package-lock.json ./
RUN npm ci --registry=$NPM_REGISTRY

COPY . .
RUN npm run build

# ---- 运行阶段 ----
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
