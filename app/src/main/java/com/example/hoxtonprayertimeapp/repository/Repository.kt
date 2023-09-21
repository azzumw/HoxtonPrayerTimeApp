package com.example.hoxtonprayertimeapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.hoxtonprayertimeapp.database.PrayerDao
import com.example.hoxtonprayertimeapp.models.FireStoreWeekModel
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.network.PrayersApi
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.getMostRecentFriday
import com.example.hoxtonprayertimeapp.utils.getTodayDate
import com.example.hoxtonprayertimeapp.utils.getYesterdayDate
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate

class Repository(
    private val prayerDao: PrayerDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _fireStoreWeekModel = MutableLiveData<FireStoreWeekModel?>()
    val fireStoreWeekModel: LiveData<FireStoreWeekModel?> = _fireStoreWeekModel

    private lateinit var listernerRegisteration: ListenerRegistration

    private val collectionPrayers: CollectionReference

    val todaysBeginningTimesFromDB: LiveData<LondonPrayersBeginningTimes?> =
        prayerDao.getTodayPrayers(
            getTodayDate(LocalDate.now())
        ).asLiveData()

    init {
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)
    }
    suspend fun getPrayerBeginningTimesFromLondonApi(localDate: LocalDate): LondonPrayersBeginningTimes {
        return PrayersApi.retrofitService.getTodaysPrayerBeginningTimes(
            date = getTodayDate(
                localDate
            )
        )
    }

    suspend fun deleteYesterdayPrayer(yesterdayDate: String = getYesterdayDate()) {
        withContext(ioDispatcher) {
            prayerDao.deleteYesterdayPrayers(yesterdayDate)
        }
    }

    suspend fun insertTodayPrayer(todayPrayerFromApi: LondonPrayersBeginningTimes) {
        withContext(ioDispatcher) {
            prayerDao.insertPrayer(todayPrayerFromApi)
        }
    }

    suspend fun updateMaghribJamaahTime(maghribJamaahTime: String?, todayLocalDate: String) {
        withContext(ioDispatcher) {
            prayerDao.updateMaghribJamaah(
                magribJamaahTime = maghribJamaahTime,
                todayDate = todayLocalDate
            )
        }
    }

    fun getJamaahTimesFromFireStore(func: () -> Unit) {

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

        }
    }

    fun writeJamaahTimesToFireStore(){
        val lastWeekNumber = createDocumentReferenceIDForLastWeek()

        val fireStoreWeekModel = FireStoreWeekModel(
            getMostRecentFriday(Clock.systemDefaultZone()),
            fajar = "06:00",
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
