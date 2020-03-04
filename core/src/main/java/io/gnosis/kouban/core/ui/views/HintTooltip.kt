package io.gnosis.kouban.core.ui.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.StringRes
import io.gnosis.kouban.core.R

class HintTooltip(
    val context: Context,
    @StringRes
    hintResId: Int
) : PopupWindow(context) {

    init {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isOutsideTouchable = true
        isFocusable = true

        contentView = LayoutInflater.from(context).inflate(R.layout.popup_hint_tooltip, null)

        contentView.findViewById<TextView>(R.id.text).setText(hintResId)
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT

        contentView.setOnClickListener {
            dismiss()
        }
    }
}
