package io.gnosis.kouban.ui.transaction.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder

class TransactionDetailFactory : BaseFactory<BaseDetailViewHolder<Any>>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): BaseDetailViewHolder<Any> {

    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {

    }

    override fun <T> viewTypeFor(item: T): Int {

    }

    private enum class ViewType {
        LabelWithAddress
    }
}

abstract class BaseDetailViewHolder<T>(viewBinding: ViewBinding) : BaseViewHolder<T>(viewBinding)
