# syntax=docker/dockerfile:1

# ---------- Fase 1: compilazione di Angular ----------
# Angular 22 richiede Node 22.22.3 o più recente (oppure 24.15+): con la 24 si è sempre a posto
FROM node:24-alpine AS build
WORKDIR /app

# Prima solo i file delle dipendenze: se non cambiano, npm ci resta in cache
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- Fase 2: nginx serve i file già compilati ----------
FROM nginx:stable-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontendArena/browser /usr/share/nginx/html

EXPOSE 80
