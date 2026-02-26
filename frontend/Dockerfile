FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
# Usamos install para evitar errores si falta el lockfile
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine AS runner

# Patch all OS-level vulnerabilities detected by Trivy (US-004)
RUN apk upgrade --no-cache

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 5173
CMD ["nginx", "-g", "daemon off;"]
