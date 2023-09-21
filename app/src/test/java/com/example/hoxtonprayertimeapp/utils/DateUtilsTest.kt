package com.example.hoxtonprayertimeapp.utils

import androidx.test.filters.SmallTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@SmallTest
class DateUtilsTest {

    @Test
    fun getTodayDate_returnsTodayDate() {

        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-08T00:00:00.00Z"), ZoneId.systemDefault())
        val localDate = LocalDate.now(fixedClock)

        //WHEN - getTodayDate is invoked
        val result  = getTodayDate(localDate)

        //THEN - assert that it returns the same date 2023-08-08
        val expectedDate = "2023-08-08"
        MatcherAssert.assertThat("The actual $result date does not match expected date: $expectedDate",result, `is`(expectedDate))

    }

    @Test
    fun isTodayFriday_when_today_is_tuesday_returns_false() {

        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-08T00:00:00.00Z"), ZoneId.systemDefault())
        val local = LocalDate.now(fixedClock)

        // WHEN - isTodayFriday is invoked
        val result = isTodayFriday(local)

        //THEN - verify it returns false i.e. it is not Friday
        MatcherAssert.assertThat(result, `is`(false))
    }

    @Test
    fun isTodayFriday_when_today_is_friday_returns_true() {

        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-11T00:00:00.00Z"), ZoneId.systemDefault())
        val local = LocalDate.now(fixedClock)

        // WHEN - isTodayFriday is invoked
        val result = isTodayFriday(local)

        //THEN - verify it returns false i.e. it is not Friday
        MatcherAssert.assertThat(result, `is`(true))
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
        MatcherAssert.assertThat(result, `is`(expectedYesterday))
    }

    @Test
    fun getMostRecentFriday_when_today_is_tuesday_returns_last_friday() {
        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-08T00:00:00.00Z"), ZoneId.systemDefault())

        //WHEN - getLastOrTodayFridayDate is invoked
        val result  = getMostRecentFriday(fixedClock)

        //THEN - assert that the date returned is Fri 4th Aug
        val expectedLastFridayDate = "2023-08-04"
        MatcherAssert.assertThat(result, `is`(expectedLastFridayDate))
    }

    @Test
    fun getMostRecentFriday_when_today_is_friday_returns_same_friday_date() {

        //GIVEN - a fixed date of Fri 15th Sept 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-09-15T00:00:00.00Z"), ZoneId.systemDefault())

        // WHEN - getLastOrTodayFridayDate is invoked
        val result  = getMostRecentFriday(fixedClock)

        // THEN - assert that the date returned is 15th Sept 2023
        val expectedFridayDate = "2023-09-15"
        MatcherAssert.assertThat(result, `is`(expectedFridayDate))
    }
}