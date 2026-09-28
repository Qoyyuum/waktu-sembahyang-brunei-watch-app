package it.anak.waktusembahyangbrunei.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.anak.waktusembahyangbrunei.data.PrayerRepository
import it.anak.waktusembahyangbrunei.data.PrayerTimesResult
import it.anak.waktusembahyangbrunei.data.ScheduleEntry
import it.anak.waktusembahyangbrunei.data.bruneiNow
import it.anak.waktusembahyangbrunei.domain.NextPrayer
import it.anak.waktusembahyangbrunei.domain.NextPrayerCalculator
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PrayerViewModel(private val repository: PrayerRepository) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val stale: Boolean = false,
        val day: String = "",
        val dateMalay: String = "",
        val hijri: String = "",
        val district: String = PrayerRepository.DEFAULT_DISTRICT,
        val lang: String = PrayerRepository.LANG_MALAY,
        val entries: List<ScheduleEntry> = emptyList(),
        val next: NextPrayer? = null,
    )

    private val result = MutableStateFlow<PrayerTimesResult?>(null)
    private val tomorrowImsak = MutableStateFlow<LocalTime?>(null)
    private val prefs = combine(repository.districtFlow, repository.languageFlow) { d, l -> d to l }
    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(30_000)
        }
    }

    val state: StateFlow<UiState> =
        combine(result, tomorrowImsak, prefs, ticker) { r, imsak, (district, lang), _ ->
            buildState(r, imsak, district, lang)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch {
            repository.districtFlow.drop(1).distinctUntilChanged().collect { load() }
        }
    }

    fun refresh() {
        viewModelScope.launch { load(forceRefresh = true) }
    }

    fun setDistrict(district: String) {
        viewModelScope.launch { repository.setDistrict(district) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    private suspend fun load(forceRefresh: Boolean = false) {
        val r = repository.getDay(forceRefresh = forceRefresh)
        result.value = r
        val schedule = r.response?.data?.schedule ?: return
        val next = NextPrayerCalculator.next(schedule, bruneiNow().toLocalDateTime())
        if (next?.isTomorrow == true && tomorrowImsak.value == null) {
            val tmrw = repository.getTomorrow()
            tomorrowImsak.value =
                NextPrayerCalculator.parseTime(tmrw.response?.data?.timings?.imsak)
        }
    }

    private fun buildState(
        r: PrayerTimesResult?,
        imsak: LocalTime?,
        district: String,
        lang: String,
    ): UiState {
        val data = r?.response?.data
        return UiState(
            loading = r == null,
            error = if (r?.response == null) r?.error else null,
            stale = r?.stale ?: false,
            day = data?.day.orEmpty(),
            dateMalay = data?.dateMalay.orEmpty(),
            hijri = data?.hijri.orEmpty(),
            district = district,
            lang = lang,
            entries = data?.schedule.orEmpty(),
            next =
                data?.schedule?.let {
                    NextPrayerCalculator.next(it, bruneiNow().toLocalDateTime(), imsak)
                },
        )
    }
}
