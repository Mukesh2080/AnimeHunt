#   Anime Hunt — Offline-First Anime Explorer (Android)

Anime Hunt is a modern **offline-first Android application** built with **Kotlin, MVVM, Room, Paging 3, and Retrofit**, powered by the **Jikan (MyAnimeList) API**.

The app focuses on **smooth UX**, **graceful offline handling**, and **automatic recovery when the network returns**.

---

##   Features

###   Anime List
- Top anime list from Jikan API
- **Paging 3 + RemoteMediator**
- **Room as single source of truth**
- Manual **Refresh / Sync** from toolbar
- Auto-refresh when network becomes available

###  Anime Details
- Detailed anime info (rating, episodes, synopsis, genres)
- Trailer playback using **YouTube embed WebView**
- Poster fallback when trailer is unavailable
- **Offline-aware behavior**
  - WebView never loads while offline
  - Offline message shown instead
  - Auto-fetch when network comes back

###  Network Awareness (Global)
- App-wide network monitoring via `Application` module
- Live online/offline state using `StateFlow`
- Snackbar notifications:
  - Offline mode
  - Back online
  - Syncing data

### 🎥 Trailer Playback
- Fullscreen video support

---

##  Architecture
MVVM + Repository Pattern

### Layers
- **UI**: Activities, Adapters
- **ViewModel**: StateFlow, lifecycle-aware
- **Repository**: Single source of truth
- **Database**: Room
- **Network**: Retrofit
- **Paging**: Paging 3 + RemoteMediator

### Offline-First Strategy
- UI reads **only from Room**
- Network writes **only via RemoteMediator**
- Paging automatically updates UI
- Detail screen retries fetch when network returns

---

##  Tech Stack

| Category | Technology |
|-------|-----------|
Language | Kotlin |
Architecture | MVVM |
Async | Coroutines + Flow |
Database | Room |
Pagination | Paging 3 |
Networking | Retrofit |
Images | Glide |
UI | Material Design |
Web | WebView (YouTube embed) |

---

## 🔁 Paging & Offline Behavior

### Anime List
- Uses `RemoteMediator`
- Stable ordering using `pageIndex`
- No list jumps on database updates
- Automatically resumes pagination after reconnect

### Anime Detail
- Cache-first approach
- Fetch blocked while offline
- Auto-retry when network becomes available
- No duplicate API calls

---

##  UX Decisions

- ❌ No WebView loading when offline
- ❌ No empty video frames
- ❌ No manual retry required on reconnect
- ✅ Clear offline messages
- ✅ Smooth UI updates
- ✅ Predictable user experience

---

##  API

**Jikan API (Unofficial MyAnimeList API)**  
https://docs.api.jikan.moe/

---
## Demo Attached
https://github.com/user-attachments/assets/f93980f3-cd24-478f-a77c-a670ce984358

---




##  Getting Started

1. Clone the repository
```bash
git clone https://github.com/Mukesh2080/anime-hunt.git



