package io.gnosis.kouban.safe_check.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.utils.formatEthAddress
import io.gnosis.kouban.safe_check.databinding.ItemEthAddressBinding
import pm.gnosis.model.Solidity

class EthAddressFactory(private val picasso: Picasso) : BaseFactory<EthAddressViewHolder>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int): EthAddressViewHolder {
        return EthAddressViewHolder(viewBinding as ItemEthAddressBinding, picasso)
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return ItemEthAddressBinding.inflate(layoutInflater, parent, false)
    }
}

class EthAddressViewHolder(private val binding: ItemEthAddressBinding, private val picasso: Picasso) :
    BaseViewHolder<Solidity.Address>(binding) {

    override fun bind(owner: Solidity.Address) {
        binding.ownerAddress.text = owner.formatEthAddress(binding.root.context)
        binding.ownerAddressImage.setAddress(owner)
    }
}
