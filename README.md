# Hermes Hub 📱

**Aplikasi Android client khusus untuk ngakses Hermes Agent.**

Bukan server — tapi **pintu gerbang** buat ngobrol sama Hermes Agent milik siapapun yang udah aktifin API Server-nya.

---

## ✨ Fitur

| Fitur | Description |
|-------|-------------|
| **Multi-Koneksi** | Simpan banyak koneksi Hermes Agent, gampang pindah |
| **Streaming Chat** | Response muncul realtime via SSE |
| **Test Koneksi** | Cek dulu sebelum disimpan |
| **Riwayat Lokal** | Chat tersimpan di HP masing-masing user (Room DB) |
| **Dark Theme** | Tampilan gelap khas developer |
| **API Key Auth** | Aman pake Bearer token |

---

## 🚀 Quick Start — Build APK

### Cara 1: GitHub Actions (Termudah ✅)

1. **Fork / Push** repo ini ke GitHub
2. Buka **Actions** tab → **Build Hermes Hub APK**
3. Klik **Run workflow** → tunggu selesai
4. Download APK dari **Artifacts**

### Cara 2: Build Lokal (Android Studio)

```bash
git clone https://github.com/username/hermes-hub-android
cd hermes-hub-android
# Buka di Android Studio → Sync Gradle → Run
```

---

## 🔌 Cara User Connect Hermes Agent-nya

### Prasyarat (di sisi user):
User harus punya **Hermes Agent** yang berjalan dengan **API Server aktif**.

### 🔧 Setup Hermes API Server

User menjalankan ini di **Termux / VPS / Terminus / laptop** mereka:

```bash
# 1. Edit file .env Hermes
nano ~/.hermes/.env
```

Tambahin ini:
```env
API_SERVER_ENABLED=true
API_SERVER_KEY=bikin-kata-sandi-sendiri
API_SERVER_PORT=8642
# Biar bisa diakses dari luar localhost (WAJIB untuk akses HP)
API_SERVER_HOST=0.0.0.0
```

```bash
# 2. Restart gateway Hermes
hermes gateway restart

# 3. Cek apakah API Server udah jalan
curl http://localhost:8642/v1/models \
  -H "Authorization: Bearer bikin-kata-sandi-sendiri"
# → Harusnya balik JSON
```

### 📱 Cara Connect di Aplikasi

Buka Hermes Hub → **Tambah Koneksi** → isi:

| Field | Isi |
|-------|-----|
| **Nama Koneksi** | Bebas (contoh: "Hermes VPS") |
| **Base URL** | `http://<IP_USER>:8642` (bisa IP lokal/VPS) |
| **API Key** | Kata sandi dari `API_SERVER_KEY` |

> 💡 **Tips:** Kalo user di Termux, IP-nya bisa dicek pake `ip a` atau `ifconfig`. Kalo di VPS, pake IP publik VPS-nya.

### 🔒 Biar Aman (Opsional)

Kalo user mau akses dari luar jaringan (internet), mending pasang **SSL** atau **tunnel**:

```bash
# Pake Cloudflare Tunnel (gratis)
cloudflared tunnel --url http://localhost:8642
# → Dapet URL https://xxxx.trycloudflare.com
```

Atau pake **Ngrok**:
```bash
ngrok http 8642
# → Dapet URL https://xxxx.ngrok.io
```

---

## 🧱 Tech Stack

| Layer | Teknologi |
|-------|-----------|
| **Bahasa** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM |
| **Networking** | Retrofit 2 + OkHttp SSE |
| **Database** | Room (SQLite) |
| **Navigation** | Compose Navigation |
| **Streaming** | OkHttp EventSource (SSE) |

---

## 📁 Project Structure

```
app/src/main/java/com/hermeshub/
├── HermesHubApp.kt           # Application class
├── MainActivity.kt           # Entry point + Navigation
├── data/
│   ├── model/Models.kt       # Data class (Connection, Message, API)
│   ├── local/Database.kt     # Room DB (DAO + Database)
│   ├── remote/HermesApiService.kt  # Retrofit API client
│   └── repository/HermesRepository.kt  # Business logic + streaming
├── viewmodel/
│   └── HermesViewModel.kt    # State management
└── ui/
    ├── theme/Theme.kt        # Dark theme colors
    ├── navigation/Navigation.kt  # Routes
    └── screens/
        ├── connectionlist/   # Daftar koneksi
        ├── chat/             # Chat interface
        └── addconnection/    # Form tambah koneksi
```

---

## 📸 Preview

```
┌────────────────────┐    ┌────────────────────┐
│  ☰ Hermes Hub      │    │ ← Hermes VPS    ⋮  │
├────────────────────┤    ├────────────────────┤
│ 🔵 Hermes VPS     │    │ 👤 Hai Hermes!     │
│    ● Online 5ms   │    │ 🤖 Halo! Ada yg    │
│                    │    │     bisa dibantu?  │
│ 🟢 Hermes Termux  │    │                    │
│    ● Online 20ms  │    │ 👤 Buat API auth   │
│                    │    │ 🤖 Oke, ini codenya│
│ ⚪ Hermes Budi    │    │ ┌────────────────┐ │
│    ○ Offline      │    │ │ def auth()...  │ │
│                    │    │ └────────────────┘ │
├────────────────────┤    ├────────────────────┤
│ [+ Tambah Koneksi]│    │ ✏️ Pesan... 📎 📷 │
└────────────────────┘    └────────────────────┘
  Daftar Koneksi              Chat Screen
```

---

## 🛠️ Development

### Requirements
- Android Studio Hedgehog (2023.1.1+) or later
- JDK 17
- Android SDK 34

### Build variants
```bash
# Debug (development)
./gradlew assembleDebug

# Release (production - perlu signing key)
./gradlew assembleRelease
```

---

## 📄 License

MIT — bebas dipake, dimodifikasi, disebarin.

---

Dibuat dengan ❤️ untuk komunitas Hermes Agent Indonesia 🇮🇩
