package io.gnosis.kouban.core.ui.safe.balance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.databinding.ItemTokenBalanceBinding
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.utils.setTransactionIcon
import io.gnosis.kouban.data.models.Balance
import io.gnosis.kouban.data.utils.shiftedString

class BalanceFactory(private val picasso: Picasso) : BaseFactory<ItemTokenBalanceBinding, BalanceViewHolder>() {
    override fun newViewHolder(viewBinding: ItemTokenBalanceBinding, viewType: Int): BalanceViewHolder {
        return BalanceViewHolder(viewBinding, picasso)
    }

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemTokenBalanceBinding {
        return ItemTokenBalanceBinding.inflate(layoutInflater, parent, false)
    }
}

class BalanceViewHolder(private val binding: ItemTokenBalanceBinding, private val picasso: Picasso) :
    BaseViewHolder<Balance, ItemTokenBalanceBinding>(binding) {

    override fun bind(balanceInfo: Balance) {
        binding.icon.setTransactionIcon(picasso, balanceInfo.tokenInfo.icon)
        binding.token.text = balanceInfo.tokenInfo.symbol
        binding.amount.text = balanceInfo.balance.shiftedString(balanceInfo.tokenInfo.decimals)
        binding.amountUsd.isVisible = balanceInfo.usdBalance != null
        balanceInfo.usdBalance?.let {
            binding.amountUsd.text = "$${it.toPlainString()}"
        }
    }
}
