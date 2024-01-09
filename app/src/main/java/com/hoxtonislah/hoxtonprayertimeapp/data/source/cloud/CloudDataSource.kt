package com.hoxtonislah.hoxtonprayertimeapp.data.source.cloud

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hoxtonislah.hoxtonprayertimeapp.data.source.remote.LondonPrayersBeginningTimes
import com.hoxtonislah.hoxtonprayertimeapp.utils.createDocumentReferenceIDForLastWeek
import com.hoxtonislah.hoxtonprayertimeapp.utils.getMostRecentFriday
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.hoxtonislah.hoxtonprayertimeapp.BuildConfig
import com.hoxtonislah.hoxtonprayertimeapp.data.source.PrayerDataSource
import timber.log.Timber
import java.time.LocalDate

class CloudDataSource(private val firestore: FirebaseFirestore) : PrayerDataSource {

    private val collectionPrayers: CollectionReference
    private lateinit var listernerRegisteration: ListenerRegistration

    private val _jamaahTimeCloudModel = MutableLiveData<JamaahTimeCloudModel?>()
    val jamaahTimeCloudModel: LiveData<JamaahTimeCloudModel?> = _jamaahTimeCloudModel

    init {
        if (BuildConfig.DEBUG) {
            Timber.d("debug firestore")
            firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
        }

        collectionPrayers = firestore.collection(COLLECTIONS_PRAYERS)
    }

    /**
     * Listens to the prayer data from firestore
     * @param - adds the jamaah time to a map, and works out the next upcoming Jamaah
     * */
    override fun getTodayJamaahTimesFromCloud(workoutNextJamaah: () -> Unit) {
        Timber.e("CloudDS: getJamaahTimesFromCloud ")
        val queryMostRecentFriday =
            firestore.collection(COLLECTIONS_PRAYERS).whereEqualTo(
                FRIDAY_DAY_KEY, getMostRecentFriday()
            )

        listernerRegisteration = queryMostRecentFriday.addSnapshotListener { value, error ->


            if (error != null) {
                if (BuildConfig.DEBUG) {
                    Timber.d("Listen failed. $error")
                    Timber.e("Error")
                }
                return@addSnapshotListener
            }

          value?.let {
              if(it.documents.isNotEmpty()){
                  Timber.e("_jammahCloudmodel being set")
                  _jamaahTimeCloudModel.value =
                      value.documents[0].toObject(JamaahTimeCloudModel::class.java)

                  workoutNextJamaah()
              }
          }
        }
    }

    override fun writeJamaahTimesToCloud() {
        val lastWeekNumber = createDocumentReferenceIDForLastWeek()

        val jamaahTimeCloudModel = JamaahTimeCloudModel(
            getMostRecentFriday(),
            fajar = "06:30",
            dhuhr = "13:00",
            asr = "14:45",
            isha = "20:00",
            weekendIsha = "18:30",
            firstJummah = "12:20",
            secondJummah = "13:00"
        )
        val docRef = collectionPrayers.document(lastWeekNumber)

        docRef.set(jamaahTimeCloudModel).addOnCompleteListener {
            if (it.isSuccessful) {
                if (BuildConfig.DEBUG) {
                    Timber.d("Data Saved")
                }

            } else {
                if (BuildConfig.DEBUG) {
                    Timber.e(it.exception.toString())
                }
            }
        }
    }

    override suspend fun getPrayerBeginTimesFromRemoteApi(localDate: LocalDate): LondonPrayersBeginningTimes {
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

    fun clear() {
        firestore.clearPersistence()
        listernerRegisteration.remove()
    }

    companion object {
        private const val EMULATOR_HOST = "10.0.2.2"
        private const val EMULATOR_PORT = 8080

        const val COLLECTIONS_PRAYERS = "Prayers"

        const val FRIDAY_DAY_KEY = "fridayDate"

    }
}