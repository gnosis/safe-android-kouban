package io.gnosis.kouban.ui.balances

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import io.gnosis.kouban.R
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.adapter.BaseAdapter
import io.gnosis.kouban.core.ui.adapter.BaseFactory
import io.gnosis.kouban.core.ui.adapter.BaseViewHolder
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.core.utils.setTransactionIcon
import io.gnosis.kouban.data.models.Balance
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.repositories.TokenRepository
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.databinding.ItemTokenBinding
import io.gnosis.kouban.databinding.WidgetBalancesConfigureBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.getColorCompat
import pm.gnosis.svalinn.common.utils.snackbar

class BalancesWidgetConfigure : AppCompatActivity(), BalancesItemFactory.OnTokenClickedListener {

    private val viewModel by currentScope.viewModel<BalancesViewModel>(this)
    private val adapter by currentScope.inject<BaseAdapter<BalanceItemViewHolder, Balance>> { parametersOf(this) }

    private val binding by lazy { WidgetBalancesConfigureBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Set the Activity result to RESULT_CANCELED, along with EXTRA_APPWIDGET_ID.
        // This way, if the user backs-out of the Activity before reaching the end,
        // the App Widget host is notified that the configuration was cancelled and
        // the App Widget will not be added.
        setResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        })

        with(binding) {
            list.layoutManager = LinearLayoutManager(this@BalancesWidgetConfigure)
            list.adapter = adapter

            swipeToRefresh.setOnRefreshListener {
                viewModel.loadBalancesForCurrentSafe()
            }

            fab.setColorFilter(getColorCompat(R.color.white), PorterDuff.Mode.SRC_IN)
            fab.setOnClickListener {
                viewModel.onTokenSelectionSubmitted()
            }
        }

        viewModel.init(appWidgetId)
        viewModel.loadingEvents.observe(this, Observer {
            binding.swipeToRefresh.isRefreshing = it.isLoading
        })
        viewModel.events.observe(this, Observer {

            when (it) {

                is Error -> {
                    if (it.throwable is NoSafeAddressSet) {
                        snackbar(binding.root, getString(R.string.error_address_not_set))
                    } else {
                        snackbar(binding.root, getString(R.string.error_widget_configure))
                    }
                    finish()
                }

                is TokenBalances -> {
                    adapter.setItemsUnsafe(it.balances)
                }

                is TokenSelection -> {
                    with(binding) {
                        if (it.token != null) {
                            fab.isEnabled = true
                            fab.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.colorPrimary))

                        } else {
                            fab.isEnabled = false
                            fab.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.dark_grey))
                        }
                    }
                }

                is TokenSelectionSubmitted -> {

                    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)
                    RemoteViews(packageName, R.layout.widget_balances).also { views ->
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }

                    val resultValue = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    override fun onTokenClicked(token: TokenRepository.TokenInfo?) {
        viewModel.onTokenSelected(token)
    }
}

class BalancesItemFactory(
    private val picasso: Picasso,
    private val tokenClickListener: OnTokenClickedListener
) : BaseFactory<BalanceItemViewHolder>() {

    interface OnTokenClickedListener {
        fun onTokenClicked(token: TokenRepository.TokenInfo?)
    }

    override fun newViewHolder(viewBinding: ViewBinding, viewType: Int) =
        BalanceItemViewHolder(viewBinding as ItemTokenBinding, picasso, tokenClickListener)

    override fun layout(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) =
        ItemTokenBinding.inflate(layoutInflater, parent, false)
}

class BalanceItemViewHolder(
    private val binding: ItemTokenBinding,
    private val picasso: Picasso,
    private val tokenClickListener: BalancesItemFactory.OnTokenClickedListener
) : BaseViewHolder<Balance>(binding) {

    override fun bind(item: Balance) {
        with(binding) {
            tokenItemSymbol.text = item.tokenInfo.symbol
            tokenItemInfo.text = item.balance.shiftedString(item.tokenInfo.decimals, 5)
            tokenItemIcon.setTransactionIcon(picasso, item.tokenInfo.icon)

            root.setOnClickListener {
                tokenItemRadio.isChecked = !tokenItemRadio.isChecked
                tokenClickListener.onTokenClicked(if (tokenItemRadio.isChecked) item.tokenInfo else null)
            }
        }
    }
}


class BalancesViewModel(
    private val safeRepository: SafeRepository,
    private val addressManager: SafeAddressManager,
    private val widgetPrefs: BalancesWidgetPrefs
) : ViewModel() {

    val events = MutableLiveData<ViewState>()
    val loadingEvents = MutableLiveData<Loading>()

    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var safeAddress: Solidity.Address
    private var selectedToken: TokenRepository.TokenInfo? = null

    fun init(widgetId: Int) {
        this.widgetId = widgetId
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

            loadingEvents.postValue(Loading(true))

            kotlin.runCatching {
                safeRepository.loadTokenBalances(safeAddress)
            }
                .onSuccess {
                    loadingEvents.postValue(Loading(false))
                    events.postValue(TokenBalances(it))
                }
                .onFailure {
                    loadingEvents.postValue(Loading(false))
                    events.postValue(Error(it))
                }
        }
    }

    fun onTokenSelected(token: TokenRepository.TokenInfo?) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedToken = token
            events.postValue(TokenSelection(token))
        }
    }

    fun onTokenSelectionSubmitted() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedToken?.let {
                widgetPrefs.saveTokenForWidget(it.address, widgetId)
                events.postValue(TokenSelectionSubmitted(it))
            }
        }
    }
}

data class TokenBalances(
    val balances: List<Balance>
) : ViewState()

data class TokenSelectionSubmitted(
    val token: TokenRepository.TokenInfo
) : ViewState()

data class TokenSelection(
    val token: TokenRepository.TokenInfo?
) : ViewState()

class NoSafeAddressSet : Throwable()


