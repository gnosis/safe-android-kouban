package io.gnosis.kouban.core.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class BaseAdapter<T, VB, VH>(
    private val factory: BaseFactory<VB, VH>
) : RecyclerView.Adapter<VH>() where VH : BaseViewHolder<T, VB>, VB : ViewBinding {

    private val items = mutableListOf<T>()

    @Deprecated("Unsafe")
    fun setItemsUnsafe(items: List<T>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        factory.newViewHolder(
            factory.layout(LayoutInflater.from(parent.context), parent, viewType),
            viewType
        )


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
//        holder.itemViewType
        holder.bind(items[position])
    }
}
