package io.gnosis.kouban.core.ui.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import io.gnosis.kouban.core.R

class HintTooltip(
    val context: Context,
    hint: String
) : PopupWindow(context) {

    init {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isOutsideTouchable = true
        isFocusable = false

        contentView = LayoutInflater.from(context).inflate(R.layout.popup_hint_tooltip, null)

        contentView.findViewById<TextView>(R.id.text).text = hint
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT

        contentView.setOnClickListener {
            dismiss()
        }
    }
}
