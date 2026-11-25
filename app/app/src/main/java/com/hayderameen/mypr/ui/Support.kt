package com.hayderameen.mypr.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hayderameen.mypr.MyPrApplication
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Creates a ViewModel scoped to the current nav destination, with access to the app container. */
@Composable
inline fun <reified VM : ViewModel> appViewModel(key: String? = null, crossinline create: (MyPrApplication) -> VM): VM =
    viewModel(
        key = key,
        factory = viewModelFactory { initializer { create(this[APPLICATION_KEY] as MyPrApplication) } },
    )

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

private val shortDate = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

fun formatShortDate(epochMs: Long): String = epochMs.toLocalDate().format(shortDate)

/** "Today", "Yesterday", "3d ago", "2w ago", "5mo ago" — the Stitch day-card meta style. */
fun relativeDays(epochMs: Long, today: LocalDate = LocalDate.now()): String {
    val days = ChronoUnit.DAYS.between(epochMs.toLocalDate(), today)
    return when {
        days <= 0 -> "Today"
        days == 1L -> "Yesterday"
        days < 14 -> "${days}d ago"
        days < 60 -> "${days / 7}w ago"
        else -> "${days / 30}mo ago"
    }
}

@Composable
fun rememberApp(): MyPrApplication = androidx.compose.ui.platform.LocalContext.current.applicationContext as MyPrApplication
