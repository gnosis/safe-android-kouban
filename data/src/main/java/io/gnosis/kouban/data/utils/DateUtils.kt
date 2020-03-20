package io.gnosis.kouban.data.utils

import java.util.*

fun Date.onlyDate(): Date =
    Calendar.getInstance().let { calendar ->
        calendar.time = this
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.time
    }

fun Date.afterOrEqual(other: Date?): Boolean = other != null && (this == other || this.after(other))

fun Date.beforeOrEqual(other: Date?): Boolean = other != null && (this == other || this.before(other))

object DateFormats {
    const val backend = "yyyy-MM-dd'T'HH:mm:ssX"
}
