package app.noor.prayer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*

@Composable
fun Header(title: String, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Surface(color = HeaderGreen, contentColor = Color.White) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("back")) }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
            actions()
        }
    }
}

@Composable
fun NoorCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, highlight: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = modifier.fillMaxWidth().then(if (onClick != null) Modifier.clip(shape).clickable(onClick = onClick) else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
fun ArabicText(text: String, size: Float, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Text(
        text,
        modifier = modifier.fillMaxWidth(),
        fontSize = size.sp,
        lineHeight = (size * 1.85f).sp,
        color = color,
        textAlign = TextAlign.Right,
        style = TextStyle(textDirection = TextDirection.Rtl)
    )
}

@Composable
fun PauseIcon(color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(width = 5.dp, height = 18.dp).background(color, RoundedCornerShape(2.dp)))
        Box(Modifier.size(width = 5.dp, height = 18.dp).background(color, RoundedCornerShape(2.dp)))
    }
}

@Composable
fun RadioRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text, modifier = Modifier.weight(1f))
    }
}

@Composable
fun Stepper(label: String, value: Int, onChange: (Int) -> Unit, min: Int, max: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = { if (value > min) onChange(value - 1) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp)
        ) { Text("−") }
        Text(
            nd(if (value > 0) "+$value" else "$value"),
            modifier = Modifier.width(52.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        OutlinedButton(
            onClick = { if (value < max) onChange(value + 1) },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(36.dp)
        ) { Text("+") }
    }
}

fun gregShort(d: java.time.LocalDate): String =
    nd("${d.dayOfMonth} ${GREG_MONTHS[Prefs.lang.idx()][d.monthValue - 1]} ${d.year}")

fun isFriday(ms: Long): Boolean =
    java.time.Instant.ofEpochMilli(ms).atZone(java.time.ZoneId.systemDefault()).dayOfWeek == java.time.DayOfWeek.FRIDAY
