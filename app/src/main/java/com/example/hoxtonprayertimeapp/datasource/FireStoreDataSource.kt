package com.example.hoxtonprayertimeapp.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hoxtonprayertimeapp.models.FireStoreWeekModel
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.getMostRecentFriday
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.hoxtonislah.hoxtonprayertimeapp.BuildConfig
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate

class FireStoreDataSource(private val firestore: FirebaseFirestore):PrayerDataSource {

    private val collectionPrayers: CollectionReference
    private lateinit var listernerRegisteration: ListenerRegistration

    private val _fireStoreWeekModel = MutableLiveData<FireStoreWeekModel?>()
    val fireStoreWeekModel: LiveData  <FireStoreWeekModel?> = _fireStoreWeekModel

    init {
        if(BuildConfig.DEBUG){
            Timber.d("debug firestore")
            firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        }

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)
    }

    /**
     * Listens to the prayer data from firestore
     * @param - adds the jamaah time to a map, and works out the next upcoming Jamaah
     * */
    override fun getTodayJamaahTimes(workoutNextJamaah: () -> Unit) {
        val queryMostRecentFriday =
            firestore.collection(COLLECTIONS_PRAYERS).whereEqualTo(
                FRIDAY_DAY_KEY, getMostRecentFriday(
                    Clock.systemDefaultZone()
                )
            )

        listernerRegisteration = queryMostRecentFriday.addSnapshotListener { value, error ->


            if (error != null) {
                Timber.e("Listen failed. $error")

                return@addSnapshotListener
            }

            _fireStoreWeekModel.value =
                value!!.documents[0].toObject(FireStoreWeekModel::class.java)

            workoutNextJamaah()
        }
    }

    override fun writeJamaahTimes() {
        val lastWeekNumber = createDocumentReferenceIDForLastWeek()

        val fireStoreWeekModel = FireStoreWeekModel(
            getMostRecentFriday(Clock.systemDefaultZone()),
            fajar = "06:15",
            dhuhr = "13:30",
            asr = "17:00",
            isha = "20:00",
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

    override suspend fun getPrayerBeginningTimesFromRemoteNetwork(localDate: LocalDate): LondonPrayersBeginningTimes {
        TODO("Not yet implemented")
    }

    override suspend fun insertTodayPrayerToLocalDataSource(todayPrayerFromApi: LondonPrayersBeginningTimes) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteYesterdayPrayerFromLocalDataSource(yesterdayDate: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMaghribJamaahTimeInLocalDataSource(
        maghribJamaahTime: String?,
        todayLocalDate: String
    ) {
        TODO("Not Required")
    }

    fun clear(){
        firestore.clearPersistence()
        listernerRegisteration.remove()
    }

    companion object{
        private const val EMULATOR_HOST = "10.0.2.2"
        private const val EMULATOR_PORT = 8080

        const val COLLECTIONS_PRAYERS = "Prayers"

        const val FRIDAY_DAY_KEY = "fridayDate"

    }
}