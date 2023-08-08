package com.example.hoxtonprayertimeapp.ui.prayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.getFridayDate
import com.example.hoxtonprayertimeapp.models.Week
import com.example.hoxtonprayertimeapp.models.fromStringToDateTime
import com.example.hoxtonprayertimeapp.models.fromStringToDateTimeObj
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
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

    private lateinit var firestore: FirebaseFirestore

    private val collectionPrayers: CollectionReference

    private lateinit var listernereRegisteration: ListenerRegistration

    private val calendar = Calendar.getInstance(Locale.getDefault())

    private val date = getFridayDate()

    private val _prayer = MutableLiveData<Week?>()
    val prayer: LiveData<Week?> get() = _prayer

    private val _status = MutableLiveData<FireStoreStatus>()
    val status: LiveData<FireStoreStatus>
        get() = _status

    init {

        Timber.e(calendar.get(Calendar.WEEK_OF_YEAR).toString())

        initialiseFireStoreEmulator()

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)

        writePrayerTimesForThisWeek(date, calendar[Calendar.YEAR])

        listenForPrayers()


    }

    private fun initialiseFireStoreEmulator() {
        firestore = Firebase.firestore
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        Timber.e("ViewModel initialised ${firestore.app}")

    }

    private fun workoutNextJamaat() {
        val currentTime = Calendar.getInstance().time

        //only add those prayers whose time is after the current time
        //and display the first instance, else show Good Night message.
        val remainingTodayPrayersMap = nextPrayersMap.filterValues {
            currentTime.before(it)
        }

        val sd = SimpleDateFormat("hh:mm a").format(remainingTodayPrayersMap.toList()[0].second)

        _nextJamaat.value = if (currentTime.before(remainingTodayPrayersMap.toList()[0].second)) {
            "${remainingTodayPrayersMap.toList()[0].first} $sd"
        } else "Good Night"
    }

    private fun writePrayerTimesForThisWeek(date: String, year: Int) {

        val lastWeekNumber = createDocumentReferenceIDForLastWeek(calendar)

        val week = Week(
            date, year, fajar = fromStringToDateTime("04:30 am"),
            dhuhr = fromStringToDateTime("01:30 pm"),
            asr = fromStringToDateTime("06:45 pm"),
            maghrib = fromStringToDateTime("08:56 pm"),
            isha = fromStringToDateTime("10:15 pm"),
            firstJummah = fromStringToDateTime("02:15 pm"),
            secondJummah = fromStringToDateTime("02:15 pm")
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

        listernereRegisteration = queryLastFriday.addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e("Listen failed. $error")

                _status.value = FireStoreStatus.ERROR

                return@addSnapshotListener
            }

            val prayerObject = value!!.documents[0].toObject(Week::class.java)
            _prayer.value = prayerObject
            _status.value = FireStoreStatus.DONE

            nextPrayersMap["Fajar"] = fromStringToDateTimeObj(prayer.value?.fajar)
            nextPrayersMap["Dhuhr"] = fromStringToDateTimeObj(prayer.value?.dhuhr)
            nextPrayersMap["Asr"] = fromStringToDateTimeObj(prayer.value?.asr)
            nextPrayersMap["Maghrib"] = fromStringToDateTimeObj(prayer.value?.maghrib)
            nextPrayersMap["Isha"] = fromStringToDateTimeObj(prayer.value?.isha)
            nextPrayersMap["FirstJummah"] = fromStringToDateTimeObj(prayer.value?.firstJummah)
            nextPrayersMap["SecondJummah"] = fromStringToDateTimeObj(prayer.value?.secondJummah)
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
        listernereRegisteration.remove()
    }

    companion object {
        private const val EMULATOR_HOST = "10.0.2.2"
        private const val EMULATOR_PORT = 8080

        private const val COLLECTIONS_PRAYERS = "Prayers"

        private const val FRIDAY_DAY_KEY = "fridayDate"
    }
}

class PrayerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerViewModel::class.java)) {
            return PrayerViewModel() as T
        } else throw IllegalArgumentException("ViewModel not recognised")
    }
}