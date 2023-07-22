package com.example.hoxtonprayertimeapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hoxtonprayertimeapp.models.Week
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.util.Calendar
import java.util.Locale

class PrayerViewModel :ViewModel() {

    private lateinit var firestore: FirebaseFirestore

    private val collectionPrayers: CollectionReference

    val calendar = Calendar.getInstance(Locale.getDefault())

    val date = getFridayDate()


    init {
        initialiseFireStoreEmulator()

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)

        writePrayerTimesForThisWeek(date,calendar[Calendar.YEAR])
    }

    private fun initialiseFireStoreEmulator(){
        firestore = Firebase.firestore
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        Timber.e("ViewModel initialised ${firestore.app}")

    }

    private fun writePrayerTimesForThisWeek(date:String,year:Int){

        val lastWeekNumber = createDocumentReferenceIDForLastWeek(calendar)
        val week28 = Week(date,year,"04:00","01:30","07:00","09:20","10:45","01:30","02:15")
        val docRef = collectionPrayers.document(lastWeekNumber)

        docRef.set(week28).addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.e( "Data Saved")
            } else Timber.e(it.exception.toString())
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