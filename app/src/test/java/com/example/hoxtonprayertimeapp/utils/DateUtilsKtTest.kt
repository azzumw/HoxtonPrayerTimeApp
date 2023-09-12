package com.example.hoxtonprayertimeapp.utils

import androidx.test.filters.SmallTest
import org.junit.Assert.*

import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@SmallTest
class DateUtilsKtTest {

    @Test
    fun getTodayDate_returnsTodayDate() {

        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-08T00:00:00.00Z"), ZoneId.systemDefault())
        val local = LocalDate.now(fixedClock)

        //WHEN - getTodayDate is invoked
        val result  = getTodayDate(local)

        //THEN - assert that it returns the same date 2023-08-08
        val expectedDate = "2023-08-08"
        assertEquals(expectedDate,result)

    }

    @Test
    fun isTodayFriday_todayTuesday_False() {

        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-08T00:00:00.00Z"), ZoneId.systemDefault())
        val local = LocalDate.now(fixedClock)

        // WHEN - isTodayFriday is invoked
        val result = isTodayFriday(local)

        //THEN - verify it returns false i.e. it is not Friday
        assertFalse(result)
    }

    @Test
    fun isTodayFriday_todayFriday_True() {

        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-11T00:00:00.00Z"), ZoneId.systemDefault())
        val local = LocalDate.now(fixedClock)

        // WHEN - isTodayFriday is invoked
        val result = isTodayFriday(local)

        //THEN - verify it returns false i.e. it is not Friday
        assertTrue(result)
    }

    @Test
    fun getYesterdayDate_returnsYesterdayDate() {

        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-08T00:00:00.00Z"), ZoneId.systemDefault())
        val local = LocalDate.now(fixedClock)

        // WHEN - getYesterday is invoked
        val result  = getYesterdayDate(local)
        val expectedYesterday = "2023-08-07"

        // THEN - verify that the date returned is 7th Aug 2023
        assertEquals(expectedYesterday,result)

    }

    @Test
    fun getLastOrTodayFridayDate_todayDate_lastFriday() {
        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-08T00:00:00.00Z"), ZoneId.systemDefault())

        //WHEN - getLastOrTodayFridayDate is invoked
        val result  = getLastOrTodayFridayDate(fixedClock)

        //THEN - assert that the date returned is Fri 4th Aug
        val expectedLastFridayDate = "2023-08-04"
        assertEquals(expectedLastFridayDate,result)
    }

    @Test
    fun getLastOrTodayFridayDate_futureFridayDate_sameFridayDate() {

        //GIVEN - a fixed date of Fri 15th Sept 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-09-15T00:00:00.00Z"), ZoneId.systemDefault())

        // WHEN - getLastOrTodayFridayDate is invoked
        val result  = getLastOrTodayFridayDate(fixedClock)

        // THEN - assert that the date returned is 15th Sept 2023
        val expectedFridayDate = "2023-09-15"
        assertEquals(expectedFridayDate,result)
    }
}