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

