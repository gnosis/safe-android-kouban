package io.gnosis.kouban.core.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseFactory<VB, out VH>
        where VB : ViewBinding, VH : BaseViewHolder<*, VB> {

    abstract fun newViewHolder(viewBinding: VB, viewType: Int): VH

    abstract fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB
}
