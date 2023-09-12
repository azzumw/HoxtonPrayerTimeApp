package com.example.hoxtonprayertimeapp

import androidx.test.filters.SmallTest
import com.example.hoxtonprayertimeapp.utils.getYesterdayDate
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(MockitoJUnitRunner::class)
@SmallTest
class DateUtilsUnitTest {

    @Mock
    private lateinit var mockedDate:LocalDate
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun getYesterdayDate() {



//        val add = mockedCalendar.add(Calendar.MONTH,-1)
        mockedDate = mock(LocalDate::class.java)

//        `when`(mockedDate.month).thenReturn(Month.SEPTEMBER)
//        `when`(mockedDate.dayOfMonth).thenReturn(12)
//        `when`(mockedDate.year).thenReturn(2023)

        val time= getYesterdayDate(mockedDate)

//        val yesterday = getYesterdayDate(mockedCalendar)
//
        assertEquals("2023-09-11",time)
    }
}