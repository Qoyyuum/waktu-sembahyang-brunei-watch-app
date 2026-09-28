package it.anak.waktusembahyangbrunei.data

import android.content.ComponentName
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import it.anak.waktusembahyangbrunei.complication.MainComplicationService
import it.anak.waktusembahyangbrunei.tile.MainTileService
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "prayer_prefs")

private val ZONE_BRUNEI: ZoneId = ZoneId.of("Asia/Brunei")

fun bruneiNow(): ZonedDateTime = ZonedDateTime.now(ZONE_BRUNEI)

fun bruneiToday(): LocalDate = bruneiNow().toLocalDate()

data class PrayerTimesResult(
    val response: PrayerTimesResponse? = null,
    val stale: Boolean = false,
    val error: String? = null,
)

class PrayerRepository(private val context: Context) {

    private val api = PrayerTimesApi()
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        const val DEFAULT_DISTRICT = "brunei-muara"
        val DISTRICTS = listOf("brunei-muara", "tutong", "belait", "temburong")
        const val LANG_MALAY = "ms"
        const val LANG_ENGLISH = "en"

        private val KEY_DISTRICT = stringPreferencesKey("district")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_TODAY_JSON = stringPreferencesKey("cache_json")
        private val KEY_TODAY_DATE = stringPreferencesKey("cache_date")
        private val KEY_TODAY_DISTRICT = stringPreferencesKey("cache_district")
        private val KEY_TMRW_JSON = stringPreferencesKey("cache_tmrw_json")
        private val KEY_TMRW_DATE = stringPreferencesKey("cache_tmrw_date")
        private val KEY_TMRW_DISTRICT = stringPreferencesKey("cache_tmrw_district")
    }

    val districtFlow: Flow<String> =
        context.dataStore.data.map { it[KEY_DISTRICT] ?: DEFAULT_DISTRICT }

    val languageFlow: Flow<String> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: LANG_MALAY }

    suspend fun setDistrict(district: String) {
        context.dataStore.edit { it[KEY_DISTRICT] = district }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language }
    }

    suspend fun currentDistrict(): String = districtFlow.first()

    suspend fun currentLanguage(): String = languageFlow.first()

    /**
     * Returns prayer times for [date] (defaults to today, Brunei time), serving the cache when it
     * matches and fetching from the API otherwise. Falls back to stale cache on network errors.
     */
    suspend fun getDay(
        district: String? = null,
        date: LocalDate = bruneiToday(),
        forceRefresh: Boolean = false,
    ): PrayerTimesResult {
        val resolvedDistrict = district ?: currentDistrict()
        val isToday = date == bruneiToday()
        val (jsonKey, dateKey, districtKey) =
            if (isToday) {
                Triple(KEY_TODAY_JSON, KEY_TODAY_DATE, KEY_TODAY_DISTRICT)
            } else {
                Triple(KEY_TMRW_JSON, KEY_TMRW_DATE, KEY_TMRW_DISTRICT)
            }

        val prefs = context.dataStore.data.first()
        val cachedJson = prefs[jsonKey]
        val cacheMatches =
            cachedJson != null &&
                prefs[dateKey] == date.toString() &&
                prefs[districtKey] == resolvedDistrict

        if (cacheMatches && !forceRefresh) {
            return PrayerTimesResult(response = parse(cachedJson!!))
        }

        return try {
            val fetched = api.fetch(resolvedDistrict, date)
            context.dataStore.edit { p ->
                p[jsonKey] = json.encodeToString(PrayerTimesResponse.serializer(), fetched)
                p[dateKey] = date.toString()
                p[districtKey] = resolvedDistrict
                // Tomorrow's slot can no longer be trusted if its date has passed.
                if (isToday) {
                    val tmrwDate = p[KEY_TMRW_DATE]
                    if (tmrwDate != null && tmrwDate <= date.toString()) {
                        p.remove(KEY_TMRW_JSON)
                        p.remove(KEY_TMRW_DATE)
                        p.remove(KEY_TMRW_DISTRICT)
                    }
                }
            }
            notifySurfacesChanged()
            PrayerTimesResult(response = fetched)
        } catch (e: IOException) {
            if (cachedJson != null) {
                PrayerTimesResult(response = parse(cachedJson), stale = true, error = e.message)
            } else {
                PrayerTimesResult(error = e.message ?: "Network error")
            }
        }
    }

    suspend fun getTomorrow(district: String? = null): PrayerTimesResult =
        getDay(district = district, date = bruneiToday().plusDays(1))

    suspend fun refresh(): PrayerTimesResult = getDay(forceRefresh = true)

    private fun parse(raw: String): PrayerTimesResponse =
        runCatching { json.decodeFromString<PrayerTimesResponse>(raw) }
            .getOrDefault(PrayerTimesResponse())

    private fun notifySurfacesChanged() {
        TileService.getUpdater(context).requestUpdate(MainTileService::class.java)
        ComplicationDataSourceUpdateRequester.create(
                context,
                ComponentName(context, MainComplicationService::class.java),
            )
            .requestUpdate()
    }
}
