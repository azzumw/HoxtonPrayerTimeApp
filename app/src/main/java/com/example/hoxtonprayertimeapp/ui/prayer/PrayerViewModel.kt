package com.example.hoxtonprayertimeapp.ui.prayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.getFridayDate
import com.example.hoxtonprayertimeapp.models.Week
import com.example.hoxtonprayertimeapp.network.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.network.PrayersApi
import com.example.hoxtonprayertimeapp.utils.formatTimeToString
import com.example.hoxtonprayertimeapp.utils.fromStringToDateTimeObj
import com.example.hoxtonprayertimeapp.utils.getCurrentGregorianDate
import com.example.hoxtonprayertimeapp.utils.getCurrentIslamicDate
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalArgumentException
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

    private val _gregoryTodayDate = MutableLiveData(getCurrentGregorianDate())
    val gregoryTodayDate: LiveData<String> get() = _gregoryTodayDate

    private val _islamicTodayDate = MutableLiveData(getCurrentIslamicDate())
    val islamicTodayDate: LiveData<String> get() = _islamicTodayDate

    private lateinit var firestore: FirebaseFirestore

    private val collectionPrayers: CollectionReference

    private lateinit var listernerRegisteration: ListenerRegistration

    private val calendar = Calendar.getInstance(Locale.getDefault())

    private val date = getFridayDate()

    private val _week = MutableLiveData<Week?>()
    val week: LiveData<Week?> get() = _week

    private val _londonPrayerBeginningTimes = MutableLiveData<LondonPrayersBeginningTimes>()
    val londonPrayerBeginningTimes: LiveData<LondonPrayersBeginningTimes> get() = _londonPrayerBeginningTimes

    private val _status = MutableLiveData<FireStoreStatus>()
    val status: LiveData<FireStoreStatus>
        get() = _status

    init {

        initialiseFireStoreEmulator()

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)

        writePrayerTimesForThisWeek()

        listenForPrayers()

        getBeginningTimesFromLondonPrayerTimesApi()

    }

    private fun initialiseFireStoreEmulator() {
        firestore = Firebase.firestore
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        Timber.e("ViewModel initialised ${firestore.app}")
    }

    private fun getBeginningTimesFromLondonPrayerTimesApi() {
        viewModelScope.launch {
            try {
                val apiResult = PrayersApi.retrofitService.getTodaysPrayerBeginningTimes()
                _londonPrayerBeginningTimes.value = apiResult
                Timber.i(londonPrayerBeginningTimes.value?.fajr.toString())
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
    private fun workoutNextJamaat() {
        //get the current time
        val currentTime = Calendar.getInstance().time

        //filter prayers with time after the current time
        nextPrayersMap.filterValues {
            currentTime.before(it)
        }.also {
            //when not empty, currentTime is before the prayer time of the first prayer element
            if (it.isNotEmpty() && currentTime.before(it.toList().first().second)) {
                //format the time in hh:mm a
                val formattedNextJamaatTime = formatTimeToString(it.toList().first().second)
                _nextJamaat.value = "${it.toList().first().first} $formattedNextJamaatTime"
            } else _nextJamaat.value = "Good Night"
        }
    }

    private fun writePrayerTimesForThisWeek() {

        val lastWeekNumber = createDocumentReferenceIDForLastWeek(calendar)

        val week = Week(
            date,
            fajar = "04:45 am",
            dhuhr = "01:30 pm",
            asr = "06:30 pm",
            maghrib = "08:27 pm",
            isha = "10:00 pm",
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

    private fun listenForPrayers() {
        //listen to last friday, it today is friday listen to today.
        _status.value = FireStoreStatus.LOADING

        val queryLastFriday =
            firestore.collection(COLLECTIONS_PRAYERS).whereEqualTo(FRIDAY_DAY_KEY, date)

        listernerRegisteration = queryLastFriday.addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e("Listen failed. $error")

                _status.value = FireStoreStatus.ERROR

                return@addSnapshotListener
            }

            _week.value = value!!.documents[0].toObject(Week::class.java)

            _status.value = FireStoreStatus.DONE

            nextPrayersMap[FAJR_KEY] = fromStringToDateTimeObj(week.value?.fajar)
            nextPrayersMap[DHOHAR_KEY] = fromStringToDateTimeObj(week.value?.dhuhr)
            nextPrayersMap[ASR_KEY] = fromStringToDateTimeObj(week.value?.asr)
            nextPrayersMap[MAGHRIB_KEY] = fromStringToDateTimeObj(week.value?.maghrib)
            nextPrayersMap[ISHA_KEY] = fromStringToDateTimeObj(week.value?.isha)
            nextPrayersMap[FIRST_JUMMAH_KEY] = fromStringToDateTimeObj(week.value?.firstJummah)
            nextPrayersMap[SECOND_JUMMAH_KEY] = fromStringToDateTimeObj(week.value?.secondJummah)
//            for (doc in value!!) {
//                //convert to Prayer object
//                val prayerObj = doc.toObject(Week::class.java)
//                pMap += doc.id to prayerObj
//            }

            Timber.e("nextPrayerMap: ${nextPrayersMap.keys}")
            workoutNextJamaat()
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

        const val FAJR_KEY = "Fajr"
        const val DHOHAR_KEY = "Dhohar"
        const val ASR_KEY = "Asr"
        const val MAGHRIB_KEY = "Maghrib"
        const val ISHA_KEY = "Isha"
        const val FIRST_JUMMAH_KEY = "FirstJummah"
        const val SECOND_JUMMAH_KEY = "SecondJummah"

    }
}

class PrayerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerViewModel::class.java)) {
            return PrayerViewModel() as T
        } else throw IllegalArgumentException("ViewModel not recognised")
    }
}