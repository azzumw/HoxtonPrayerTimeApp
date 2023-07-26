package com.example.hoxtonprayertimeapp.ui.prayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.getFridayDate
import com.example.hoxtonprayertimeapp.models.Week
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.DateTime
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.util.Calendar
import java.util.Locale

class PrayerViewModel :ViewModel() {

    private lateinit var firestore: FirebaseFirestore

    private val collectionPrayers: CollectionReference

    val calendar = Calendar.getInstance(Locale.getDefault())

    val date = getFridayDate()

    private val _prayer = MutableLiveData<Week?>()
    val prayer :LiveData<Week?> get() = _prayer

    init {
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
        val fajarTS = DateTime.getDefaultInstance()
        Timber.e(fajarTS.toString())
        val week28 = Week(date,year,"04:15","01:30", "07:00","09:05","10:30","01:30")
        val docRef = collectionPrayers.document(lastWeekNumber)

        docRef.set(week28).addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.e( "Data Saved")
            } else Timber.e(it.exception.toString())
        }
    }

    private fun listenForPrayers(){
        //listen to last friday, it today is friday listen to today.
        val date = getFridayDate()

        val queryLastFriday = firestore.collection(COLLECTIONS_PRAYERS).whereEqualTo(FRIDAY_DAY_KEY,date)

        val registration = queryLastFriday.addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e( "Listen failed. $error")
//                _errorMessage.value = error.message
//                _prayer.value = null
                return@addSnapshotListener
            }

            val prayerObject = value!!.documents[0].toObject(Week::class.java)
            _prayer.value = prayerObject
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