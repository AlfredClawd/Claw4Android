# OpenClaw Android Client - Architecture

## 1. Overview
The OpenClaw Android Client is a native Java application designed to interface with the OpenClaw Gateway. It provides a chat interface, system monitoring, and configuration management.

## 2. Tech Stack
- **Language:** Java 17
- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Architecture Pattern:** MVVM (Model-View-ViewModel)
- **Build System:** Gradle (Kotlin DSL preferred for build scripts, but Groovy is fine too. We will use Groovy for compatibility).

## 3. Libraries
- **Networking:** Retrofit2 (REST), OkHttp3 (WebSocket/HTTP)
- **JSON Parsing:** Gson
- **Dependency Injection:** Manual (Simple Locator) or Hilt (Overkill for now, keeping it simple). -> **Manual Injection** (Singleton Managers).
- **UI Components:** Material Design 3, AndroidX (AppCompat, ConstraintLayout, RecyclerView).
- **Async:** `java.util.concurrent` (Executors) or standard Callbacks.

## 4. Modules / Packages (`ai.openclaw.android`)
- **`api`**: Retrofit interfaces, Data Models (DTOs), WebSocket client.
- **`ui`**: Activities, Fragments, Adapters.
    - `chat`: ChatFragment, ChatAdapter.
    - `dashboard`: DashboardFragment (System stats).
    - `settings`: SettingsFragment (Config).
- **`viewmodel`**: ViewModels for the UI screens.
- **`util`**: Helpers (Prefs, Formatting).

## 5. API Contract (Assumed OpenClaw Gateway)
The client connects to `gatewayUrl` (e.g., `http://192.168.1.100:19001`) with a `Bearer Token`.

### REST Endpoints
- `GET /api/v1/system/status` -> Returns generic system health, version.
- `GET /api/v1/system/limits` -> Returns rate limits (LLM quotas).
- `GET /api/v1/models` -> Returns available models.
- `POST /api/v1/chat/send` -> Sends a message (or use WebSocket).

### WebSocket
- Path: `/v1/gateway/socket` or similar.
- Events: `message.received`, `status.changed`.

## 6. Data Flow
1. **User** opens App.
2. **App** checks `SharedPreferences` for `gatewayUrl` and `token`.
3. If missing -> Show **Login/Config Screen**.
4. If present -> Connect **WebSocket** & Fetch **Initial State** (REST).
5. **Dashboard** listens to `LiveData` from `SystemRepository`.
6. **Chat** sends messages via `ChatRepository` -> API.

## 7. Next Steps (Kimi Tasks)
1. **Scaffold:** Create `build.gradle`, `AndroidManifest.xml`, `settings.gradle`.
2. **Network Layer:** Implement `ApiClient` (Retrofit) and `SocketManager`.
3. **Data Models:** Create POJOs for `Message`, `Status`, `RateLimit`.
4. **UI Skeleton:** Create `MainActivity` with Bottom Navigation.
