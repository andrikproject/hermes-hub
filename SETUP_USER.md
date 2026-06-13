# 🚀 Cara Setup Hermes Agent Biar Bisa Diakses dari Android App

## 📋 Prasyarat

- Hermes Agent udah terinstall di **Termux**, **VPS**, **Terminus**, atau **laptop**
- Udah bisa `hermes chat` jalan normal

---

## 🎯 Metode 1: Via API Server (Paling Gampang)

### Langkah-langkah

#### 1. Aktifkan API Server
Buka file environment Hermes:
```bash
nano ~/.hermes/.env
```

Tambahin ini:
```env
API_SERVER_ENABLED=true
API_SERVER_KEY=buat-password-rahasia-kamu
API_SERVER_PORT=8642
```

> **Tips:** Ganti `API_SERVER_KEY` dengan password acak yang susah ditebak! Ini kunci akses ke Hermes kamu.

#### 2. Jalankan Gateway
```bash
hermes gateway
```

Kalo berhasil, bakal keluar:
```
[API Server] API server listening on http://0.0.0.0:8642
```

#### 3. Cek IP Perangkat Kamu

**Di Termux (HP):**
```bash
# Cek IP lokal WiFi
ifconfig wlan0 | grep "inet " | awk '{print $2}'

# Atau pake perintah ini kalo ifconfig gak ada
ip addr show wlan0 | grep "inet " | awk '{print $2}'
```

**Di VPS:**
```bash
curl -s ifconfig.me
```

#### 4. Test Pake curl (dari perangkat LAIN)
```bash
curl http://IP-KAMU:8642/v1/chat/completions \
  -H "Authorization: Bearer buat-password-rahasia-kamu" \
  -H "Content-Type: application/json" \
  -d '{"model":"hermes-agent","messages":[{"role":"user","content":"Halo!"}],"stream":false}'
```

Kalo berhasil, Hermes bakal jawab! 🎉

#### 5. Masukin ke Aplikasi Android
Di app **Hermes Hub**:
- **Nama Koneksi:** Bebas (misal "Hermes Termux")
- **Base URL:** `http://IP-KAMU:8642`
- **API Key:** `buat-password-rahasia-kamu`
- Klik **Test Koneksi** → kalo ijo, **Simpan**!

---

## ⚠️ PENTING: Masalah Jaringan

### Kalo di Termux (HP sendiri)
| Masalah | Solusi |
|---------|--------|
| App gak bisa connect | Pastikan HP dan Termux di **WiFi yang sama** |
| Connect tapi lemot | Cek sinyal WiFi |
| "Connection refused" | Pastikan `hermes gateway` masih jalan |

### Kalo di VPS
| Masalah | Solusi |
|---------|--------|
| Gak bisa dari luar | Buka port di firewall: `sudo ufw allow 8642` |
| Masih gak bisa | Cek security group VPS (AWS/GCP/DigitalOcean) |
| Pengen aman | Pasang SSL biar pake HTTPS (opsional) |

---

## 🎯 Metode 2: Via SSH Tunnel

Buat yang lebih advance — Hermes di VPS tanpa perlu buka port publik:

```bash
# Di Termux/HP
ssh -L 8642:localhost:8642 user@vps-ip

# Terus di app masukin:
# URL: http://localhost:8642
```

---

## 🎯 Metode 3: Biarkan Gateway Berjalan di Background

Biar Hermes gak mati pas Termux ditutup:

```bash
# Di Termux, install termux-services dulu
pkg install termux-services

# Terus jalanin gateway sebagai service
hermes gateway install
hermes gateway start
```

Untuk VPS pake systemd:
```bash
hermes gateway install
systemctl --user start hermes-gateway
systemctl --user enable hermes-gateway
```

---

## 🔒 Tips Keamanan

1. **Ganti API_KEY** pake string acak panjang (min 16 karakter)
2. **Jangan share API_KEY** ke orang lain — itu kunci akses penuh ke Hermes kamu
3. Kalo cuma mau dipake di WiFi rumah, gak perlu HTTPS
4. Kalo mau diakses dari internet, pake VPN atau SSH tunnel aja

---

## ❓ Troubleshooting

| Gejala | Penyebab | Solusi |
|--------|----------|--------|
| `401 Unauthorized` | API_KEY salah | Cek `.env`, pastikan cocok |
| `Connection refused` | Gateway gak jalan | Jalankan `hermes gateway` |
| `Timeout` | Firewall ngeblok | Buka port 8642 |
| App tiba-tiba error | Gateway mati | Cek `hermes gateway status` |
