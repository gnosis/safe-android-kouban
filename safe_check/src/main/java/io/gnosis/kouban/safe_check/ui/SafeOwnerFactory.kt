package io.gnosis.kouban.safe_check.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.safe_check.databinding.ItemSafeOwnerBinding
import pm.gnosis.model.Solidity

class SafeOwnerFactory(private val picasso: Picasso) : BaseFactory<ItemSafeOwnerBinding, SafeOwnerViewHolder>() {
    override fun newViewHolder(viewBinding: ItemSafeOwnerBinding, viewType: Int): SafeOwnerViewHolder {
        return SafeOwnerViewHolder(viewBinding, picasso)
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemSafeOwnerBinding {
        return ItemSafeOwnerBinding.inflate(layoutInflater, parent, false)
    }
}

class SafeOwnerViewHolder(private val binding: ItemSafeOwnerBinding, private val picasso: Picasso) :
    BaseViewHolder<Solidity.Address, ItemSafeOwnerBinding>(binding) {

    override fun bind(owner: Solidity.Address) {
        binding.ownerAddress.text = ""
        binding.ownerAddressImage.setAddress(owner)
    }
}
