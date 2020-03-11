package io.gnosis.kouban.ui.filter.transaction

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.ui.adapter.UnsupportedViewType
import io.gnosis.kouban.core.utils.showDatePickerDialog
import io.gnosis.kouban.core.utils.underline
import io.gnosis.kouban.data.managers.SearchManager
import io.gnosis.kouban.data.managers.TransactionTokenSymbolFilter
import io.gnosis.kouban.databinding.ItemFilterDateBinding
import io.gnosis.kouban.databinding.ItemFilterTokenSymbolBinding
import pm.gnosis.svalinn.common.utils.snackbar
import java.text.SimpleDateFormat
import java.util.*

enum class ViewType { TokenSymbol, Date }

class TransactionFilterFactory(
    private val searchManager: SearchManager,
    private val dateFormat: SimpleDateFormat
) :
    BaseFactory<BaseTransactionFilterViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseTransactionFilterViewHolder<Any> {
        return when (viewType) {
            ViewType.TokenSymbol.ordinal -> TokenSymbolFilterViewHolder(searchManager, viewBinding as ItemFilterTokenSymbolBinding)
            ViewType.Date.ordinal -> DatePickerFilterViewHolder(viewBinding as ItemFilterDateBinding, dateFormat)
            else -> throw UnsupportedViewType("TransactionFilterFactory $viewType")
        } as BaseTransactionFilterViewHolder<Any>
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            ViewType.TokenSymbol.ordinal -> ItemFilterTokenSymbolBinding.inflate(layoutInflater, parent, false)
            ViewType.Date.ordinal -> ItemFilterDateBinding.inflate(layoutInflater, parent, false)
            else -> throw UnsupportedViewType("TransactionFilterFactory $viewType")
        }
    }

    override fun <T> viewTypeFor(item: T): Int {
        return when (item) {
            is String -> ViewType.TokenSymbol.ordinal
            is FilterDates -> ViewType.Date.ordinal
            else -> throw UnsupportedViewType("TransactionFilterFactory $item")
        }
    }
}

abstract class BaseTransactionFilterViewHolder<T : Any>(
    viewBinding: ViewBinding
) : BaseViewHolder<T>(viewBinding)

class TokenSymbolFilterViewHolder(
    private val searchManager: SearchManager,
    private val viewBinding: ItemFilterTokenSymbolBinding
) : BaseTransactionFilterViewHolder<String>(viewBinding) {

    override fun bind(item: String) {
        with(viewBinding.tokenSymbolCheckbox) {
            text = item
            isChecked = searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.contains(item) == true
            setOnClickListener {
                if (isChecked) uncheck(item) else check(item)
            }
        }
    }

    private fun uncheck(item: String) {
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.remove(item)
        viewBinding.tokenSymbolCheckbox.isChecked = false
        viewBinding.tokenSymbolCheckbox.invalidate()
    }

    private fun check(item: String) {
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.remove(item)
        searchManager.getFilterFor(TransactionTokenSymbolFilter::class.java)?.selectedValue?.add(item)
        viewBinding.tokenSymbolCheckbox.isChecked = true
        viewBinding.tokenSymbolCheckbox.invalidate()
    }
}

data class FilterDates(val lowerBound: Date?, val upperBound: Date?)

class DatePickerFilterViewHolder(
    private val viewBinding: ItemFilterDateBinding,
    private val dateFormat: SimpleDateFormat
) : BaseTransactionFilterViewHolder<FilterDates>(viewBinding) {

    override fun bind(item: FilterDates) {
        setFromDateListener(item)
        with(viewBinding) {
            dateFromClearButton.setOnClickListener { dateFrom.text = null }
        }
    }

    private fun setFromDateListener(item: FilterDates) {
        with(viewBinding.dateFrom) {
            text = item.lowerBound?.toLocaleString()
            setOnClickListener {
                showDatePickerDialog(it.context, text?.toString().toDate()) { date ->
                    text = dateFormat.format(date).underline()
                }
            }
        }
    }

    private fun String?.toDate(): Date? = takeUnless { isNullOrEmpty() }?.let { dateFormat.parse(it) }
}
