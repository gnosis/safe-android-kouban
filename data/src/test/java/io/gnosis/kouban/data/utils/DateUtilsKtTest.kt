package io.gnosis.kouban.data.utils

import org.junit.Test

import java.util.*

class DateUtilsKtTest {

    @Test
    fun `afterOrEqual - same date`() {
        val date = Calendar.getInstance().time.onlyDate()

        assert(date.afterOrEqual(date) == true)
    }

    @Test
    fun `afterOrEqual - date in the future`() {
        val date = Calendar.getInstance().time.onlyDate()
        val futureDate = dateNextMonth(date).onlyDate()

        assert(futureDate.afterOrEqual(date) == true)
    }

    @Test
    fun `afterOrEqual - compare to null`() {
        val date = Calendar.getInstance().time.onlyDate()

        assert(date.afterOrEqual(null) == false)
    }

    @Test
    fun `beforeOrEqual - same date`() {
        val date = Calendar.getInstance().time.onlyDate()

        assert(date.beforeOrEqual(date) == true)
    }

    @Test
    fun `beforeOrEqual - date in the past`() {
        val date = Calendar.getInstance().time.onlyDate()
        val futureDate = dateNextMonth(date).onlyDate()

        assert(date.beforeOrEqual(futureDate) == true)
    }

    @Test
    fun `beforeOrEqual - compare to null`() {
        val date = Calendar.getInstance().time.onlyDate()

        assert(date.beforeOrEqual(null) == false)
    }

    private fun dateNextMonth(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            add(Calendar.MONTH, 1)
        }.time
    }

}
