package io.gnosis.kouban.core.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseViewHolder<in T, VB : ViewBinding>(
    private val viewBinding: VB
) : RecyclerView.ViewHolder(viewBinding.root) {

    abstract fun bind(item: T)

//    fun <U> binds(item: U): Boolean = (item as? T) != null
}
