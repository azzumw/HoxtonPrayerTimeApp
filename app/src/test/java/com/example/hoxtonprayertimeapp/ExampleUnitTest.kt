package com.example.hoxtonprayertimeapp

import androidx.test.filters.SmallTest
import com.example.hoxtonprayertimeapp.utils.getYesterdayDate
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito.mock
import java.util.Calendar

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@SmallTest
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun getYesterdayDate() {
        val mockedCalender = mock<Calendar>()
        mockedCalender.set(2023,8,26)

        val yesterday = getYesterdayDate(mockedCalender)
        assertEquals(yesterday,"25082023")
    }
}