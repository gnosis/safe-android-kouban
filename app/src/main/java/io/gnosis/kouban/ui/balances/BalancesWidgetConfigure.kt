package io.gnosis.kouban.ui.balances

import android.app.Activity
import android.app.PendingIntent
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
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.gnosis.kouban.R
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.core.ui.base.Error
import io.gnosis.kouban.core.ui.base.Loading
import io.gnosis.kouban.core.ui.base.ViewState
import io.gnosis.kouban.core.utils.setTransactionIcon
import io.gnosis.kouban.data.models.Balance
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.utils.shiftedString
import io.gnosis.kouban.databinding.ItemTokenBinding
import io.gnosis.kouban.databinding.WidgetBalancesConfigureBinding
import io.gnosis.kouban.ui.SplashActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.getColorCompat
import pm.gnosis.svalinn.common.utils.snackbar
import java.lang.ref.WeakReference

class BalancesWidgetConfigure : AppCompatActivity() {

    private val viewModel by currentScope.viewModel<BalancesViewModel>(this) { parametersOf(appWidgetId) }
    private val adapter by currentScope.inject<BalancesAdapter> { parametersOf(WeakReference(viewModel)) }
    private val picasso: Picasso by inject()
    private val binding by lazy { WidgetBalancesConfigureBinding.inflate(layoutInflater) }

    private val appWidgetId by lazy {
        intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        viewModel.init()
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
                            adapter.selectedItem = it.position

                        } else {
                            fab.isEnabled = false
                            fab.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.dark_grey))
                            adapter.selectedItem = -1
                        }
                    }
                }

                is TokenSelectionSubmitted -> {

                    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)

                    // Create an Intent to launch App
                    val pendingIntent: PendingIntent = Intent(this, SplashActivity::class.java)
                        .let { intent ->
                            PendingIntent.getActivity(this, 0, intent, 0)
                        }
                    // Get the layout for the App Widget and attach an on-click listener
                    // to the button
                    val views: RemoteViews = RemoteViews(
                        packageName,
                        R.layout.widget_balances
                    ).apply {
                        setOnClickPendingIntent(R.id.token_item_icon, pendingIntent)
                        setTextViewText(R.id.token_item_symbol, it.token.tokenInfo.symbol)
                        setTextViewText(R.id.safe_balance, it.token.balance.shiftedString(it.token.tokenInfo.decimals, 5))
                        setTransactionIcon(R.id.token_item_icon, picasso, it.token.tokenInfo.icon)
                    }
                    // Tell the AppWidgetManager to perform an update on the current app widget
                    appWidgetManager?.updateAppWidget(appWidgetId, views)

                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    })
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
}

class BalancesAdapter(
    private val picasso: Picasso,
    private val tokenClickListener: WeakReference<OnTokenClickedListener>
) : RecyclerView.Adapter<BalanceItemViewHolder>() {

    private val items = mutableListOf<Balance>()

    var selectedItem: Int = -1
        set(value) {
            field = value
            notifyAllChanged()
        }

    interface OnTokenClickedListener {
        fun onTokenClicked(tokenBalance: Balance?, position: Int)
    }

    @Deprecated("Unsafe")
    fun setItemsUnsafe(items: List<Balance>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BalanceItemViewHolder, position: Int) {
        holder.bind(items[position], position == selectedItem, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceItemViewHolder {
        return BalanceItemViewHolder(
            ItemTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            picasso,
            tokenClickListener
        )
    }

    override fun getItemCount() = items.size

    fun notifyAllChanged() {
        notifyItemRangeChanged(0, items.size)
    }
}


class BalanceItemViewHolder(
    val binding: ItemTokenBinding,
    private val picasso: Picasso,
    private val tokenClickListener: WeakReference<BalancesAdapter.OnTokenClickedListener>
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Balance, selected: Boolean, position: Int) {
        with(binding) {
            tokenItemSymbol.text = item.tokenInfo.symbol
            tokenItemInfo.text = item.balance.shiftedString(item.tokenInfo.decimals, 5)
            tokenItemIcon.setTransactionIcon(picasso, item.tokenInfo.icon)
            tokenItemRadio.isChecked = selected

            root.setOnClickListener {
                tokenClickListener.get()?.onTokenClicked(if (!selected) item else null, position)
            }
        }
    }
}

class BalancesViewModel(
    private val safeRepository: SafeRepository,
    private val addressManager: SafeAddressManager,
    private val widgetPrefs: BalancesWidgetPrefs,
    private val widgetId: Int
) : ViewModel(), BalancesAdapter.OnTokenClickedListener {

    val events = MutableLiveData<ViewState>()
    val loadingEvents = MutableLiveData<Loading>()

    private lateinit var safeAddress: Solidity.Address
    private var selectedToken: Balance? = null

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

    override fun onTokenClicked(token: Balance?, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedToken = token
            events.postValue(TokenSelection(token, position))
        }
    }

    fun onTokenSelectionSubmitted() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedToken?.let {
                widgetPrefs.saveTokenForWidget(it.tokenInfo.address, widgetId)
                events.postValue(TokenSelectionSubmitted(it))
            }
        }
    }
}

data class TokenBalances(
    val balances: List<Balance>
) : ViewState()

data class TokenSelectionSubmitted(
    val token: Balance
) : ViewState()

data class TokenSelection(
    val token: Balance?,
    val position: Int
) : ViewState()

class NoSafeAddressSet : Throwable()


