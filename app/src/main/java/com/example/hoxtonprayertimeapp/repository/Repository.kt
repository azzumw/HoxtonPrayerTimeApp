package com.example.hoxtonprayertimeapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hoxtonprayertimeapp.datasource.LocalDataSource
import com.example.hoxtonprayertimeapp.datasource.RemoteDataSource
import com.example.hoxtonprayertimeapp.models.FireStoreWeekModel
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.example.hoxtonprayertimeapp.utils.getMostRecentFriday
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
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _fireStoreWeekModel = MutableLiveData<FireStoreWeekModel?>()
    val fireStoreWeekModel: LiveData<FireStoreWeekModel?> = _fireStoreWeekModel

    private lateinit var listernerRegisteration: ListenerRegistration

    private val collectionPrayers: CollectionReference

    val todaysBeginningTimesFromDB: LiveData<LondonPrayersBeginningTimes?> =
        localDataSource.todayPrayersFromLocalDataSource

    init {
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)
    }
    suspend fun getPrayerBeginningTimesFromLondonApi(localDate: LocalDate): LondonPrayersBeginningTimes {
        return remoteDataSource.getPrayerBeginningTimesFromRemoteNetwork(localDate)
    }

    suspend fun deleteYesterdayPrayer(yesterdayDate: String = getYesterdayDate()) {
        withContext(ioDispatcher) {
            localDataSource.deleteYesterdayPrayerFromLocalDataSource(yesterdayDate)
        }
    }

    suspend fun insertTodayPrayer(todayPrayerFromApi: LondonPrayersBeginningTimes) {
        withContext(ioDispatcher) {
            localDataSource.insertTodayPrayerToLocalDataSource(todayPrayerFromApi)
        }
    }

    suspend fun updateMaghribJamaahTime(maghribJamaahTime: String?, todayLocalDate: String) {
        withContext(ioDispatcher) {
            localDataSource.updateMaghribJamaahTimeInLocalDataSource(
                 maghribJamaahTime =  maghribJamaahTime,
                todayLocalDate = todayLocalDate
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
