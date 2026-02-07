# OpenClaw Android Client

## Status
**Phase:** Alpha / Skeleton
**Developer:** Kimi K2.5 (via Super-Agent)

## Features Implemented
- **Project Structure:** Standard Android Gradle (Java).
- **Architecture:** MVVM with Retrofit.
- **Navigation:** Bottom Bar (Dashboard, Chat, Settings).
- **Dashboard:** Fetches Rate Limits from `GET /api/v1/system/limits` (Mocked).

## Setup
1. Open in Android Studio.
2. Sync Gradle.
3. Run on Emulator/Device.

## Configuration
Default Gateway: `http://10.0.2.2:19001` (Localhost from Emulator).
To change, edit `ApiClient.java` or implement the Settings screen.
