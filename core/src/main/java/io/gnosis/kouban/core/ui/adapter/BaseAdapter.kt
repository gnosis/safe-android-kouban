package io.gnosis.kouban.core.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class BaseAdapter<VH>(
    private val factory: BaseFactory<VH>
) : RecyclerView.Adapter<VH>() where VH : BaseViewHolder<Any> {

    private val items = mutableListOf<Any>()

    @Deprecated("Unsafe")
    fun setItemsUnsafe(items: List<Any>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        factory.newViewHolder(
            factory.layout(LayoutInflater.from(parent.context), parent, viewType),
            viewType
        )

    override fun getItemViewType(position: Int): Int =
        factory.viewTypeFor(items[position]).takeUnless { it < 0 } ?: throw UnsupportedItem()


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }
}

class UnsupportedItem : Throwable()
