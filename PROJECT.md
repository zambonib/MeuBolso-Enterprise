# Project: Meu Bolso Enterprise

## Architecture
- **Backend**: Java 21 + Spring Boot 3 + Spring Security + JWT + Oracle DB (`jdbc:oracle:thin:@10.0.0.200:1521/XEPDB1`, schema `meubolso`, password `MeuBolso@2026`).
- **Multi-tenancy**: SaaS multi-tenant isolation via User/Tenant scoping on all entities (Usuario, Conta, Categoria, Transacao). All requests authenticated via JWT header (`Authorization: Bearer <token>`). Every DB query filters strictly by tenant user.
- **Frontend**: React + Vite in `frontend/`. Converts Nimbus UI 2.1 design components, integrates with backend REST API (Axios/Fetch), including Login, Dashboard Principal, and Lançamentos.
- **Version Control**: Git remote `https://github.com/zambonib/MeuBolso-Enterprise.git`, branch `main`, strictly Portuguese commit messages.

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Backend Auth & Security | Spring Security, JWT auth, User entity, Login/Register endpoints, Tenant isolation foundation | none | DONE |
| M2 | Backend REST API Endpoints | Account, Category, Transaction REST CRUD, User scoping, validation, balance calculations | M1 | DONE |
| M3 | Frontend Nimbus UI React | Convert Nimbus UI HTML/CSS to React components (Dashboard, Lançamentos, Auth/Login), API state | M2 | DONE |
| M4 | E2E Integration & Testing | Integration test suite, multi-tenant security verification, JWT guard verification | M3 | DONE |
| M5 | Versioning & Push | Git commits in Portuguese, push to remote `https://github.com/zambonib/MeuBolso-Enterprise.git` main branch | M4 | DONE |

## Interface Contracts
### Auth API (`/api/auth`)
- `POST /api/auth/register`: Request `{ username, email, password, name }` -> Response `{ token, user }`
- `POST /api/auth/login`: Request `{ email, password }` -> Response `{ token, user }`
- `GET /api/auth/me`: Header `Authorization: Bearer <token>` -> Response `{ id, username, email, name }`

### REST API Scoped Endpoints (`/api/contas`, `/api/categorias`, `/api/transacoes`)
- `GET /api/contas`, `POST /api/contas`, `PUT /api/contas/{id}`, `DELETE /api/contas/{id}`
- `GET /api/categorias`, `POST /api/categorias`, `PUT /api/categorias/{id}`, `DELETE /api/categorias/{id}`
- `GET /api/transacoes`, `POST /api/transacoes`, `PUT /api/transacoes/{id}`, `DELETE /api/transacoes/{id}`
- All endpoints filter strictly by the logged-in user's ID. Accessing another user's entity returns 404 Not Found.

## Code Layout
- Backend: `backend/src/main/java/com/meubolso/backend/`
  - `config/` (SecurityConfig, JwtAuthenticationFilter, CorsConfig)
  - `security/` (JwtTokenProvider, CustomUserDetailsService, UserPrincipal, SecurityUtils)
  - `entity/` (Usuario, Conta, Categoria, Transacao, TipoTransacao, TipoConta)
  - `repository/` (UsuarioRepository, ContaRepository, CategoriaRepository, TransacaoRepository)
  - `service/` (AuthService, ContaService, CategoriaService, TransacaoService)
  - `controller/` (AuthController, ContaController, CategoriaController, TransacaoController)
  - `dto/` (RegisterRequest, LoginRequest, AuthResponse, UserDTO, ContaDTO, CategoriaDTO, TransacaoDTO)
  - `exception/` (ResourceNotFoundException, GlobalExceptionHandler)
- Frontend: `frontend/src/`
  - `components/` (Navbar, Sidebar, StatCard, TransactionModal, etc.)
  - `pages/` (LoginPage, RegisterPage, DashboardPage, TransactionsPage)
  - `context/` (AuthContext.jsx)
  - `services/` (api.js, authService.js, transactionService.js)
