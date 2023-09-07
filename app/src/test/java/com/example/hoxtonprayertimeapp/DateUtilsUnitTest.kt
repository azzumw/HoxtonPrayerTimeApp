package com.example.hoxtonprayertimeapp

import androidx.test.filters.SmallTest
import com.example.hoxtonprayertimeapp.utils.getYesterdayDate
import org.junit.Test

import org.junit.Assert.*
import org.junit.Ignore
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Calendar

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@SmallTest
class DateUtilsUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Ignore("issue with the Calender mocking")
    fun getYesterdayDate() {

       val mockedCalendar = mock<Calendar>()
        `when`(mockedCalendar.get(Calendar.MONTH)).thenReturn(8)
        `when`(mockedCalendar.get(Calendar.DATE)).thenReturn(8)
        `when`(mockedCalendar.get(Calendar.YEAR)).thenReturn(2023)

        val yesterday = getYesterdayDate(mockedCalendar)
//
        assertEquals(yesterday,"")
    }
}