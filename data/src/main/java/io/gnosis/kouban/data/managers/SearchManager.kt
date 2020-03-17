package io.gnosis.kouban.data.managers

import io.gnosis.kouban.data.models.Transaction
import io.gnosis.kouban.data.models.TransactionType
import io.gnosis.kouban.data.repositories.TokenRepository
import io.gnosis.kouban.data.utils.onlyDate
import java.util.*

class SearchManager {

    private val activeFilters = mutableListOf<Filter<Any>>()

    fun <T : Any, F : Filter<T>> activateFilter(newFilter: F) {
        if (activeFilters.find { it.type() == newFilter.type() } == null) {
            activeFilters.add(newFilter as Filter<Any>)
        }
    }

    fun <T : Any> filter(input: List<T>) =
        activeFilters.takeUnless { it.isEmpty() }
            ?.fold(input) { filteredInput, filter ->
                filteredInput.filter { filter.apply(it) }
            }
            ?: input

    fun clearAllFilters() = activeFilters.forEach(Filter<Any>::clear)

    fun <T : Any, F : Filter<T>> getFilterFor(type: Class<F>): F? {
        return activeFilters.find { it.type() == type } as F?
    }

}

interface Filter<in T> {

    fun apply(item: T): Boolean

    fun clear()

    fun type() = this::class.java
}


abstract class SelectionFilter<in T> : Filter<T> {
    abstract val availableValues: List<*>
    abstract val selectedValue: MutableList<*>

    override fun clear() {
        selectedValue.clear()
    }
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
                && (item.isDataInfoAvailable() || item.usesSelectedToken())

    private fun Transaction.isDataInfoAvailable() = this.dataInfo != null && selectedValue.contains(TokenRepository.ETH_TOKEN_INFO.symbol)

    private fun Transaction.usesSelectedToken(): Boolean =
        selectedValue.contains(this.transferInfo?.tokenSymbol)

}

data class TransactionTimestampFilter(
    override var lowerBound: Date?,
    override var upperBound: Date?

) : BoundaryFilter<Transaction>() {

    override fun apply(item: Transaction): Boolean {
        return with(item.timestampAsDate().onlyDate()) {
            (upperBound == null || upperBound?.onlyDate() == this || before(upperBound))
                    && (lowerBound == null || lowerBound?.onlyDate() == this || after(lowerBound))
        }
    }

    override fun clear() {
        lowerBound = null
        upperBound = null
    }
}


data class TransactionTypeFilter(
    override val availableValues: List<TransactionType>,
    override val selectedValue: MutableList<TransactionType>
) : SelectionFilter<Transaction>() {

    override fun apply(item: Transaction): Boolean =
        selectedValue.isNotEmpty() && selectedValue.contains(item.type)

}
