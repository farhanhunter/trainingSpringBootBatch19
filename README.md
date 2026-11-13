# Spring Boot application template

## Purpose

The purpose of this template is to speed up the creation of new Spring applications within HMCTS
and help keep the same standards across multiple teams. If you need to create a new app, you can
simply use this one as a starting point and build on top of it.

## What's inside

The template is a working application with a minimal setup. It contains:
 * application skeleton
 * setup script to prepare project
 * common plugins and libraries
 * [HMCTS Java plugin](https://github.com/hmcts/gradle-java-plugin)
 * docker setup
 * automatically publishes API documentation to [hmcts/cnp-api-docs](https://github.com/hmcts/cnp-api-docs)
 * code quality tools already set up
 * MIT license and contribution information
 * Helm chart using chart-java.

The application exposes health endpoint (http://localhost:4550/health) and metrics endpoint
(http://localhost:4550/metrics).

## Plugins

The template contains the following plugins:

  * HMCTS Java plugin

    Applies code analysis tools with HMCTS default settings. See the [project repository](https://github.com/hmcts/gradle-java-plugin) for details.

    Analysis tools include:

    * checkstyle

        https://docs.gradle.org/current/userguide/checkstyle_plugin.html

        Performs code style checks on Java source files using Checkstyle and generates reports from these checks.
        The checks are included in gradle's *check* task (you can run them by executing `./gradlew check` command).

    * org.owasp.dependencycheck

        https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html

        Provides monitoring of the project's dependent libraries and creating a report
        of known vulnerable components that are included in the build. To run it
        execute `gradle dependencyCheck` command.

  * jacoco

    https://docs.gradle.org/current/userguide/jacoco_plugin.html

    Provides code coverage metrics for Java code via integration with JaCoCo.
    You can create the report by running the following command:

    ```bash
      ./gradlew jacocoTestReport
    ```

    The report will be created in build/reports subdirectory in your project directory.

  * io.spring.dependency-management

    https://github.com/spring-gradle-plugins/dependency-management-plugin

    Provides Maven-like dependency management. Allows you to declare dependency management
    using `dependency 'groupId:artifactId:version'`
    or `dependency group:'group', name:'name', version:version'`.

  * org.springframework.boot

    http://projects.spring.io/spring-boot/

    Reduces the amount of work needed to create a Spring application


  * com.github.ben-manes.versions

    https://github.com/ben-manes/gradle-versions-plugin

    Provides a task to determine which dependencies have updates. Usage:

    ```bash
      ./gradlew dependencyUpdates -Drevision=release
    ```

## Setup

Located in `./bin/init.sh`. Simply run and follow the explanation how to execute it.

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Note: Docker Compose V2 is highly recommended for building and running the application.
In the Compose V2 old `docker-compose` command is replaced with `docker compose`.

Create docker image:

```bash
  docker compose build
```

Run the distribution (created in `build/install/spring-boot-template` directory)
by executing the following command:

```bash
  docker compose up
```

This will start the API container exposing the application's port
(set to `4550` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4550/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

# Final Project — Product Platform

Bangun platform produk end-to-end dari materi Day 1–6: login JWT, gateway, CRUD produk, dan impor otomatis dari DummyJSON. Kerjakan semua requirement **wajib** dulu. Bonus baru dihitung kalau semua wajib sudah jalan.

---

## Aturan main

| Boleh | Tidak boleh |
| --- | --- |
| Bertanya ke AI lewat aplikasi chat untuk memahami konsep atau arti error | Memakai **AI agent** yang menulis atau mengubah file project (Claude Code, Cursor Agent, Copilot Agent Mode, dan sejenisnya) |
| Membuka slide, catatan kelas, repo latihan sendiri, dan dokumentasi | Menyalin project teman atau repo orang lain |
| Diskusi konsep dengan teman | Mengumpulkan kode yang tidak bisa dijelaskan |

---

## Arsitektur

```
Postman
   │
   ▼
api-gateway :8080 ── cek JWT, route lb://
   │                  │                        │
   ▼                  ▼                        ▼
auth-service :8081   product-service :8082 ◄─Feign─ scheduler-service :8083 ──Feign──► DummyJSON
   │                  │   (cek duplikat + aturan bisnis)
   └────────┬─────────┘
            ▼
     PostgreSQL :5432 (Docker)

Semua service terdaftar di eureka-server :8761
scheduler-service TIDAK mengakses database: data dikirim ke product-service lewat Feign
```

| Service | Port | Tanggung jawab |
| --- | --- | --- |
| `eureka-server` | 8761 | Service registry |
| `api-gateway` | 8080 | Satu pintu masuk, verifikasi JWT |
| `auth-service` | 8081 | Register, login, membuat JWT |
| `product-service` | 8082 | CRUD produk, aturan bisnis, cek duplikat |
| `scheduler-service` | 8083 | Impor awal + terjadwal dari DummyJSON, lalu kirim ke product-service |

Nama package, nama tabel, dan nama class bebas, asalkan konsisten dan bisa dijelaskan.

### Spesifikasi impor DummyJSON

| Item | Nilai |
| --- | --- |
| Kategori | `smartphones` (`GET https://dummyjson.com/products/category/smartphones`) |
| Field tambahan wajib | `brand` |
| Aturan bisnis | Produk dengan `rating` < 3 **tidak diimpor** (dicatat di log) |
| Jadwal | Setiap 2 menit (`0 */2 * * * *`), maksimal 5 produk per putaran |
| Hasil akhir yang benar | **14 dari 16** produk smartphones tersimpan |

---

## Requirement wajib (syarat lulus)

### W1 — auth-service
| # | Kriteria lulus |
| --- | --- |
| W1.1 | Register menyimpan user baru. Password di database berupa hash BCrypt, bukan teks asli |
| W1.2 | Username yang sama tidak bisa didaftarkan dua kali (status 4xx, format Base Response) |
| W1.3 | Login benar → mendapat JWT berisi `sub` dan `exp` (umur token kalian tentukan sendiri, tulis alasannya di Catatan Keputusan) |
| W1.4 | Login salah → 401 dengan pesan yang **sama** untuk username salah maupun password salah |
| W1.5 | Payload JWT tidak berisi password atau data sensitif lain |

### W2 — product-service: CRUD
| # | Kriteria lulus |
| --- | --- |
| W2.1 | Produk punya minimal: id, nama, harga, kategori, dan `brand` |
| W2.2 | Buat produk → **201**, data tersimpan di PostgreSQL |
| W2.3 | Detail produk per id → 200. Id tidak ada → **404** |
| W2.4 | Daftar produk dengan filter nama mengandung huruf tertentu (`?huruf=`), tidak peka huruf besar/kecil |
| W2.5 | Ubah produk per id → 200. Id tidak ada → 404 |
| W2.6 | Hapus produk per id → 200. Id tidak ada → 404 |
| W2.7 | Aturan bisnis diterapkan di service, bukan di controller |
| W2.8 | Response tidak mengembalikan Entity langsung (pakai DTO) |
| W2.9 | Pola Controller → Service (interface + impl) → Repository, dependency lewat constructor |

### W3 — Validasi & error handling
| # | Kriteria lulus |
| --- | --- |
| W3.1 | Nama produk kosong atau harga tidak valid → **400**, pesan jelas field mana yang salah |
| W3.2 | Semua error (400, 401, 404, 503) memakai format Base Response yang **sama** dengan response sukses |
| W3.3 | Tidak ada `try-catch` di controller. Semua error ditangani satu Global Exception Handler per service |
| W3.4 | Minimal 2 custom exception yang namanya sesuai domain produk |

### W4 — Eureka + Gateway
| # | Kriteria lulus |
| --- | --- |
| W4.1 | Kelima service tampil **UP** di dashboard Eureka |
| W4.2 | Gateway meneruskan request dengan `lb://`, tanpa IP atau port tertulis di route |
| W4.3 | Endpoint auth bisa diakses tanpa token. Endpoint produk tanpa token → **401 dari Gateway** (bukan dari product-service) |
| W4.4 | Token palsu atau sudah kedaluwarsa → 401 |
| W4.5 | product-service menerima identitas user dari Gateway lewat header, dan mencatatnya di log saat produk dibuat |

### W5 — scheduler-service: impor dari DummyJSON (Scheduler + Feign + Eureka)
| # | Kriteria lulus |
| --- | --- |
| W5.1 | `scheduler-service` (port 8083) terdaftar di Eureka dan mengambil produk kategori `smartphones` dari DummyJSON lewat Feign |
| W5.2 | Saat start, `scheduler-service` langsung melakukan **impor awal** tanpa menunggu jadwal pertama. Setelah itu berjalan setiap 2 menit |
| W5.3 | `scheduler-service` **tidak mengakses database produk**. Data dikirim ke product-service lewat Feign memakai nama service di Eureka, tanpa URL atau port |
| W5.4 | product-service menyediakan endpoint internal untuk menerima data impor. Aturan bisnis (rating < 3 tidak diimpor) dan pengecekan duplikat ada di product-service |
| W5.5 | Produk yang sudah pernah diimpor **tidak tersimpan dua kali** walaupun impor jalan berulang (cara mendeteksinya ditulis di Catatan Keputusan) |
| W5.6 | Kalau DummyJSON atau product-service tidak bisa dihubungi, `scheduler-service` **tidak mati**: error dicatat di log dan putaran berikutnya tetap jalan |
| W5.7 | Endpoint internal product-service **ditolak** kalau dipanggil dari luar lewat Gateway. Impor manual dipicu lewat Gateway dengan JWT |

### W6 — Git & README
| # | Kriteria lulus |
| --- | --- |
| W6.1 | `.gitignore` benar: tidak ada folder build, file IDE, atau secret yang ter-commit |
| W6.2 | Secret JWT dan password database **tidak tertulis langsung** di file yang di-commit (pakai environment variable dengan nilai default untuk lokal, atau file yang di-ignore) |
| W6.3 | Minimal **8 commit** dengan pesan jelas, tersebar selama sesi coding (bukan 1 commit raksasa di akhir) |
| W6.4 | README berisi cara menjalankan, urutan start service, perintah Docker, dan **Catatan Keputusan** (minimal 4 poin, bahasa sendiri) |

---

## Bonus (plus value, +3 per fitur, maksimal +15)

Bonus hanya dihitung kalau **W1–W6 sudah jalan semua**, dan harus bisa dijelaskan saat QnA.

### B1 — Redis cache (+3)
| # | Kriteria |
| --- | --- |
| B1.1 | Detail produk per id disimpan di Redis dengan prefix key yang jelas dan TTL |
| B1.2 | Panggilan kedua untuk id yang sama tidak menjalankan query database (dibuktikan lewat log) |
| B1.3 | Ubah atau hapus produk membersihkan cache-nya, sehingga tidak ada data basi |

### B2 — Kafka event (+3)
| # | Kriteria |
| --- | --- |
| B2.1 | Setiap produk baru (manual maupun hasil impor) mengirim event ke topic `product-created` |
| B2.2 | Ada consumer yang memproses event itu dan menghasilkan sesuatu yang bisa dilihat (statistik, log ringkasan, atau data di Redis) |
| B2.3 | Saat consumer dimatikan lalu dinyalakan lagi, event yang tertunda tetap diproses |

### B3 — Laporan CSV ke MinIO (+3)
| # | Kriteria |
| --- | --- |
| B3.1 | Laporan CSV berisi produk yang masuk dalam periode tertentu dibuat dan di-upload ke bucket MinIO |
| B3.2 | Laporan bisa dipicu scheduler dan juga manual lewat endpoint |
| B3.3 | Link download memakai presigned URL yang ada masa berlakunya |

### B4 — Feign ke DummyJSON untuk tulis data (+3)
| # | Kriteria |
| --- | --- |
| B4.1 | Saat produk dibuat, diubah, atau dihapus di aplikasi kita, perubahan yang sama juga dikirim ke DummyJSON (`POST /products/add`, `PUT /products/{id}`, `DELETE /products/{id}`) |
| B4.2 | Kegagalan DummyJSON tidak membatalkan simpan di database kita. Keputusan ini dijelaskan di Catatan Keputusan |
| B4.3 | Response DummyJSON dicatat di log (DummyJSON hanya mensimulasikan, datanya tidak benar-benar berubah) |

### B5 — AOP logging durasi (+3)
| # | Kriteria |
| --- | --- |
| B5.1 | Satu aspect mencatat nama method, argumen, dan durasi semua method di layer service |
| B5.2 | Data sensitif (password) **tidak** tercetak di log |
| B5.3 | Exception dari service tercatat sebelum ditangani Global Exception Handler |

---

## Kontrak endpoint (lewat Gateway :8080)

| Method | Endpoint | Auth | Keterangan | Wajib/Bonus |
| --- | --- | --- | --- | --- |
| POST | `/auth/register` | Publik | Daftar user baru | W1 |
| POST | `/auth/login` | Publik | Login, dapat JWT | W1 |
| POST | `/product` | JWT | Buat produk → 201 | W2 |
| GET | `/product/{id}` | JWT | Detail produk | W2 |
| GET | `/product?huruf=a` | JWT | Daftar + filter nama | W2 |
| PUT | `/product/{id}` | JWT | Ubah produk | W2 |
| DELETE | `/product/{id}` | JWT | Hapus produk | W2 |
| POST | `/scheduler/import` | JWT | Picu impor manual (diteruskan Gateway ke scheduler-service) | W5 |
| POST | endpoint internal product-service (nama bebas) | **Internal saja, ditolak Gateway** | Menerima data impor dari scheduler-service | W5 |
| POST | `/product/report` | JWT | Buat laporan CSV | B3 |

Kalau service memakai context-path, prefix di Gateway dan `pathMatchers` harus disesuaikan. Jelaskan pilihan kalian di Catatan Keputusan.

### Format Base Response (semua response, termasuk error)

```json
{ "status": "success", "message": "Produk berhasil dibuat", "data": { "...": "..." } }
```

```json
{ "status": "failed", "message": "Nama produk wajib diisi", "data": null }
```

Untuk endpoint daftar, boleh menambahkan `metadata` berisi `page`, `size`, dan `total`. Ini tidak wajib.

---

## Alur demo (urutan wajib, 10 menit demo + 5 menit QnA)

1. Dashboard Eureka: kelima service **UP**
2. Register → login → salin token
3. `POST /product` **tanpa** token → 401 dari Gateway
4. `POST /product` dengan token → 201, lalu tunjukkan datanya di DBeaver
5. `POST /product` dengan nama kosong → 400 format Base Response
6. `GET /product/{id}` → data tampil (cache hit di log kalau mengerjakan B1)
7. Impor DummyJSON: tunjukkan log impor awal di scheduler-service dan log penerimaan di product-service, produk smartphones bertambah (14 dari 16), lalu picu sekali lagi dan buktikan tidak ada data dobel. Terakhir, panggil endpoint internal lewat Gateway dan tunjukkan bahwa ditolak
8. Fitur bonus yang dikerjakan

Siapkan Postman collection yang sudah berurutan, supaya waktu demo tidak habis untuk mengetik request.

---

## Rubrik (100 poin)

| Aspek | Poin | Yang dinilai |
| --- | --- | --- |
| Fungsionalitas wajib | 35 | W1–W5 jalan sesuai kriteria |
| Struktur kode | 15 | Controller–Service–Repository, DTO, constructor injection |
| Error handling & validasi | 10 | Global handler, status code tepat, Base Response konsisten |
| Security | 10 | BCrypt, JWT dicek di Gateway, secret tidak di repo |
| Bonus | 15 | +3 per fitur yang benar-benar jalan dan bisa dijelaskan |
| Presentasi & QnA | 15 | Bisa menjelaskan **kenapa**, bukan hanya apa |

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

