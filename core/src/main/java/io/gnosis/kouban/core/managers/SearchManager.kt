package io.gnosis.kouban.core.managers

import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.data.models.TransactionType

class SearchManager {

    private val activeFilters = mutableListOf<Filter<Any>>()

    fun <Z : Any, T : Filter<Z>> activateFilter(newFilter: T) {
        activeFilters.add(newFilter as Filter<Any>)
    }

    fun <Z : Any> applyDiff(input: List<Z>) =
        activeFilters.fold(input) { filteredInput, filter ->
            filteredInput.filter { filter.apply(it) }
        }
}

interface Filter<in T> {
    fun apply(item: T): Boolean
}


abstract class SelectionFilter<in T> : Filter<T> {
    abstract val availableValues: List<*>
    abstract val selectedValue: MutableList<*>
}

abstract class BoundaryFilter<in T> : Filter<T> {
    abstract val lowerBound: Any?
    abstract val upperBound: Any?
}

data class TransactionTokenSymbolFilter(
    override val availableValues: List<String>,
    override val selectedValue: MutableList<String>
) : SelectionFilter<Transaction>() {

    override fun apply(item: Transaction): Boolean =
        selectedValue.isNotEmpty()
                && item.transferInfo != null
                && selectedValue.contains(item.transferInfo?.tokenSymbol)
}
