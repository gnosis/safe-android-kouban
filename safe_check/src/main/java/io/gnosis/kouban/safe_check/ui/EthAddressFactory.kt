package io.gnosis.kouban.safe_check.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.safe_check.databinding.ItemEthAddressBinding
import pm.gnosis.model.Solidity

class EthAddressFactory(private val picasso: Picasso) : BaseFactory<ItemEthAddressBinding, EthAddressViewHolder>() {
    override fun newViewHolder(viewBinding: ItemEthAddressBinding, viewType: Int): EthAddressViewHolder {
        return EthAddressViewHolder(viewBinding, picasso)
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemEthAddressBinding {
        return ItemEthAddressBinding.inflate(layoutInflater, parent, false)
    }
}

class EthAddressViewHolder(private val binding: ItemEthAddressBinding, private val picasso: Picasso) :
    BaseViewHolder<Solidity.Address, ItemEthAddressBinding>(binding) {

    override fun bind(owner: Solidity.Address) {
        binding.ownerAddress.text = ""
        binding.ownerAddressImage.setAddress(owner)
    }
}
