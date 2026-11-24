# Board Game Buddy — UAS ADSE

Implementasi Bab 1–2 *Spring AI in Action* (Craig Walls). Tidak ada UI, database, deployment, atau video. Upload GitHub/Teams dilakukan manual.

## Hasil verifikasi
- `clean test bootJar`: berhasil; **10 test, 0 failure/error**.
- Aplikasi JAR berjalan dan menerima POST /ask untuk Bab 1 dan Bab 2, dengan mock LLM.
- **Belum diuji dengan AI nyata**: tidak ada OPENAI_API_KEY pada lingkungan saat pengerjaan. Hasil mock bukan bukti akurasi model.
- Azure/Ollama: konfigurasi tersedia, belum diverifikasi dengan layanan nyata.

## Prasyarat
Java **23** sesuai build.gradle dan koneksi internet saat unduh Gradle/dependency. Gradle Wrapper disertakan; tidak perlu Gradle global.

## Test tanpa API key (Windows PowerShell)
```powershell
.\gradlew.bat test
.\gradlew.bat bootJar
```
Linux/Git Bash: `bash gradlew test`. HTML test: `build/reports/tests/test/index.html`.

## Jalankan OpenAI (menggunakan quota)
```powershell
$env:OPENAI_API_KEY = 'ISI_KEY_SENDIRI'
$env:OPENAI_MODEL = 'gpt-4o-mini'
.\gradlew.bat bootRun
```
Jendela PowerShell lain:
```powershell
Invoke-RestMethod http://127.0.0.1:8080/ask -Method Post -ContentType 'application/json' -Body '{"question":"How many dice are used in Yahtzee?"}'
```
Bab 2, hentikan proses sebelumnya lalu:
```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=chapter2'
```
Pengujian kualitas dengan provider nyata: ` .\gradlew.bat liveTest`. Dua live test sengaja dipisahkan dari test default, bukan hasil yang sudah lulus.

## Alternatif provider dalam Bab 1
Ollama (perlu instalasi Ollama dan `ollama pull llama3.2`):
```powershell
.\gradlew.bat -Pprovider=ollama bootRun --args='--spring.profiles.active=ollama'
```
Azure (perlu deployment dan credential sendiri):
```powershell
$env:AZURE_OPENAI_API_KEY = 'ISI_KEY_SENDIRI'
$env:AZURE_OPENAI_ENDPOINT = 'https://RESOURCE.openai.azure.com/'
$env:AZURE_OPENAI_DEPLOYMENT = 'NAMA_DEPLOYMENT'
.\gradlew.bat -Pprovider=azure bootRun --args='--spring.profiles.active=azure'
```
Gabungkan dengan Bab 2 melalui `--spring.profiles.active=ollama,chapter2` atau `azure,chapter2`. Pilihan dependency provider adalah build-time; JAR default hanya berisi OpenAI. Jangan mengklaim alternatif sudah berjalan sebelum dicoba.

## Berkas pengumpulan
- [Laporan Word sesuai template, dimulai dari Bab 1](docs/LAPORAN_UAS_ADSE.docx) — berkas utama untuk Teams; isi identitas sebelum upload.
- [Catatan praktikum ringkas](docs/LAPORAN.md)
- [UML](docs/UML.md)
- `docs/evidence/`: log build, XML JUnit, hasil HTTP dan log startup.
- `submission/UAS_ADSE_Teams.zip`: paket source, laporan, bukti dan JAR yang disiapkan lokal. Folder ini tidak masuk Git.

## Upload manual
**GitHub:** upload source, Gradle Wrapper, README, dan docs. Jangan upload `.gradle/`, `build/`, `submission/`, API key, atau PDF buku/soal. `.gitignore` disiapkan. Jika memakai Git CLI, buat repository sendiri lalu periksa `git status` sebelum commit/push. Belum dibuat commit/repository oleh asisten.

**Teams:** isi identitas di `docs/LAPORAN_UAS_ADSE.docx` dahulu; lalu unggah ZIP di `submission/`. Jika mengubah laporan, buat ulang ZIP agar isinya sinkron. File DOCX sudah bisa dibuka dan disunting di Microsoft Word.

Video sengaja tidak disertakan sesuai instruksi Anda; ini tidak menghapus kewajiban video pada rubrik asli. Screenshot belum dibuat; bukti yang disertakan adalah log dan hasil otomatis, bukan pengganti yang diklaim setara.

## Referensi
- PDF soal dan ebook milik pengguna (tidak didistribusikan ulang).
- https://docs.spring.io/spring-ai/reference/1.0/api/chatclient.html
- Source evaluator versi 1.0.1: https://repo.maven.apache.org/maven2/org/springframework/ai/spring-ai-client-chat/1.0.1/spring-ai-client-chat-1.0.1-sources.jar
