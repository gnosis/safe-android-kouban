package io.gnosis.kouban.core.utils

import android.app.DatePickerDialog
import android.content.Context
import androidx.core.os.ConfigurationCompat
import io.gnosis.kouban.core.R
import java.text.SimpleDateFormat
import java.util.*

fun Long.asFormattedDateTime(context: Context, dateFormat: String = DateFormats.default): String =
    Date().apply { time = this@asFormattedDateTime * 1000 }.let { date ->
        with(SimpleDateFormat(dateFormat, ConfigurationCompat.getLocales(context.resources.configuration)[0])) {
            format(date)
        }
    }

fun Long.asFormattedDateTime(formatter: SimpleDateFormat): String =
    Date().apply { time = this@asFormattedDateTime * 1000 }.let { date -> formatter.format(date) }

fun showDatePickerDialog(context: Context, date: Date? = null, onDatePicked: (Date) -> Unit) {
    val datePickedListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        val pickedDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        onDatePicked(pickedDate.time)
    }
    val startDate = Calendar.getInstance().apply { date?.let { time = date } }
    with(startDate) {
        DatePickerDialog(
            context,
            R.style.DialogTheme,
            datePickedListener,
            get(Calendar.YEAR),
            get(Calendar.MONTH),
            get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}

object DateFormats {
    const val transactionList = "dd-MM-yyyy HH:mm"
    const val monthYear = "MMMM, yyyy"
    const val datePicker = "yyyy.MM.dd"

    const val default = transactionList
}
