# Envanter Yönetim Sistemi

Laboratuvar, ofis veya atölye gibi ortamlarda **ekipmanların takibi**, **zaman bazlı rezervasyonu**
ve **değişiklik geçmişinin raporlanması** için geliştirilmiş tam yığın (full-stack) bir web uygulaması.

## Özellikler

| Özellik | Açıklama |
|---|---|
| Kayıt / Giriş | JWT tabanlı kimlik doğrulama, BCrypt ile şifrelenmiş parolalar |
| Rol yönetimi | `USER` ve `ADMIN` rolleri; yetkiler backend'de kontrol edilir |
| Ekipman yönetimi | Ekleme, düzenleme, silme (soft delete), arama, kalibrasyon tarihi uyarısı |
| Rezervasyon takvimi | Aylık / haftalık / günlük takvim, sürükleyerek saat seçme, çakışma kontrolü |
| Rezervasyon iptali | Kullanıcı kendi rezervasyonunu, yönetici tüm rezervasyonları iptal edebilir |
| Değişiklik kaydı (audit log) | Her ekleme / güncelleme / silme işlemi kim tarafından, ne zaman yapıldığıyla kaydedilir; güncellemelerde alan bazında eski → yeni değer tutulur |
| Raporlar | İşlem özetleri, değişiklik geçmişi, filtrelenebilir rezervasyon listesi |
| Kullanıcı yönetimi | Yönetici, diğer kullanıcıların rolünü değiştirebilir |

## Kullanılan Teknolojiler

**Backend:** Java 17, Spring Boot 3.5, Spring Web, Spring Data JPA (Hibernate), Spring Security, JWT (jjwt), Bean Validation
**Veritabanı:** PostgreSQL (testlerde H2)
**Frontend:** React 18, Vite, React Router, React Bootstrap, Axios, FullCalendar
**Test:** JUnit 5, Mockito, Spring Boot Test (MockMvc)

## Mimari

```
┌──────────────┐   HTTP + JSON (JWT)   ┌───────────────────────────────────────────┐      ┌────────────┐
│  React (SPA) │ ────────────────────▶ │ Controller → Service → Repository (JPA)   │ ───▶ │ PostgreSQL │
└──────────────┘                       │      ▲ Security Filter (JWT doğrulama)    │      └────────────┘
                                       └───────────────────────────────────────────┘
```

Backend katmanlı mimariyle yazılmıştır:

- **controller** — HTTP isteklerini karşılar, sadece servisleri çağırır
- **service** — İş kuralları (çakışma kontrolü, yetki, audit log)
- **repository** — Spring Data JPA arayüzleri
- **model** — Veritabanı tabloları (entity)
- **dto** — API'nin dışarıya açtığı istek/cevap modelleri (entity'ler doğrudan dışarı verilmez)
- **security** — JWT üretimi ve her istekte doğrulanması
- **exception** — Tüm hataları tek formatta (`{ status, message, fieldErrors }`) döndüren merkezi hata yönetimi

### Veritabanı tabloları

