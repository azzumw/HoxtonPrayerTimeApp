package com.example.hoxtonprayertimeapp.ui.prayer

import android.text.Html
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hoxtonprayertimeapp.models.Week
import com.example.hoxtonprayertimeapp.network.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.network.PrayersApi
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.formatTimeToString
import com.example.hoxtonprayertimeapp.utils.fromStringToDateTimeObj
import com.example.hoxtonprayertimeapp.utils.getCurrentGregorianDate
import com.example.hoxtonprayertimeapp.utils.getCurrentIslamicDate
import com.example.hoxtonprayertimeapp.utils.getFridayDate
import com.example.hoxtonprayertimeapp.utils.getTodayDate
import com.example.hoxtonprayertimeapp.utils.isFridayToday
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FireStoreStatus {
    LOADING, ERROR, DONE
}

class PrayerViewModel : ViewModel() {

    private val nextPrayersMap = mutableMapOf<String, Date?>()

    private val _nextJamaat = MutableLiveData<String>()
    val nextJamaat: LiveData<String> get() = _nextJamaat

    val nextJamaatLabelVisibility: LiveData<Boolean> = Transformations.map(nextJamaat) {
        it != GOOD_NIGHT_MSG
    }

    private val _gregoryTodayDate = MutableLiveData(getCurrentGregorianDate())
    val gregoryTodayDate: LiveData<String> get() = _gregoryTodayDate

    private val _islamicTodayDate = MutableLiveData(getCurrentIslamicDate())
    val islamicTodayDate: LiveData<String> get() = _islamicTodayDate

    private lateinit var firestore: FirebaseFirestore

    private val collectionPrayers: CollectionReference

    private lateinit var listernerRegisteration: ListenerRegistration

    private val calendar = Calendar.getInstance(Locale.getDefault())

    private val fridayDate = getFridayDate()

    private val _week = MutableLiveData<Week?>()
    val week: LiveData<Week?> get() = _week

    private val _londonPrayerBeginningTimes = MutableLiveData<LondonPrayersBeginningTimes>()
    val londonPrayerBeginningTimes: LiveData<LondonPrayersBeginningTimes> get() = _londonPrayerBeginningTimes

    private val _status = MutableLiveData<FireStoreStatus>()
    val status: LiveData<FireStoreStatus>
        get() = _status

    private val _maghribFromApi = MutableLiveData<String>()
    val maghribFromApi: LiveData<String> get() = _maghribFromApi

    init {

        getBeginningTimesFromLondonPrayerTimesApi()

        initialiseFireStoreEmulator()

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)

        writePrayerTimesToFirestoreForThisWeek()

        listenForPrayersFromFirestore()
    }

    private fun initialiseFireStoreEmulator() {
        firestore = Firebase.firestore
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        Timber.e("ViewModel initialised ${firestore.app}")
    }


    private fun getBeginningTimesFromLondonPrayerTimesApi() {
        viewModelScope.launch {
            try {
                val apiResult = PrayersApi.retrofitService.getTodaysPrayerBeginningTimes(
                    date = getTodayDate(LONDON_PRAYER_API_DATE_PATTERN)
                )
                _londonPrayerBeginningTimes.value = apiResult

                _maghribFromApi.value = londonPrayerBeginningTimes.value?.magribJamaat

                nextPrayersMap[MAGHRIB_KEY] = fromStringToDateTimeObj(maghribFromApi.value)

                workoutNextJamaah()

               Timber.i("${nextPrayersMap.values}")

            } catch (e: Exception) {
                Timber.e(e.message)
            }
        }
    }

    /**
    This method weeds out those prayer times before the current time,
    If the list is empty i.e. all prayers have been filtered out, then a Good Night
    message is displayed. At midnight, this cycle repeats.
     */
    private fun workoutNextJamaah() {
        //get the current time
        val currentTime = Calendar.getInstance().time

        //filter prayers with time after the current time
        val nj = nextPrayersMap.filterValues {
            currentTime.before(it)
        }.toList().sortedBy {
            it.second
        }.firstOrNull()

        _nextJamaat.value = if(nj != null){
            when(nj.first){
                FIRST_JUMMAH_KEY -> {
                    "${Html.fromHtml(FIRST_SUPERSCRIPT,Html.FROM_HTML_MODE_LEGACY)} ${nj.first} ${formatTimeToString(nj.second)}"
                }
                SECOND_JUMMAH_KEY ->{
                    "${Html.fromHtml(SECOND_SUPERSCRIPT,Html.FROM_HTML_MODE_LEGACY)} ${nj.first} ${formatTimeToString(nj.second)}"
                }
                else -> "${nj.first} ${formatTimeToString(nj.second)}"

            }

        }else GOOD_NIGHT_MSG
    }

    private fun writePrayerTimesToFirestoreForThisWeek() {
        val lastWeekNumber = createDocumentReferenceIDForLastWeek(calendar)

        val week = Week(
            fridayDate,
            fajar = "05:00 am",
            dhuhr = "01:30 pm",
            asr = "06:30 pm",
            isha = "09:45 pm",
            firstJummah = "01:30 pm",
            secondJummah = "02:15 pm"
        )
        val docRef = collectionPrayers.document(lastWeekNumber)

        docRef.set(week).addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.e("Data Saved")
            } else Timber.e(it.exception.toString())
        }
    }

    private fun listenForPrayersFromFirestore() {
        //listen to last friday, it today is friday listen to today.
        _status.value = FireStoreStatus.LOADING

        val queryLastFriday =
            firestore.collection(COLLECTIONS_PRAYERS).whereEqualTo(FRIDAY_DAY_KEY, fridayDate)

        listernerRegisteration = queryLastFriday.addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e("Listen failed. $error")

                _status.value = FireStoreStatus.ERROR

                return@addSnapshotListener
            }

            _week.value = value!!.documents[0].toObject(Week::class.java)
            _status.value = FireStoreStatus.DONE

            nextPrayersMap.also {

                it[FAJR_KEY] = fromStringToDateTimeObj(week.value?.fajar)

                if (isFridayToday()) {
                    it[FIRST_JUMMAH_KEY] = fromStringToDateTimeObj(week.value?.firstJummah)
                    if (week.value?.secondJummah != null) {
                        it[SECOND_JUMMAH_KEY] = fromStringToDateTimeObj(week.value?.secondJummah)
                    }
                } else {
                    it[DHOHAR_KEY] = fromStringToDateTimeObj(week.value?.dhuhr)
                }

                it[ASR_KEY] = fromStringToDateTimeObj(week.value?.asr)
                it[ISHA_KEY] = fromStringToDateTimeObj(week.value?.isha)
            }

            workoutNextJamaah()
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
        const val DHOHAR_KEY = "Dhohar"
        const val ASR_KEY = "Asr"
        const val MAGHRIB_KEY = "Maghrib"
        const val ISHA_KEY = "Isha"
        const val FIRST_JUMMAH_KEY = "1st Jumuah"
        const val SECOND_JUMMAH_KEY = "2nd Jumuah"
        const val LONDON_PRAYER_API_DATE_PATTERN = "yyyy-MM-dd"
        const val FIRST_SUPERSCRIPT = "1&#x02E2;&#x1D57;"
        const val SECOND_SUPERSCRIPT = "2&#x207F;&#x1D48;"
    }
}

class PrayerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerViewModel::class.java)) {
            return PrayerViewModel() as T
        } else throw IllegalArgumentException("ViewModel not recognised")
    }
}