package com.example.hoxtonprayertimeapp.ui.prayer

import android.text.Html
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.hoxtonprayertimeapp.models.FireStoreWeekModel
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.models.convertTo12hour
import com.example.hoxtonprayertimeapp.repository.Repository
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.fromLocalTimeToString
import com.example.hoxtonprayertimeapp.utils.fromStringToLocalTime
import com.example.hoxtonprayertimeapp.utils.getCurrentGregorianDate
import com.example.hoxtonprayertimeapp.utils.getCurrentIslamicDate
import com.example.hoxtonprayertimeapp.utils.getMostRecentFriday
import com.example.hoxtonprayertimeapp.utils.getTodayDate
import com.example.hoxtonprayertimeapp.utils.isTodayFriday
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Clock
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

    private val _fireStoreWeekModel = MutableLiveData<FireStoreWeekModel?>()
    val fireStoreWeekModel: LiveData<FireStoreWeekModel?> get() = _fireStoreWeekModel

    val isTodayFriday:Boolean = isTodayFriday(LocalDate.now())

    val fajarJamaah12hour:LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.fajar)
    }

    val dhuhrJamaah12hour:LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.dhuhr)
    }

    val firstJummahJamaah12hour:LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.firstJummah)
    }

    val secondJummahJamaah12hour:LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.secondJummah)
    }

    val asrJamaah12hour:LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.asr)
    }

    val ishaJamaah12hour:LiveData<String?> = fireStoreWeekModel.map {
        it?.to12hour(it.isha)
    }

     private val londonPrayerBeginningTimesFromDB: LiveData<LondonPrayersBeginningTimes?> = repository.todaysBeginningTimesFromDB

    val tempLondonPrayerTimein12HourFormat:LiveData<LondonPrayersBeginningTimes?> = londonPrayerBeginningTimesFromDB.map {
        it?.convertTo12hour()
    }

    private val _fireStoreApiStatus = MutableLiveData<ApiStatus>()
    private val fireStoreApiStatus: LiveData<ApiStatus>
        get() = _fireStoreApiStatus

    private val _londonApiStatus = MutableLiveData<ApiStatus>()
    private val londonApiStatus: LiveData<ApiStatus>
        get() = _londonApiStatus

    val apiStatusLiveMerger = MediatorLiveData<ApiStatus>()

    private lateinit var firestore: FirebaseFirestore

    private val collectionPrayers: CollectionReference

    private lateinit var listernerRegisteration: ListenerRegistration

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

        initialiseFireStoreEmulator()
        var count = 1

        apiStatusLiveMerger.addSource(londonPrayerBeginningTimesFromDB) { londonDataDB ->
            apiStatusLiveMerger.addSource(londonApiStatus) { status ->
                count++
                if (londonDataDB == null) {
                    if (status == ApiStatus.ERROR) {
                        apiStatusLiveMerger.value = ApiStatus.ERROR
                        Timber.i("DB null,status = ERROR")

                    } else {
                        apiStatusLiveMerger.value = ApiStatus.LOADING
                        Timber.i("DB null,status = LOAD")
                    }
                } else {
                    when (status) {
                        ApiStatus.LOADING -> {
                            apiStatusLiveMerger.value = (ApiStatus.LOADING)
                            Timber.i("DB,status = LOAD")
                        }

                        ApiStatus.ERROR -> {
                            apiStatusLiveMerger.value = ApiStatus.S_ERROR
                            Timber.i("DB ,status = S_ERROR")
                        }

                        else -> {
                            apiStatusLiveMerger.value = (ApiStatus.DONE)
                            Timber.i("DB,status = DONE")
                        }
                    }
                }
                apiStatusLiveMerger.removeSource(londonApiStatus)
            }

            if (count > 5) {
                apiStatusLiveMerger.removeSource(londonPrayerBeginningTimesFromDB)
            }
        }

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)

        writePrayerTimesToFirestoreForThisWeek()

        listenForPrayersFromFirestore()


    }


    private fun initialiseFireStoreEmulator() {
        firestore = Firebase.firestore
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        Timber.e("ViewModel initialised ${firestore.app}")
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

                repository.updateMaghribJamaahTime(maghribJamaahTime = mjt, todayLocalDate = getTodayDate(todayLocalDate))

                workoutNextJamaah(mjt)


            } catch (dateTimeException:DateTimeParseException) {
                Timber.e("Date parsing exception ${dateTimeException.message}")
            }catch (e: Exception){
                Timber.e("Network exception ${e.message}")
                _londonApiStatus.value = ApiStatus.ERROR
            }
        }
    }

    private fun listenForPrayersFromFirestore() {
        //listen to last friday, if today is friday listen to today.

        _fireStoreApiStatus.value = ApiStatus.LOADING
        val queryLastFriday =
            firestore.collection(COLLECTIONS_PRAYERS).whereEqualTo(FRIDAY_DAY_KEY, getMostRecentFriday(
                Clock.systemDefaultZone()))

        listernerRegisteration = queryLastFriday.addSnapshotListener { value, error ->


            if (error != null) {
                Timber.e("Listen failed. $error")

                _fireStoreApiStatus.value = ApiStatus.ERROR
                Log.e("FireApi status set:", fireStoreApiStatus.value!!.name)
                return@addSnapshotListener
            }

            _fireStoreWeekModel.value =
                value!!.documents[0].toObject(FireStoreWeekModel::class.java)

            _fireStoreApiStatus.value = ApiStatus.DONE

            Log.e("FireApi status set:", fireStoreApiStatus.value.toString())
            workoutNextJamaah()
        }
    }

    /**
    This method weeds out those prayer times before the current time,
    If the list is empty i.e. all prayers have been filtered out, then a Good Night
    message is displayed. At midnight, this cycle repeats.
     */
    private fun workoutNextJamaah(prayerTime: String? = null) {
        //get the current time
        val currentTime = LocalTime.now()
        Timber.i(currentTime.toString())

        val tempPairNextJammah = addPrayersToMapForTheNextPrayer(prayerTime).firstOrNull {
            Timber.i("prayer: ${it.first} ${it.second}" )
            currentTime.isBefore(it.second)
        }

        Timber.i("${tempPairNextJammah?.second}")

        _nextJamaat.value = if (tempPairNextJammah != null) {
            when (tempPairNextJammah.first) {
                FIRST_JUMMAH_KEY -> {
                    "${
                        Html.fromHtml(
                            FIRST_SUPERSCRIPT,
                            Html.FROM_HTML_MODE_LEGACY
                        )
                    } ${tempPairNextJammah.first.substringAfter(" ")} ${
                        fromLocalTimeToString(tempPairNextJammah.second)
                    }"
                }

                SECOND_JUMMAH_KEY -> {
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

    private fun addPrayersToMapForTheNextPrayer(prayerTime: String?): List<Pair<String, LocalTime?>> {
        return nextPrayersMap.also {

            it[FAJR_KEY] = fromStringToLocalTime(fireStoreWeekModel.value?.fajar)

            if (isTodayFriday(LocalDate.now())) {
                it[FIRST_JUMMAH_KEY] =
                    fromStringToLocalTime(fireStoreWeekModel.value?.firstJummah)
                if (fireStoreWeekModel.value?.secondJummah != null) {
                    it[SECOND_JUMMAH_KEY] =
                        fromStringToLocalTime(fireStoreWeekModel.value?.secondJummah)
                }
            } else {
                it[DHUHR_KEY] = fromStringToLocalTime(fireStoreWeekModel.value?.dhuhr)
            }

            it[ASR_KEY] = fromStringToLocalTime(fireStoreWeekModel.value?.asr)

            prayerTime?.let { mjt ->
                it[MAGHRIB_KEY] = fromStringToLocalTime(mjt)
            }

            it[ISHA_KEY] = fromStringToLocalTime(fireStoreWeekModel.value?.isha)

        }.toList().sortedBy {
            it.second
        }.also {
            Timber.i("${it.size}")
        }
    }

    private fun writePrayerTimesToFirestoreForThisWeek() {
        val lastWeekNumber = createDocumentReferenceIDForLastWeek()

        val fireStoreWeekModel = FireStoreWeekModel(
            getMostRecentFriday(Clock.systemDefaultZone()),
            fajar = "05:45",
            dhuhr = "13:30",
            asr = "17:45",
            isha = "21:00",
            firstJummah = "13:30",
            secondJummah = "14:15"
        )
        val docRef = collectionPrayers.document(lastWeekNumber)

        docRef.set(fireStoreWeekModel).addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.e("Data Saved")
            } else Timber.e(it.exception.toString())
        }
    }

    override fun onCleared() {
        super.onCleared()
        firestore.clearPersistence()
        listernerRegisteration.remove()
    }

    companion object {
        private const val EMULATOR_HOST = "10.0.2.2"
        private const val EMULATOR_PORT = 8080

        private const val COLLECTIONS_PRAYERS = "Prayers"

        private const val FRIDAY_DAY_KEY = "fridayDate"

        private const val GOOD_NIGHT_MSG = "Good Night"

        const val FAJR_KEY = "Fajr"
        const val DHUHR_KEY = "Dhohar"
        const val ASR_KEY = "Asr"
        const val MAGHRIB_KEY = "Maghrib"
        const val ISHA_KEY = "Isha"
        const val JUMUAH_TEXT = "Jumuah"
        const val FIRST_JUMMAH_KEY = "1st Jumuah"
        const val SECOND_JUMMAH_KEY = "2nd Jumuah"

        const val FIRST_SUPERSCRIPT = "1&#x02E2;&#x1D57;"
        const val SECOND_SUPERSCRIPT = "2&#x207F;&#x1D48;"
    }
}

//class PrayerViewModelFactory(private val dao: PrayerDao) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(PrayerViewModel::class.java)) {
//            return PrayerViewModel(dao) as T
//        } else throw IllegalArgumentException("ViewModel not recognised")
//    }
//}