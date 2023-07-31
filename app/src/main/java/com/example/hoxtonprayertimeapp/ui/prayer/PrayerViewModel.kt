package com.example.hoxtonprayertimeapp.ui.prayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.getFridayDate
import com.example.hoxtonprayertimeapp.models.Week
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class FireStoreStatus{
    LOADING,ERROR,DONE
}
class PrayerViewModel :ViewModel() {

    private val _nextJamaat = MutableLiveData<String>()
    val nextJamaat:LiveData<String> get() = _nextJamaat

    private lateinit var firestore: FirebaseFirestore

    private val collectionPrayers: CollectionReference

    private lateinit var listernereRegisteration:ListenerRegistration

    private val calendar = Calendar.getInstance(Locale.getDefault())

    private val date = getFridayDate()

    private val _prayer = MutableLiveData<Week?>()
    val prayer :LiveData<Week?> get() = _prayer

    private val _status = MutableLiveData<FireStoreStatus>()
    val status : LiveData<FireStoreStatus>
        get() = _status

    init {

        Timber.e(calendar.get(Calendar.WEEK_OF_YEAR).toString())

        initialiseFireStoreEmulator()

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)

        writePrayerTimesForThisWeek(date,calendar[Calendar.YEAR])

        listenForPrayers()
    }

    private fun initialiseFireStoreEmulator(){
        firestore = Firebase.firestore
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        Timber.e("ViewModel initialised ${firestore.app}")

    }

    private fun writePrayerTimesForThisWeek(date:String,year:Int){

        val lastWeekNumber = createDocumentReferenceIDForLastWeek(calendar)
//        val d = Timestamp.now().toDate().toString()
        val time = Timestamp.now()
        val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
//        val f = df.format(time)

        val week = Week(date,year,"04:30","01:30", "07:00","09:00","10:30","01:30", secondJummah = "02:15")
        val docRef = collectionPrayers.document(lastWeekNumber)

        docRef.set(week).addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.e( "Data Saved")
            } else Timber.e(it.exception.toString())
        }
    }

    private fun listenForPrayers(){
        //listen to last friday, it today is friday listen to today.

        _status.value = FireStoreStatus.LOADING

        val queryLastFriday = firestore.collection(COLLECTIONS_PRAYERS).whereEqualTo(FRIDAY_DAY_KEY,date)

        listernereRegisteration = queryLastFriday.addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e( "Listen failed. $error")

                _status.value = FireStoreStatus.ERROR

                return@addSnapshotListener
            }

            val prayerObject = value!!.documents[0].toObject(Week::class.java)
            _prayer.value = prayerObject
            _status.value = FireStoreStatus.DONE
//            for (doc in value!!) {
//                //convert to Prayer object
//                val prayerObj = doc.toObject(Week::class.java)
//                pMap += doc.id to prayerObj
//            }
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

class PrayerViewModelFactory: ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PrayerViewModel::class.java)){
            return PrayerViewModel() as T
        }else throw IllegalArgumentException("ViewModel not recognised")
    }
}