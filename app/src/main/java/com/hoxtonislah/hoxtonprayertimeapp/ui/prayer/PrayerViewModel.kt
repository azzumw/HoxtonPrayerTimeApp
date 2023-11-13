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
    LOADING, ERROR, DONE, S_ERROR
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

    val isTodayFriday: Boolean = isTodayFriday(LocalDate.now())

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
        if(isTodayWeekend()&& it?.winterTime == true){
            it.to12hour(it.weekendIsha)
        }else {
            it?.to12hour(it.isha)
        }
    }

    private val londonPrayerBeginningTimesFromDB: LiveData<LondonPrayersBeginningTimes?> =
        repository.todaysBeginningTimesFromDB

    val prayerBeginningTimesIn12HourFormat: LiveData<LondonPrayersBeginningTimes?> =
        londonPrayerBeginningTimesFromDB.map {
            it?.convertTo12hour()
        }

    private val _fireStoreApiStatus = MutableLiveData<ApiStatus>()

    private val _londonApiStatus = MutableLiveData<ApiStatus>()
    private val londonApiStatus: LiveData<ApiStatus>
        get() = _londonApiStatus

    val apiStatusLiveMerger = MediatorLiveData<ApiStatus>()

    //To highlight next prayer background view
    val fajrListItemBackground: LiveData<Boolean> = nextJamaat.map {
        it.substringBefore(" ") == FAJR_KEY
    }

    val dhuhrListItemBackground: LiveData<Boolean> = nextJamaat.map {
        when {
            it.contains(DHUHR_KEY) -> true
            it.contains(JUMUAH_TEXT) -> true
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
        getBeginningTimesFromLondonPrayerTimesApi()

        var count = 1

        apiStatusLiveMerger.addSource(londonPrayerBeginningTimesFromDB) { londonDataDB ->
            apiStatusLiveMerger.addSource(londonApiStatus) { status ->
                count++
                if (londonDataDB == null) {
                    if (status == ApiStatus.ERROR) {
                        apiStatusLiveMerger.value = ApiStatus.ERROR
                        if(BuildConfig.DEBUG){
                            Timber.d("DB null,status = ERROR")
                        }
                    } else {
                        apiStatusLiveMerger.value = ApiStatus.LOADING
                        if(BuildConfig.DEBUG){
                            Timber.d("DB null,status = LOAD")
                        }
                    }
                } else {
                    when (status) {
                        ApiStatus.LOADING -> {
                            apiStatusLiveMerger.value = (ApiStatus.LOADING)
                            if (BuildConfig.DEBUG){
                                Timber.d("DB,status = LOAD")
                            }
                        }

                        ApiStatus.ERROR -> {
                            apiStatusLiveMerger.value = ApiStatus.S_ERROR
                            if(BuildConfig.DEBUG){
                                Timber.d("DB ,status = S_ERROR")
                            }
                        }

                        else -> {
                            apiStatusLiveMerger.value = (ApiStatus.DONE)
                            if(BuildConfig.DEBUG){
                                Timber.d("DB,status = DONE")
                            }
                        }
                    }
                }
                apiStatusLiveMerger.removeSource(londonApiStatus)
            }

            if (count > 5) {
                apiStatusLiveMerger.removeSource(londonPrayerBeginningTimesFromDB)
            }
        }

        if(BuildConfig.DEBUG){
            repository.writeJamaahTimesToFireStore()
        }

        listenForPrayersFromFirestore()
    }


    private fun getBeginningTimesFromLondonPrayerTimesApi(todayLocalDate: LocalDate = LocalDate.now()) {
        _londonApiStatus.value = ApiStatus.LOADING

        viewModelScope.launch {

            try {

                val apiResult = repository.getPrayerBeginningTimesFromLondonApi(todayLocalDate)

                _londonApiStatus.value = ApiStatus.DONE

                repository.deleteYesterdayPrayer()

                repository.insertTodayPrayer(apiResult)

                val mjt = apiResult.getMaghribJamaahTime()

                repository.updateMaghribJamaahTime(
                    maghribJamaahTime = mjt,
                    todayLocalDate = getTodayDate(todayLocalDate)
                )

                workoutNextJamaah(mjt)


            } catch (dateTimeException: DateTimeParseException) {
               if (BuildConfig.DEBUG){
                   Timber.e("Date parsing exception ${dateTimeException.message}")
               }
            } catch (e: Exception) {
                if(BuildConfig.DEBUG){
                    Timber.e("Network exception ${e.message}")
                }
                _londonApiStatus.value = ApiStatus.ERROR
            }
        }
    }


    private fun listenForPrayersFromFirestore() {

        _fireStoreApiStatus.value = ApiStatus.LOADING

        repository.getJamaahTimesFromFireStore {
            workoutNextJamaah()
        }

        _fireStoreApiStatus.value = ApiStatus.DONE
    }

    /**
    This method weeds out those prayer times before the current time,
    If the list is empty i.e. all prayers have been filtered out, then a Good Night
    message is displayed. At midnight, this cycle repeats.
     */
    private fun workoutNextJamaah(prayerTime: String? = null) {
        //get the current time

        val tempPairNextJammah = addPrayersToMapForTheNextPrayerAndReturnSortedList(prayerTime).firstOrNull {

            LocalTime.now().isBefore(it.second)
        }

        _nextJamaat.value = if (tempPairNextJammah != null) {
            when (tempPairNextJammah.first) {
                FIRST_JUMUAH_KEY -> {
                    "${
                        Html.fromHtml(
                            FIRST_SUPERSCRIPT,
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } ${tempPairNextJammah.first.substringAfter(" ")} ${
                        fromLocalTimeToString(tempPairNextJammah.second)
                    }"
                }

                SECOND_JUMUAH_KEY -> {
                    "${
                        Html.fromHtml(
                            SECOND_SUPERSCRIPT,
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } ${tempPairNextJammah.first.substringAfter(" ")} ${
                        fromLocalTimeToString(
                            tempPairNextJammah.second
                        )
                    }"
                }

                else -> "${tempPairNextJammah.first} ${fromLocalTimeToString(tempPairNextJammah.second)}"

            }

        } else GOOD_NIGHT_MSG
    }

    private fun addPrayersToMapForTheNextPrayerAndReturnSortedList(prayerTime: String?): List<Pair<String, LocalTime?>> {
        return nextPrayersMap.also {

            it[FAJR_KEY] = fromStringToLocalTime(fireStoreWeekModel.value?.fajar)

            if (isTodayFriday(LocalDate.now())) {
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

            prayerTime?.let { mjt ->
                it[MAGHRIB_KEY] = fromStringToLocalTime(mjt)
            }

            it[ISHA_KEY] = if(isTodayWeekend() && fireStoreWeekModel.value?.winterTime == true){
                fromStringToLocalTime(fireStoreWeekModel.value?.weekendIsha)
            }else{
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
