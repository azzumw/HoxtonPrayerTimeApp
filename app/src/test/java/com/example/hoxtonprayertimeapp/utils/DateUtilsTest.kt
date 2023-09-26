package com.example.hoxtonprayertimeapp.utils

import androidx.test.filters.SmallTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId


@SmallTest
class DateUtilsTest {

    @Test
    fun getCurrentGregorianDate_returns_today_date_in_format() {
        //GIVEN - a fixed date of Tue 8th August 2023
        val fixedClock = Clock.fixed(Instant.parse("2023-08-08T00:00:00.00Z"), ZoneId.systemDefault())
        val localDate = LocalDate.now(fixedClock)

        // WHEN - getCurrentGregorianDate() is invoked
        val result = getCurrentGregorianDate(localDate)

        //THEN - assert that it returns the same date in the format EEE dd MMM yyyy
        val expectedDate = "Tue 08 Aug 2023"
        MatcherAssert.assertThat(result, `is`(expectedDate))

    }

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

        //GIVEN - a fixed date of Fri 11th August 2023
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

    @Test
    fun fromStringToLocalTime_when_no_mins_are_added_returns_the_same_time_in_LocalTime() {
        // GIVEN - a ISO format time of 14:15 in string
        val timeInString = "14:45"

        // WHEN - fromStringtoLocalTime is invoked with 0 minutes added
        val resultTime = fromStringToLocalTime(timeInString)

        // THEN - assert that the time is converted to the LocalTime object
        val expectedTime = LocalTime.of(14,45)
        MatcherAssert.assertThat(resultTime, `is`(expectedTime))
    }

    @Test
    fun fromStringToLocalTime_when_5_mins_are_added_returns_the_time_in_LocalTime_with_5_mins_added() {
        // GIVEN - a ISO format time of 14:15 in string
        val timeInString = "14:45"

        // WHEN - fromStringtoLocalTime is invoked with 5 minutes added
        val resultTime = fromStringToLocalTime(timeInString,5L)

        // THEN - assert that the time is converted to the LocalTime object
        val expectedTime = LocalTime.of(14,50)
        MatcherAssert.assertThat(resultTime, `is`(expectedTime))
    }

    @Test
    fun fromLocalTimeToString_when_time_is_before_noon_returns_12_hour_format() {
        // GIVEN - a local time in 24 hour format before noon
        val localTime = LocalTime.of(5,15)

        // WHEN - fromLocalTimeToString is invoked
        val resultTimeInString = fromLocalTimeToString(localTime)

        // THEN - assert that the time returned is correctly shown
        val expectedResult = "05:15 am"
        MatcherAssert.assertThat(resultTimeInString, `is`(expectedResult))

    }

    @Test
    fun fromLocalTimeToString_when_time_is_after_noon_returns_12_hour_format() {
        // GIVEN - a local time in 24 hour format afternoon
        val localTime = LocalTime.of(15,15)

        // WHEN - fromLocalTimeToString is invoked
        val resultTimeInString = fromLocalTimeToString(localTime)

        // THEN - assert that the time returned is correctly shown
        val expectedResult = "03:15 pm"
        MatcherAssert.assertThat(resultTimeInString, `is`(expectedResult))

    }
}