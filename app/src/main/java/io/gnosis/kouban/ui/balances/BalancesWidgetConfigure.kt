package io.gnosis.kouban.ui.balances

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.core.utils.setTransactionIcon
import io.gnosis.kouban.data.models.Balance
import io.gnosis.kouban.data.models.TokenInfo
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.databinding.ItemTokenBinding
import io.gnosis.kouban.databinding.WidgetBalancesConfigureBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import pm.gnosis.model.Solidity

class BalancesWidgetConfigure : AppCompatActivity() {

    private val viewModel by currentScope.viewModel<BalancesViewModel>(this)
    private val adapter by currentScope.inject<BaseAdapter<BalanceItemViewHolder, Balance>>()

    private val binding by lazy { WidgetBalancesConfigureBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        with(binding) {
            list.layoutManager = LinearLayoutManager(this@BalancesWidgetConfigure)
            list.adapter = adapter

            swipeToRefresh.setOnRefreshListener {
                viewModel.loadBalancesForCurrentSafe()
            }
        }

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Set the Activity result to RESULT_CANCELED, along with EXTRA_APPWIDGET_ID.
        // This way, if the user backs-out of the Activity before reaching the end,
        // the App Widget host is notified that the configuration was cancelled and
        // the App Widget will not be added.
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        })

        viewModel.init()
        viewModel.events.observe(this, Observer {

            when(it) {

                is TokenBalances -> {
                    adapter.setItemsUnsafe(it.balances)
                }

                is TokenSelection -> {

                }
            }
        })




//        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
//
//
//        RemoteViews(context.packageName, R.layout.widget_balances).also { views ->
//            appWidgetManager.updateAppWidget(appWidgetId, views)
//        }


        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        // finish()
    }


}

class BalancesItemFactory(
    private val picasso: Picasso
) : BaseFactory<BalanceItemViewHolder>() {

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int) =
        BalanceItemViewHolder(viewBinding as ItemTokenBinding, picasso)

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        ItemTokenBinding.inflate(layoutInflater, parent, false)
}

class BalanceItemViewHolder(
    private val binding: ItemTokenBinding,
    private val picasso: Picasso) : BaseViewHolder<Balance>(binding) {

    override fun bind(item: Balance) {
        with(binding) {
            tokenItemSymbol.text = item.tokenInfo.symbol
            tokenItemInfo.text = item.balance.shiftedString(item.tokenInfo.decimals, 5)
            tokenItemIcon.setTransactionIcon(picasso, item.tokenInfo.icon)
        }
    }
}


class BalancesViewModel(
    private val safeRepository: SafeRepository,
    private val addressManager: SafeAddressManager
) : ViewModel() {

    val events = MutableLiveData<ViewState>()
    private lateinit var safeAddress: Solidity.Address
    private var selectedToken: TokenInfo? = null

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                safeAddress = addressManager.getSafeAddress()!!
            }
                .onSuccess {
                    loadBalancesForCurrentSafe()
                }
                .onFailure {
                    events.postValue(Error(NoSafeAddressSet()))
                }
        }
    }

    fun loadBalancesForCurrentSafe() {
        viewModelScope.launch(Dispatchers.IO) {

            events.postValue(Loading(true))

            kotlin.runCatching {
                safeRepository.loadTokenBalances(safeAddress)
            }
                .onSuccess {
                    events.postValue(Loading(false))
                    events.postValue(TokenBalances(it))
                }
                .onFailure {
                    events.postValue(Loading(false))
                    events.postValue(Error(it))
                }
        }
    }

    fun onTokenSelected(token: TokenInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedToken = token
            events.postValue(TokenSelection(token))
        }
    }

    fun onTokenSelectionCanceled() {
        viewModelScope.launch(Dispatchers.IO) {
            events.postValue(TokenSelectionCanceled())
        }
    }

    fun onTokenSelectionSubmitted() {
        viewModelScope.launch(Dispatchers.IO) {
            events.postValue(TokenSelectionSubmitted(selectedToken!!))
        }
    }
}

data class TokenBalances(
    val balances: List<Balance>
) : ViewState()

data class TokenSelection(
    val token: TokenInfo
) : ViewState()

data class TokenSelectionSubmitted(
    val token: TokenInfo
) : ViewState()

class TokenSelectionCanceled : ViewState()

class NoSafeAddressSet : Throwable()


