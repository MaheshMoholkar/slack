# Slack

Full-stack Slack-style chat app with:
- `server/`: Spring Boot API (Java 17)
- `client/`: React + Vite UI served by Nginx

This repo is configured for split deployment (separate API and UI images).

## Architecture

- UI image: `mahesh1822/slack-ui:latest`
- API image: `mahesh1822/slack-api:latest`
- UI proxy routes:
- `/api/*` -> API service
- `/ws` -> API service (WebSocket/SockJS)

## Local Run (Docker Compose)

Requirements:
- Docker + Docker Compose

Files used:
- `.env.api` (API runtime variables)
- `.env.ui` (UI nginx upstream variables)

Run:

```bash
docker compose up --build
```

Endpoints:
- UI: `http://localhost:3000`
- API: `http://localhost:8080`

## Railway Deployment (Split Services)

Deploy 2 Railway services from Docker images:

1. API service
- Image: `mahesh1822/slack-api:latest`
- Port: `8080`
- Set API env vars from `.env.api`

2. UI service
- Image: `mahesh1822/slack-ui:latest`
- Port: `80`
- Set UI env vars:

```env
API_UPSTREAM=http://slack-api.railway.internal:8080
WS_UPSTREAM=http://slack-api.railway.internal:8080
```

Networking recommendation:
- Expose UI publicly
- Keep API private/internal

## Demo Seed Data (Important)

On API startup, demo seeding currently does a hard DB reset and reseeds data.

This means:
- Existing DB data is deleted on every API start/redeploy.
- Fresh demo data is inserted (workspaces, channels, messages, thread replies, reactions).

Guest accounts:
- `guest.user1@slack-app.dev` / `GuestUser@123`
- `guest.user2@slack-app.dev` / `GuestUser@123`

## Build and Push Images

```bash
docker buildx build --platform linux/amd64 -t mahesh1822/slack-api:latest -f server/Dockerfile server --push
docker buildx build --platform linux/amd64 -t mahesh1822/slack-ui:latest -f client/Dockerfile client --push
```

## Repo Layout

- `client/` -> React UI + nginx config
- `server/` -> Spring Boot API
- `docker-compose.yml` -> local split setup
- `.env.api` -> API env
- `.env.ui` -> UI env
