# emBrain AI

> AI-powered study assistant for IGCSE & International A-Level students.

## Overview

emBrain provides syllabus-aware academic assistance, examiner-style answers, and intelligent revision support via a microservice architecture backed by a Retrieval-Augmented Generation (RAG) pipeline.

See [`documents/srs.md`](documents/srs.md) for the full Software Requirements Specification and [`documents/development-plan.md`](documents/development-plan.md) for the phased build plan.

---

## Monorepo Structure

```
emBrain/
├── services/
│   ├── auth/         # Java 21 · Spring Boot — authentication & JWT
│   ├── user/         # Java 21 · Spring Boot — profiles & progress
│   ├── ai/           # Python · FastAPI     — prompt building & model calls
│   └── rag/          # Python · FastAPI     — semantic retrieval over gRPC
├── frontend/         # Next.js · TypeScript  — web UI (Phase 2)
├── gateway/          # Nginx config          — API gateway & routing
├── infra/
│   ├── postgres/     # DB init SQL
│   └── redis/        # Redis config
├── proto/            # Shared .proto contracts (AI ↔ RAG, AI ↔ ML Model)
├── documents/        # SRS and development plan
└── docker-compose.yml
```

---

## Running Locally

**Prerequisites:** Docker & Docker Compose installed.

```bash
# Start all services
docker compose up --build

# Or in detached mode
docker compose up --build -d

# Check status
docker compose ps

# Tear down
docker compose down
```

All containers start and become healthy automatically. The first run will build the Java and Python images, which may take a few minutes.

---

## Service Port Map

| Service | Internal Port | Accessible Via |
|---|---|---|
| API Gateway (Nginx) | 80 | `http://localhost` |
| Auth Service | 8081 | `http://localhost/api/v1/auth/` |
| User Service | 8082 | `http://localhost/api/v1/users/` |
| AI Service | 8083 | `http://localhost/api/v1/ai/` |
| RAG Service | 8084 | `http://localhost/api/v1/rag/` |
| PostgreSQL | 5432 | `localhost:5432` |
| Redis | 6379 | `localhost:6379` |
| Frontend (Phase 2) | 3000 | `http://localhost:3000` |

---

## Health Checks

All services expose `/health`. Verify the stack is running:

```bash
curl http://localhost/health                   # gateway ok
curl http://localhost/api/v1/auth/health       # {"status":"ok","service":"auth"}
curl http://localhost/api/v1/users/health      # {"status":"ok","service":"user"}
curl http://localhost/api/v1/ai/health         # {"status":"ok","service":"ai"}
curl http://localhost/api/v1/rag/health        # {"status":"ok","service":"rag"}
```

---

## CI/CD

GitHub Actions runs on every push and pull request to `main`:

| Job | What it does |
|---|---|
| `lint-python` | `flake8` on `services/ai` and `services/rag` |
| `build-java` | Maven build (skip tests) on `services/auth` and `services/user` |
| `health-check` | Spins up Docker Compose, curls every health endpoint, tears down |

---

## gRPC Contracts

Internal service contracts are defined in [`proto/embrain.proto`](proto/embrain.proto):

- `RagService.RetrieveContext` — AI Service → RAG Service
- `ModelService.Generate` — AI Service → ML Model

---

## Development Phases

| Phase | Focus | Weeks |
|---|---|---|
| 1 ✅ | Foundation & infrastructure | 1–3 |
| 2 | Auth & user layer | 4–6 |
| 3 | AI chat MVP | 7–11 |
| 4 | Hardening & observability | 12–14 |
| 5 | Beta & iteration | 15–18 |
