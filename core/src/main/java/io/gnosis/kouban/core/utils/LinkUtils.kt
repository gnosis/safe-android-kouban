package io.gnosis.kouban.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import io.gnosis.kouban.core.R
import pm.gnosis.svalinn.common.utils.appendText
import pm.gnosis.svalinn.common.utils.openUrl
import timber.log.Timber

fun Context.openUrl(url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName)
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
    }

}

fun TextView.setupLink(url: String, text: String) {
    val linkDrawable = ContextCompat.getDrawable(this.context, R.drawable.ic_launch_black_24dp)!!
    linkDrawable.setBounds(0, 0, linkDrawable.intrinsicWidth, linkDrawable.intrinsicHeight)
    this.text = SpannableStringBuilder(Html.fromHtml(text))
        .append(" ")
        .appendText(" ", ImageSpan(linkDrawable, ImageSpan.ALIGN_BASELINE))
    setOnClickListener { this.context.openUrl(url) }
}

fun TextView.setupEtherscanTransactionUrl(transactionHash: String, @StringRes stringId: Int) {
    setupEtherscanTransactionUrl(transactionHash, context.getString(stringId))
}

fun TextView.setupEtherscanTransactionUrl(transactionHash: String, text: String) {
    setupLink(context.getString(R.string.etherscan_transaction_url, transactionHash), text)
}
