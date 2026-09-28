package it.anak.waktusembahyangbrunei.data

import java.io.IOException
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

private const val BASE_URL = "https://waktu.co.bn/api.php"

class PrayerTimesApi(
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetch(district: String, date: LocalDate): PrayerTimesResponse =
        withContext(Dispatchers.IO) {
            val request =
                Request.Builder()
                    .url("$BASE_URL?district=$district&date=$date&format=24h")
                    .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                val body = response.body?.string() ?: throw IOException("Empty response body")
                val parsed = json.decodeFromString<PrayerTimesResponse>(body)
                if (parsed.status != "success") throw IOException("API status: ${parsed.status}")
                parsed
            }
        }
}
