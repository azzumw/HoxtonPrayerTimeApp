package com.hoxtonislah.hoxtonprayertimeapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import com.google.firebase.firestore.model.Values.isNullValue
import com.hoxtonislah.hoxtonprayertimeapp.data.source.remote.LondonPrayersBeginningTimes
import com.hoxtonislah.hoxtonprayertimeapp.util.MainCoroutineRule
import org.hamcrest.MatcherAssert
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.hoxtonislah.hoxtonprayertimeapp.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
@SmallTest
class PrayerDaoTests {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var database:PrayerBeginningTimesDatabase
    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PrayerBeginningTimesDatabase::class.java)
            .build()
    }

    @After
    fun tearDown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun insertPrayer_saves_the_prayer_in_the_database() = runTest {
        //GIVEN - a prayer object
        val prayer = LondonPrayersBeginningTimes(
            date = "2024-01-09",
            fajr = "07:00",
            sunrise = "08:00",
            dhuhr = "13:00",
            asr = "14:56",
            magrib = "16:15",
            isha = "17:51"
        )

        // AND - database is empty
        var result = database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat("prayer is not null",result, nullValue())


        // WHEN - this prayer is inserted in to the database
        database.prayerDao.insertPrayer(prayer)

        //THEN - verify the prayer is stored in the database
        result = database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(result, notNullValue())
        MatcherAssert.assertThat(result, equalTo(prayer))
        MatcherAssert.assertThat(result?.date, equalTo(prayer.date))
        MatcherAssert.assertThat(result, notNullValue(LondonPrayersBeginningTimes::class.java))
    }

    @Test
    fun getTodayPrayers_returns_null_when_there_is_no_data() = runTest {

        val result = database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()

        MatcherAssert.assertThat("database is not null",result, nullValue())
    }

    @Test
    fun updateMaghribJamaah() = runTest{
        //GIVEN - a prayer object (does not have a maghrib Jamaah time)
        val prayer = LondonPrayersBeginningTimes(
            date = "2024-01-09",
            fajr = "07:00",
            sunrise = "08:00",
            dhuhr = "13:00",
            asr = "14:56",
            magrib = "16:15",
            isha = "17:51"
        )

        // is inserted in the database
        database.prayerDao.insertPrayer(prayer)

        // verify prayer is inserted
        var prayerIsStored = database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(prayerIsStored, notNullValue())

        // however, maghrib jamaat is null value
        MatcherAssert.assertThat(prayerIsStored?.magribJamaah, nullValue())

        // WHEN - maghrib Jamaah is updated
        database.prayerDao.updateMaghribJamaah("16:17",prayer.date)

        // THEN - verify the maghrib Jamaah is updated for the same date
        prayerIsStored = database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(prayerIsStored?.magribJamaah, `is`("16:17"))

    }

    @Test
    fun clear_deletes_the_data_from_database() = runTest {
        //GIVEN - a prayer object
        val prayer = LondonPrayersBeginningTimes(
            date = "2024-01-09",
            fajr = "07:00",
            sunrise = "08:00",
            dhuhr = "13:00",
            asr = "14:56",
            magrib = "16:15",
            isha = "17:51"
        )

        database.prayerDao.insertPrayer(prayer)

        // AND - that there is prayer stored in the database
        var result = database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(result, notNullValue())

        // WHEN - clear is called
        database.prayerDao.clear()

        // THEN - verify that there is no data in the database
        result = database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(result, nullValue())
    }
}