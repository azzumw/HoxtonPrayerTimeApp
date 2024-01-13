package com.hoxtonislah.hoxtonprayertimeapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.test.filters.SmallTest
import com.hoxtonislah.hoxtonprayertimeapp.data.source.remote.LondonPrayersBeginningTimes
import com.hoxtonislah.hoxtonprayertimeapp.util.DatabaseRule
import com.hoxtonislah.hoxtonprayertimeapp.util.MainCoroutineRule
import org.hamcrest.MatcherAssert
import org.hamcrest.CoreMatchers.*
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

    @JvmField
    @Rule
    var databaseRule = DatabaseRule()

    private val prayer = LondonPrayersBeginningTimes(
        date = "2024-01-09",
        fajr = "07:00",
        sunrise = "08:00",
        dhuhr = "13:00",
        asr = "14:56",
        magrib = "16:15",
        isha = "17:51"
    )


    @Test
    fun insertPrayer_saves_the_prayer_in_the_database() = runTest {

        // GIVEN - an empty database
        var result = databaseRule.database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat("Prayer is not null",result, nullValue())

        // WHEN - a prayer is inserted in to the database
        databaseRule.database.prayerDao.insertPrayer(prayer)

        //THEN - verify that the prayer is stored in the database
        result = databaseRule.database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(result, notNullValue())
        MatcherAssert.assertThat(result, equalTo(prayer))
        MatcherAssert.assertThat(result?.date, equalTo(prayer.date))
        MatcherAssert.assertThat(result, notNullValue(LondonPrayersBeginningTimes::class.java))
    }

    @Test
    fun getTodayPrayers_returns_null_when_there_is_no_data() = runTest {

        val result = databaseRule.database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()

        MatcherAssert.assertThat("database is not null",result, nullValue())
    }

    @Test
    fun updateMaghribJamaah_updates_maghrib_jamaah_time() = runTest{

        //GIVEN - a prayer is inserted in to the database
        databaseRule.database.prayerDao.insertPrayer(prayer)
        var prayerIsStored = databaseRule.database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(prayerIsStored, notNullValue())

        // and, maghrib jamaah is null value initially
        MatcherAssert.assertThat(prayerIsStored?.magribJamaah, nullValue())

        // WHEN - maghrib Jamaah is updated
        databaseRule.database.prayerDao.updateMaghribJamaah("16:17",prayer.date)

        // THEN - verify that the maghrib Jamaah is updated for the same date
        prayerIsStored = databaseRule.database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(prayerIsStored?.magribJamaah, `is`("16:17"))

    }

    @Test
    fun clear_deletes_the_data_from_database() = runTest {

        //GIVEN - a prayer is inserted in the database
        databaseRule.database.prayerDao.insertPrayer(prayer)
        var result = databaseRule.database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(result, notNullValue())

        // WHEN - clear is called
        databaseRule.database.prayerDao.clear()

        // THEN - verify that there is no data in the database
        result = databaseRule.database.prayerDao.getTodayPrayers().asLiveData().getOrAwaitValue()
        MatcherAssert.assertThat(result, nullValue())
    }
}