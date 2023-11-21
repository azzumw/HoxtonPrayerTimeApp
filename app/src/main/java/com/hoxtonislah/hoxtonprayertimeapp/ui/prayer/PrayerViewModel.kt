package com.hoxtonislah.hoxtonprayertimeapp.ui.prayer

import android.text.Html
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.hoxtonislah.hoxtonprayertimeapp.models.FireStoreWeekModel
import com.hoxtonislah.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.hoxtonislah.hoxtonprayertimeapp.models.convertTo12hour
import com.hoxtonislah.hoxtonprayertimeapp.repository.Repository
import com.hoxtonislah.hoxtonprayertimeapp.utils.fromLocalTimeToString
import com.hoxtonislah.hoxtonprayertimeapp.utils.fromStringToLocalTime
import com.hoxtonislah.hoxtonprayertimeapp.utils.getCurrentGregorianDate
import com.hoxtonislah.hoxtonprayertimeapp.utils.getCurrentIslamicDate
import com.hoxtonislah.hoxtonprayertimeapp.utils.getTodayDate
import com.hoxtonislah.hoxtonprayertimeapp.utils.isTodayFriday
import com.hoxtonislah.hoxtonprayertimeapp.BuildConfig
import com.hoxtonislah.hoxtonprayertimeapp.utils.isTodayWeekend
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

enum class ApiStatus {
    LOADING, ERROR, DONE
}

class PrayerViewModel(private val repository: Repository) : ViewModel() {

    private val nextPrayersMap = mutableMapOf<String, LocalTime?>()

    private val _nextJamaat = MutableLiveData<String>()
    val nextJamaat: LiveData<String> get() = _nextJamaat

    val nextJamaatLabelVisibility: LiveData<Boolean> = nextJamaat.map() {
        it != GOOD_NIGHT_MSG
    }

    private val _gregoryTodayDate = MutableLiveData(getCurrentGregorianDate())
    val gregoryTodayDate: LiveData<String> get() = _gregoryTodayDate

    private val _islamicTodayDate = MutableLiveData(getCurrentIslamicDate())
    val islamicTodayDate: LiveData<String> get() = _islamicTodayDate

    val fireStoreWeekModel: LiveData<FireStoreWeekModel?> = repository.fireStoreWeekModel

    val isTodayFriday: Boolean = isTodayFriday()

