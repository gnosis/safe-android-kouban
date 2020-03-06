package io.gnosis.kouban.core.managers

import io.gnosis.kouban.data.managers.BoundaryFilter
import io.gnosis.kouban.data.managers.SearchManager
import org.junit.Test

class SearchManagerTest {

    @Test
    fun `searchManager - filter integers greater than threshold`() {
        val searchManager = SearchManager()
        searchManager.activateFilter(object : BoundaryFilter<Int>() {
            override val lowerBound: Int = 2
            override val upperBound: Int? = null

            override fun apply(item: Int): Boolean = item > lowerBound

            override fun clear() {
                //no-op
            }
        })

        val input = listOf(1, 2, 3)
        val result = searchManager.filter(input)

        assert(result == listOf(3))
    }
}
