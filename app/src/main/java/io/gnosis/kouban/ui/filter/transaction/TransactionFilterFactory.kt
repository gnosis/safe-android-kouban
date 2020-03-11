package io.gnosis.kouban.ui.filter.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.ui.adapter.UnsupportedViewType
import io.gnosis.kouban.core.utils.showDatePickerDialog
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.data.managers.TransactionTimestampFilter
import io.gnosis.kouban.data.managers.TransactionTokenSymbolFilter
import io.gnosis.kouban.data.managers.TransactionTypeFilter
import io.gnosis.kouban.data.models.TransactionType
import io.gnosis.kouban.databinding.ItemFilterCheckboxBinding
import io.gnosis.kouban.databinding.ItemFilterDateBinding
import io.gnosis.kouban.databinding.ItemLabelBinding
import io.gnosis.kouban.ui.transaction.details.TransactionTypeViewHolder
import java.text.SimpleDateFormat
import java.util.*

enum class ViewType { TokenSymbol, Date, Type, Label }

class TransactionFilterFactory(
    private val searchManager: SearchManager,
    private val dateFormat: SimpleDateFormat
) :
    BaseFactory<BaseTransactionFilterViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseTransactionFilterViewHolder<Any> {
        return when (viewType) {
            ViewType.Type.ordinal -> TransactionTypeFilterViewHolder(searchManager, viewBinding as ItemFilterCheckboxBinding)
            ViewType.Label.ordinal -> TransactionLabelFilterViewHolder(viewBinding as ItemLabelBinding)
            ViewType.TokenSymbol.ordinal -> TokenSymbolFilterViewHolder(searchManager, viewBinding as ItemFilterCheckboxBinding)
            ViewType.Date.ordinal -> DatePickerFilterViewHolder(viewBinding as ItemFilterDateBinding, searchManager, dateFormat)
            else -> throw UnsupportedViewType("TransactionFilterFactory $viewType")
        } as BaseTransactionFilterViewHolder<Any>
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            ViewType.TokenSymbol.ordinal, ViewType.Type.ordinal -> ItemFilterCheckboxBinding.inflate(layoutInflater, parent, false)
            ViewType.Date.ordinal -> ItemFilterDateBinding.inflate(layoutInflater, parent, false)
            ViewType.Label.ordinal -> ItemLabelBinding.inflate(layoutInflater, parent, false)
            else -> throw UnsupportedViewType("TransactionFilterFactory $viewType")
        }
    }

    override fun <T> viewTypeFor(item: T): Int {
        return when (item) {
            is String -> ViewType.TokenSymbol.ordinal
            is FilterDates -> ViewType.Date.ordinal
            is Int -> ViewType.Label.ordinal
            is TransactionType -> ViewType.Type.ordinal
            else -> throw UnsupportedViewType("TransactionFilterFactory $item")
        }
    }
}

abstract class BaseTransactionFilterViewHolder<T : Any>(
    viewBinding: ViewBinding
) : BaseViewHolder<T>(viewBinding)

class TokenSymbolFilterViewHolder(
    private val searchManager: SearchManager,
    private val viewBinding: ItemFilterCheckboxBinding
) : BaseTransactionFilterViewHolder<String>(viewBinding) {

    override fun bind(item: String) {
        with(viewBinding.checkbox) {
            text = item
            isChecked = searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.contains(item) == true
            setOnClickListener {
                if (isChecked) uncheck(item) else check(item)
            }
        }
    }

    private fun uncheck(item: String) {
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.remove(item)
        viewBinding.checkbox.isChecked = false
        viewBinding.checkbox.invalidate()
    }

    private fun check(item: String) {
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.remove(item)
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.add(item)
        viewBinding.checkbox.isChecked = true
        viewBinding.checkbox.invalidate()
    }
}

data class FilterDates(val lowerBound: Date?, val upperBound: Date?)

class DatePickerFilterViewHolder(
    private val viewBinding: ItemFilterDateBinding,
    private val searchManager: SearchManager,
    private val dateFormat: SimpleDateFormat
) : BaseTransactionFilterViewHolder<FilterDates>(viewBinding) {

    override fun bind(item: FilterDates) {
        setFromDateListener(item)
        setToDateListener(item)
        with(viewBinding) {
            dateFromClearButton.setOnClickListener {
                dateFrom.text = null
                searchManager.getFilterFor(TransactionTimestampFilter::class.java)?.lowerBound = null
            }
            dateToClearButton.setOnClickListener {
                dateTo.text = null
                searchManager.getFilterFor(TransactionTimestampFilter::class.java)?.upperBound = null
            }
        }
    }

    private fun setFromDateListener(item: FilterDates) {
        with(viewBinding.dateFrom) {
            item.lowerBound?.let { text = dateFormat.format(it) }
            setOnClickListener {
                showDatePickerDialog(it.context, text?.toString().toDate()) { date ->
                    text = dateFormat.format(date)
                    searchManager.getFilterFor(TransactionTimestampFilter::class.java)?.lowerBound = date
                }
            }
        }
    }

    private fun setToDateListener(item: FilterDates) {
        with(viewBinding.dateTo) {
            item.upperBound?.let { text = dateFormat.format(it) }
            setOnClickListener {
                showDatePickerDialog(it.context, text?.toString().toDate()) { date ->
                    text = dateFormat.format(date)
                    searchManager.getFilterFor(TransactionTimestampFilter::class.java)?.upperBound = date
                }
            }
        }
    }

    private fun String?.toDate(): Date? = takeUnless { isNullOrEmpty() }?.let { dateFormat.parse(it) }
}

class TransactionTypeFilterViewHolder(
    private val searchManager: SearchManager,
    private val viewBinding: ItemFilterCheckboxBinding
) : BaseTransactionFilterViewHolder<TransactionType>(viewBinding) {

    override fun bind(item: TransactionType) {
        with(viewBinding.checkbox) {
            text = item.name
            isChecked = searchManager.getFilterFor(TransactionTypeFilter::class.java)?.selectedValue?.contains(item) == true
            setOnClickListener {
                if (isChecked) uncheck(item) else check(item)
            }
        }
    }

    private fun uncheck(item: TransactionType) {
        searchManager.getFilterFor(TransactionTypeFilter::class.java)?.selectedValue?.remove(item)
        viewBinding.checkbox.isChecked = false
        viewBinding.checkbox.invalidate()
    }

    private fun check(item: TransactionType) {
        searchManager.getFilterFor(TransactionTypeFilter::class.java)?.selectedValue?.remove(item)
        searchManager.getFilterFor(TransactionTypeFilter::class.java)?.selectedValue?.add(item)
        viewBinding.checkbox.isChecked = true
        viewBinding.checkbox.invalidate()
    }
}

class TransactionLabelFilterViewHolder(
    private val viewBinding: ItemLabelBinding
) : BaseTransactionFilterViewHolder<Int>(viewBinding) {

    override fun bind(item: Int) {
        viewBinding.label.setText(item)
    }
}