    val fajarJamaah12hour: LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.fajar)
    }

    val dhuhrJamaah12hour: LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.dhuhr)
    }

    val firstJummahJamaah12hour: LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.firstJummah)
    }

    val secondJummahJamaah12hour: LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.secondJummah)
    }

    val asrJamaah12hour: LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.asr)
    }

    val ishaJamaah12hour: LiveData<String?> = fireStoreWeekModel.map {
        if (isTodayWeekend() && it?.weekendIsha != null) {
            it.to12hour(it.weekendIsha)
        } else it?.to12hour(it.isha)
    }

    private val prayerBeginTimesFromLocal = repository.todayPrayerBeginTimesFromLocal

    val prayerBeginTimesIn12HourFormat: LiveData<LondonPrayersBeginningTimes?> =
        prayerBeginTimesFromLocal.map {
            it?.convertTo12hour()
        }

    private val _remoteApiStatus = MutableLiveData<ApiStatus>()
    private val remoteApiStatus: LiveData<ApiStatus>
        get() = _remoteApiStatus

    val apiStatusLiveMerger = MediatorLiveData<ApiStatus>()

    //To highlight the 'Next Prayer' background
    val fajrListItemBackground: LiveData<Boolean> = nextJamaat.map {
        it.substringBefore(" ") == FAJR_KEY
    }

    val dhuhrListItemBackground: LiveData<Boolean> = nextJamaat.map {
        when {
            it.contains(DHUHR_KEY) || it.contains(JUMUAH_TEXT) -> true
            else -> false
        }
    }

    val asrListItemBackground: LiveData<Boolean> = nextJamaat.map {
        it.substringBefore(" ") == ASR_KEY
    }

    val maghribListItemBackground: LiveData<Boolean> = nextJamaat.map {
        it.substringBefore(" ") == MAGHRIB_KEY
    }

    val ishaListItemBackground: LiveData<Boolean> = nextJamaat.map {
        it.substringBefore(" ") == ISHA_KEY
    }

    init {
        var count = 1

        apiStatusLiveMerger.addSource(prayerBeginTimesFromLocal) { local ->
            if (local != null) {
                getJamaahTimesFromCloud(local.magribJamaah)
                apiStatusLiveMerger.value = ApiStatus.DONE
            } else {

                count++

                getPrayerBeginTimesFromRemote()

                apiStatusLiveMerger.addSource(remoteApiStatus) { status ->
                    when (status) {
                        ApiStatus.LOADING -> {
                            apiStatusLiveMerger.value = ApiStatus.LOADING
                        }

                        ApiStatus.ERROR -> {
                            apiStatusLiveMerger.value = ApiStatus.ERROR
                        }

                        else -> {
                            apiStatusLiveMerger.value = ApiStatus.DONE
                        }

                    }

                    if (count > 5) {
                        apiStatusLiveMerger.removeSource(remoteApiStatus)
                    }
                }
            }

            if (count > 5) {
                apiStatusLiveMerger.removeSource(prayerBeginTimesFromLocal)
            }
        }

        if (BuildConfig.DEBUG) {
            repository.writeJamaahTimesToCloud()
        }
    }


    private fun getPrayerBeginTimesFromRemote(todayLocalDate: LocalDate = LocalDate.now()) {
        _remoteApiStatus.value = ApiStatus.LOADING
        Timber.e("Im inside network method")

        viewModelScope.launch {

            try {

                val apiResult = repository.getPrayerBeginningTimesFromLondonApi(todayLocalDate)

                _remoteApiStatus.value = ApiStatus.DONE

                deleteYesterdayPrayersFromLocal()

                insertTodayPrayersIntoLocal(apiResult)

                val mjt = apiResult.getMaghribJamaahTime()

                repository.updateMaghribJamaahTime(
                    maghribJamaahTime = mjt,
                    todayLocalDate = getTodayDate(todayLocalDate)
                )
            } catch (dateTimeException: DateTimeParseException) {
                if (BuildConfig.DEBUG) {
                    Timber.e("Date parsing exception ${dateTimeException.message}")
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Timber.e("Network exception ${e.message}")
                }
                _remoteApiStatus.value = ApiStatus.ERROR
                Timber.e("Network exception ApiStatus set to ERROR")
            }
        }
    }

    private fun getJamaahTimesFromCloud(maghribJamaahTime: String? = null) {
        repository.getJamaahTimesFromFireStore {
            workoutNextJamaah(maghribJamaahTime)
        }
    }

    private fun insertTodayPrayersIntoLocal(resultFromNetwork: LondonPrayersBeginningTimes) {
        viewModelScope.launch {
            repository.insertTodayPrayer(resultFromNetwork)
        }
    }

    private fun deleteYesterdayPrayersFromLocal() {
        viewModelScope.launch {
            repository.deleteYesterdayPrayer()
        }
    }


    /**
    This method weeds out those prayer times before the current time,
    If the list is empty i.e. all prayers have been filtered out, then a Good Night
    message is displayed. At midnight, this cycle repeats.
     */
    private fun workoutNextJamaah(maghribJamaahTime: String? = null) {
        //get the current time

        val tempPairNextJammah =
            addJamaahTimesToMapReturnsChronologicallySortedList(maghribJamaahTime).firstOrNull {
                LocalTime.now().isBefore(it.second)
            }

        _nextJamaat.value = tempPairNextJammah?.let {
            when (it.first) {
                FIRST_JUMUAH_KEY -> {
                    "${
                        Html.fromHtml(
                            FIRST_SUPERSCRIPT,
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } ${it.first.substringAfter(" ")} ${
                        fromLocalTimeToString(it.second)
                    }"
                }

                SECOND_JUMUAH_KEY -> {
                    "${
                        Html.fromHtml(
                            SECOND_SUPERSCRIPT,
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } ${it.first.substringAfter(" ")} ${
                        fromLocalTimeToString(
                            it.second
                        )
                    }"
                }

                else -> "${it.first} ${fromLocalTimeToString(it.second)}"
            }

        } ?: GOOD_NIGHT_MSG
    }

    private fun addJamaahTimesToMapReturnsChronologicallySortedList(magribJamaahTime: String?): List<Pair<String, LocalTime?>> {
        return nextPrayersMap.also {

            it[FAJR_KEY] = fromStringToLocalTime(fireStoreWeekModel.value?.fajar)

            if (isTodayFriday()) {
                it[FIRST_JUMUAH_KEY] =
                    fromStringToLocalTime(fireStoreWeekModel.value?.firstJummah)
                if (fireStoreWeekModel.value?.secondJummah != null) {
                    it[SECOND_JUMUAH_KEY] =
                        fromStringToLocalTime(fireStoreWeekModel.value?.secondJummah)
                }
            } else {
                it[DHUHR_KEY] = fromStringToLocalTime(fireStoreWeekModel.value?.dhuhr)
            }

            it[ASR_KEY] = fromStringToLocalTime(fireStoreWeekModel.value?.asr)

            magribJamaahTime?.let { mjt ->
                it[MAGHRIB_KEY] = fromStringToLocalTime(mjt)
            }

            it[ISHA_KEY] = if (isTodayWeekend() && fireStoreWeekModel.value?.weekendIsha != null) {
                fromStringToLocalTime(fireStoreWeekModel.value?.weekendIsha)
            } else {
                fromStringToLocalTime(fireStoreWeekModel.value?.isha)
            }
        }.toList().sortedBy {
            it.second
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.clear()
    }

    companion object {

        private const val GOOD_NIGHT_MSG = "Good Night"

        const val FAJR_KEY = "Fajr"
        const val DHUHR_KEY = "Dhohar"
        const val ASR_KEY = "Asr"
        const val MAGHRIB_KEY = "Maghrib"
        const val ISHA_KEY = "Isha"
        const val JUMUAH_TEXT = "Jumuah"
        const val FIRST_JUMUAH_KEY = "1st Jumuah"
        const val SECOND_JUMUAH_KEY = "2nd Jumuah"

        const val FIRST_SUPERSCRIPT = "1&#x02E2;&#x1D57;"
        const val SECOND_SUPERSCRIPT = "2&#x207F;&#x1D48;"
    }
}
