package com.example.hoxtonprayertimeapp.ui.prayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.getFridayDate
import com.example.hoxtonprayertimeapp.models.Week
import com.example.hoxtonprayertimeapp.utils.formatTimeToString
import com.example.hoxtonprayertimeapp.utils.fromStringToDateTimeObj
import com.example.hoxtonprayertimeapp.utils.getCurrentGregorianDate
import com.example.hoxtonprayertimeapp.utils.getCurrentIslamicDate
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
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

    private val _gregoryTodaysDate =  MutableLiveData(getCurrentGregorianDate())
    val gregoryTodaysDate : LiveData<String> get() = _gregoryTodaysDate

    private val _islamicTodaysDate = MutableLiveData(getCurrentIslamicDate())
    val islamicTodaysDate: LiveData<String> get() = _islamicTodaysDate

    val nextJamaat: LiveData<String> get() = _nextJamaat

    private lateinit var firestore: FirebaseFirestore

    private val collectionPrayers: CollectionReference

    private lateinit var listernerRegisteration: ListenerRegistration

    private val calendar = Calendar.getInstance(Locale.getDefault())

    private val date = getFridayDate()

    private val _prayer = MutableLiveData<Week?>()
    val prayer: LiveData<Week?> get() = _prayer

    private val _status = MutableLiveData<FireStoreStatus>()
    val status: LiveData<FireStoreStatus>
        get() = _status

    init {

        initialiseFireStoreEmulator()

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)

        writePrayerTimesForThisWeek()

        listenForPrayers()

    }

    private fun initialiseFireStoreEmulator() {
        firestore = Firebase.firestore
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        Timber.e("ViewModel initialised ${firestore.app}")
    }

    /**
    This method weeds out those prayers whose time is before the current time,
    leaving only those prayers in the list whose time is after the current time.
    If the list is empty i.e. all prayers have been filtered out, then a Good Night
    message is displayed. At midnight, this cycle repeats.
    */
    private fun workoutNextJamaat() {
        val currentTime = Calendar.getInstance().time

        nextPrayersMap.filterValues {
            currentTime.before(it)
        }.also {
            if (it.isNotEmpty() && currentTime.before(it.toList().first().second)) {
                val sd = formatTimeToString(it.toList().first().second)
                _nextJamaat.value = "${it.toList().first().first} $sd"
            } else _nextJamaat.value = "Good Night"
        }
    }

    private fun writePrayerTimesForThisWeek() {

        val lastWeekNumber = createDocumentReferenceIDForLastWeek(calendar)

        val week = Week(
            date,
            fajar = "04:30 am",
            dhuhr = "01:30 pm",
            asr = "06:45 pm",
            maghrib = "08:56 pm",
            isha = "10:15 pm",
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

            _prayer.value = value!!.documents[0].toObject(Week::class.java)

            _status.value = FireStoreStatus.DONE

            nextPrayersMap[FAJR_KEY] = fromStringToDateTimeObj(prayer.value?.fajar)
            nextPrayersMap[DHOHAR_KEY] = fromStringToDateTimeObj(prayer.value?.dhuhr)
            nextPrayersMap[ASR_KEY] = fromStringToDateTimeObj(prayer.value?.asr)
            nextPrayersMap[MAGHRIB_KEY] = fromStringToDateTimeObj(prayer.value?.maghrib)
            nextPrayersMap[ISHA_KEY] = fromStringToDateTimeObj(prayer.value?.isha)
            nextPrayersMap[FIRST_JUMMAH_KEY] = fromStringToDateTimeObj(prayer.value?.firstJummah)
            nextPrayersMap[SECOND_JUMMAH_KEY] = fromStringToDateTimeObj(prayer.value?.secondJummah)
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