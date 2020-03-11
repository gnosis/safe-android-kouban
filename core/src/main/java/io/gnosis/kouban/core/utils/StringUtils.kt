package io.gnosis.kouban.core.utils

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.core.os.ConfigurationCompat
import pm.gnosis.utils.*
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*

fun String.asMiddleEllipsized(boundariesLength: Int): String {
    return if (this.length > boundariesLength * 2)
        "${this.subSequence(0, boundariesLength)}...${this.subSequence(this.length - boundariesLength, this.length)}"
    else this
}

fun String.underline(): CharSequence =
    SpannableString(this).also {
        it.setSpan(UnderlineSpan(), 0, length, 0)
    }

fun String.parseToBigIntegerOrNull(): BigInteger? =
    nullOnThrow { parseToBigInteger() }

fun String.parseToBigInteger(): BigInteger =
    if (startsWith("0x")) hexAsBigInteger() else decimalAsBigInteger()