| Tablo | İçerik |
|---|---|
| `users` | Kullanıcılar (ad, soyad, email, telefon, şifre hash'i, rol) |
| `equipment` | Ekipmanlar (`active = false` ise silinmiş sayılır) |
| `reservations` | Ekipman + kullanıcı + başlangıç/bitiş zamanı |
| `audit_logs` | Ekipman üzerindeki işlemler (CREATE / UPDATE / DELETE) |
| `audit_log_changes` | Güncelleme işlemlerinde değişen alanlar (eski / yeni değer) |

Tablolar uygulama ilk açıldığında Hibernate tarafından otomatik oluşturulur.

## Proje Yapısı

```
├── backend/                      Spring Boot uygulaması
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/inventory/
│       │   ├── config/           Güvenlik, CORS, ilk yönetici hesabı
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── model/
│       │   ├── dto/
│       │   ├── security/
│       │   └── exception/
│       └── test/                 Birim ve entegrasyon testleri
├── frontend/                     React uygulaması
│   └── src/
│       ├── api/                  Axios istemcisi (token ekleme, hata mesajları)
│       ├── context/              Oturum ve bildirim yönetimi
│       ├── components/           Ortak bileşenler (layout, formlar, korumalı rota)
│       ├── pages/                Sayfalar
│       ├── styles/
│       └── utils/
└── docker-compose.yml            PostgreSQL (isteğe bağlı)
```

## Kurulum ve Çalıştırma

### Gereksinimler

- Java 17+
- Node.js 18+
- PostgreSQL 14+ (veya Docker)

### 1. Veritabanı

**Seçenek A — Docker ile:**

```bash
docker compose up -d
```

**Seçenek B — Kurulu PostgreSQL ile:** `inventory_db` adında boş bir veritabanı oluşturun:

```sql
CREATE DATABASE inventory_db;
```

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

Backend `http://localhost:8080` adresinde çalışır.

Çalıştırmadan önce `backend` klasöründe bir `.env` dosyası oluşturup veritabanı bilgilerini yazın.
Bu dosya git'e gönderilmez:

```properties
# backend/.env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=inventory_db
DB_USERNAME=postgres
DB_PASSWORD=kendi-sifreniz
```

> Docker ile çalıştırıyorsanız kullanıcı adı ve şifre `docker-compose.yml` içindeki gibi `postgres / postgres` olmalıdır.

`.env` dosyasında (veya ortam değişkeni olarak) kullanılabilecek değerler:

| Değişken | Zorunlu | Açıklama |
|---|---|---|
| `DB_HOST` | Evet | Veritabanı sunucusu (ör. `localhost`) |
| `DB_PORT` | Evet | Veritabanı portu (ör. `5432`) |
| `DB_NAME` | Evet | Veritabanı adı (ör. `inventory_db`) |
| `DB_USERNAME` | Evet | Veritabanı kullanıcısı |
| `DB_PASSWORD` | Evet | Veritabanı şifresi |
| `JWT_SECRET` | Hayır (geliştirme anahtarı) | Base64, en az 256 bit |
| `ADMIN_EMAIL` | Hayır (`admin@envanter.com`) | İlk yönetici hesabı |
| `ADMIN_PASSWORD` | Hayır (`Admin123!`) | İlk yönetici şifresi |

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Uygulama `http://localhost:5173` adresinde açılır. Geliştirme sunucusu `/api` isteklerini
otomatik olarak backend'e yönlendirir.

### 4. Giriş

Uygulama ilk açıldığında sistemde hiç yönetici yoksa otomatik olarak bir yönetici hesabı oluşturulur:

- **Email:** `admin@envanter.com`
- **Şifre:** `Admin123!`

Kayıt sayfasından oluşturulan hesaplar `USER` rolüyle başlar. Yönetici, **Kullanıcılar** sayfasından
başka bir kullanıcıyı yönetici yapabilir.

## Testler

```bash
cd backend
./mvnw test
```

Testler PostgreSQL gerektirmez, bellek içi H2 veritabanı kullanır:

- `ReservationServiceTest` — tarih kontrolleri, çakışma kontrolü, iptal yetkisi
- `EquipmentServiceDiffTest` — güncellemede değişen alanların tespiti
- `ApiIntegrationTest` — kayıt, giriş, yetkisiz erişim (401/403), doğrulama hataları, audit log

## API Özeti

| Metot | Adres | Yetki | Açıklama |
|---|---|---|---|
| POST | `/api/auth/register` | Herkes | Kayıt ol |
| POST | `/api/auth/login` | Herkes | Giriş yap, JWT al |
| GET | `/api/auth/me` | Giriş yapmış | Oturumdaki kullanıcı |
| GET | `/api/equipment` | Giriş yapmış | Ekipman listesi |
| GET | `/api/equipment/{id}` | Giriş yapmış | Ekipman detayı |
| POST | `/api/equipment` | ADMIN | Ekipman ekle |
| PUT | `/api/equipment/{id}` | ADMIN | Ekipman güncelle |
| DELETE | `/api/equipment/{id}` | ADMIN | Ekipman sil |
| GET | `/api/reservations` | Giriş yapmış | Rezervasyon listesi |
| POST | `/api/reservations` | Giriş yapmış | Rezervasyon oluştur |
| DELETE | `/api/reservations/{id}` | Sahibi / ADMIN | Rezervasyon iptal et |
| GET | `/api/users` | ADMIN | Kullanıcı listesi |
| PATCH | `/api/users/{id}/role` | ADMIN | Rol değiştir |
| GET | `/api/audit-logs?action=UPDATE` | ADMIN | Değişiklik geçmişi |

Korumalı adreslere istek atarken `Authorization: Bearer <token>` başlığı gönderilmelidir.
