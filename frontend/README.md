# emBrain — Frontend

**Status: Phase 1 placeholder — scaffold will be built in Phase 2.**

## Planned Stack
| Concern | Technology |
|---|---|
| Framework | Next.js 14 (App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS |
| State | React Context / Zustand |
| Auth | httpOnly cookie JWT |

## Phase 2 Scope
- Sign up, login, and logout pages
- JWT stored in httpOnly cookies, managed via the Auth Service
- Basic routing and layout shell

## Running Locally (Phase 2+)
```bash
cd frontend
npm install
npm run dev       # http://localhost:3000
```

## Environment Variables (Phase 2+)
```env
NEXT_PUBLIC_API_URL=http://localhost/api/v1
```
