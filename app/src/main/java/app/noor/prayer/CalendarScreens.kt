package app.noor.prayer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val today = LocalDate.now()
    val th = Hijri.of(today)
    var y by remember { mutableIntStateOf(th.y) }
    var m by remember { mutableIntStateOf(th.m) }

    val first = Hijri.toGregorian(y, m, 1)
    val len = Hijri.monthLength(y, m)
    val last = first.plusDays(len - 1L)
    val lead = first.dayOfWeek.value % 7
    val events = Hijri.events.filter { it.month == m }
    val eventDays = events.map { it.day }.toSet()
    val rows = (lead + len + 6) / 7

    Column(Modifier.fillMaxSize()) {
        Header(tr("calendar"), onBack) {
            TextButton(onClick = { y = th.y; m = th.m }) { Text(tr("today"), color = Color.White) }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (m == 1) { m = 12; y-- } else m-- },
                    enabled = y > 1300 || m > 1
                ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(nd("${Hijri.monthName(m)} $y"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${gregShort(first)} – ${gregShort(last)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(
                    onClick = { if (m == 12) { m = 1; y++ } else m++ },
                    enabled = y < 1600
                ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                WEEKDAYS_SHORT[Prefs.lang.idx()].forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0..6) {
                        val d = r * 7 + c - lead + 1
                        if (d in 1..len) {
                            val g = first.plusDays(d - 1L)
                            val isToday = g == today
                            Box(
                                Modifier.weight(1f).aspectRatio(0.9f).padding(2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isToday) MaterialTheme.colorScheme.primary else if (d in eventDays) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                val fg = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(nd("$d"), fontWeight = FontWeight.Bold, color = fg)
                                    Text(nd("${g.dayOfMonth}"), style = MaterialTheme.typography.labelSmall, color = fg.copy(alpha = 0.7f))
                                }
                            }
                        } else {
                            Spacer(Modifier.weight(1f).aspectRatio(0.9f))
                        }
                    }
                }
            }
            SectionTitle(tr("events"))
            if (events.isEmpty()) {
                Text("—", Modifier.padding(horizontal = 16.dp))
            }
            events.forEach { e ->
                NoorCard(Modifier.padding(vertical = 4.dp)) {
                    Text(e.name.s(), fontWeight = FontWeight.SemiBold)
                    Text(
                        nd("${e.day} ${Hijri.monthName(m)} $y") + "  •  " + gregShort(Hijri.toGregorian(y, m, e.day)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RamadanScreen(onBack: () -> Unit) {
    val today = LocalDate.now()
    val th = Hijri.of(today)
    var year = th.y
    var start = Hijri.toGregorian(year, 9, 1)
    var len = Hijri.monthLength(year, 9)
    if (today.isAfter(start.plusDays(len - 1L))) {
        year++
        start = Hijri.toGregorian(year, 9, 1)
        len = Hijri.monthLength(year, 9)
    }
    val daysTo = ChronoUnit.DAYS.between(today, start)
    val dayNo = if (!today.isBefore(start)) ChronoUnit.DAYS.between(start, today) + 1 else 0L
    val startF = start
    val lenF = len
    val days = remember(startF, lenF, Prefs.lat, Prefs.lon, Prefs.method, Prefs.hanafi, Prefs.adj.toList()) {
        (0 until lenF).map { i ->
            val d = startF.plusDays(i.toLong())
            d to PrayerCalc.day(d)
        }
    }
    val todayTimes = if (dayNo > 0) days.getOrNull((dayNo - 1).toInt())?.second else days.firstOrNull()?.second

    Column(Modifier.fillMaxSize()) {
        Header(tr("ramadan"), onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NoorCard(highlight = true) {
                if (dayNo > 0) {
                    Text("${tr("ramadan_day")} ${nd("$dayNo")} / ${nd("$len")}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("${tr("starts_in")} ${nd("$daysTo")} ${tr("days")}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${nd("1 ${Hijri.monthName(9)} $year")} • ${gregShort(start)}", style = MaterialTheme.typography.bodyMedium)
                }
                if (todayTimes != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(tr("sehri_ends"), style = MaterialTheme.typography.labelLarge)
                            Text(fmtTime(todayTimes.t[0]), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(tr("iftar"), style = MaterialTheme.typography.labelLarge)
                            Text(fmtTime(todayTimes.t[4]), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            NoorCard {
                Row(Modifier.fillMaxWidth()) {
                    Text(tr("day"), Modifier.weight(0.6f), fontWeight = FontWeight.Bold)
                    Text(tr("date"), Modifier.weight(1.6f), fontWeight = FontWeight.Bold)
                    Text(tr("sehri"), Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(tr("iftar"), Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                days.forEachIndexed { i, (d, t) ->
                    val isToday = d == today
                    Row(
                        Modifier.fillMaxWidth()
                            .background(if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .padding(vertical = 5.dp, horizontal = 2.dp)
                    ) {
                        Text(nd("${i + 1}"), Modifier.weight(0.6f), fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                        Text(nd("${d.dayOfMonth} ${GREG_MONTHS[Prefs.lang.idx()][d.monthValue - 1].take(if (Prefs.lang == "en") 3 else 20)}"), Modifier.weight(1.6f))
                        Text(if (t != null) fmtTime(t.t[0]) else "--", Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(if (t != null) fmtTime(t.t[4]) else "--", Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
            DuaCard(DUA_SEHRI)
            DuaCard(DUA_IFTAR)
            Text(tr("ramadan_note"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }
    }
}
