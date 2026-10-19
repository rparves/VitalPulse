@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitapulse.app.ui.theme.VP

private val CardShape = RoundedCornerShape(26.dp)
private val Hairline = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f)))

/** Glass card: soft vertical gradient, hairline border, optional coloured glow in the top-right corner. */
@Composable
fun VCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glow: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var m = modifier.fillMaxWidth()
        .clip(CardShape)
        .background(Brush.verticalGradient(listOf(VP.CardHi, VP.Card)))
    if (glow != null) {
        m = m.drawBehind {
            val r = size.maxDimension * 0.75f
            val c = Offset(size.width, 0f)
            drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = 0.30f), Color.Transparent), center = c, radius = r), r, c)
        }
    }
    m = m.border(1.dp, Hairline, CardShape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m.padding(18.dp), content = content)
}

@Composable
fun IconBadge(icon: ImageVector, color: Color, size: Dp = 34.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = color, modifier = Modifier.size(size * 0.55f)) }
}

@Composable
fun CardHeader(icon: ImageVector, title: String, color: Color, trailing: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon, color, 28.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            title.uppercase(), style = MaterialTheme.typography.labelSmall, color = color,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Small uppercase "eyebrow" label above headings. */
@Composable
fun Eyebrow(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color)
}

@Composable
fun LiveBadge(text: String, color: Color = VP.Lime) {
    val t = rememberInfiniteTransition(label = "live")
    val a by t.animateFloat(0.25f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse")
    Row(
        Modifier.clip(CircleShape).background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).alpha(a).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier.clip(shape).background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val bg: Brush = if (enabled) VP.brand else SolidColor(Color.White.copy(alpha = 0.08f))
    val fg = if (enabled) VP.OnLime else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier.height(54.dp).clip(shape).background(bg).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
            if (icon != null) {
                Icon(icon, null, tint = fg)
                Spacer(Modifier.width(8.dp))
            }
            Text(text, color = fg, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/** Animated count-up for headline numbers. */
@Composable
fun countUp(target: Int): Int {
    val v by animateIntAsState(target, tween(1100, easing = FastOutSlowInEasing), label = "count")
    return v
}

@Composable
fun ProgressLine(progress: Float, color: Color, height: Dp = 7.dp) {
    val a by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "progress")
    Box(Modifier.fillMaxWidth().height(height).clip(CircleShape).background(color.copy(alpha = 0.14f))) {
        Box(
            Modifier.fillMaxWidth(a).fillMaxHeight().clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.55f), color)))
        )
    }
}

@Composable
fun MetricTile(
    icon: ImageVector,
    title: String,
    value: String,
    unit: String,
    color: Color,
    progress: Float?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    VCard(modifier, onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, color)
            Spacer(Modifier.width(10.dp))
            Eyebrow(title)
        }
        Spacer(Modifier.height(14.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        if (unit.isNotBlank()) {
            Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
        if (progress != null) {
            Spacer(Modifier.height(12.dp))
            ProgressLine(progress, color)
        }
        if (action != null) {
            Spacer(Modifier.height(10.dp))
            action()
        }
    }
}

data class Ring(val progress: Float, val color: Color, val color2: Color = color)

/** Concentric gradient rings with a soft neon glow, animated on change. */
@Composable
fun ActivityRings(rings: List<Ring>, modifier: Modifier = Modifier, strokeFraction: Float = 0.1f) {
    val animated = rings.map { r ->
        animateFloatAsState(r.progress.coerceIn(0f, 1f), tween(1200, easing = FastOutSlowInEasing), label = "ring").value
    }
    Canvas(modifier) {
        val stroke = size.minDimension * strokeFraction
        rings.forEachIndexed { i, r ->
            val inset = stroke * 0.8f + i * stroke * 1.3f
            val topLeft = Offset(inset, inset)
            val arc = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(r.color.copy(alpha = 0.12f), 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
            val sweep = 360f * animated[i]
            if (sweep > 0.5f) {
                rotate(-90f) {
                    drawArc(r.color.copy(alpha = 0.18f), 0f, sweep, false, topLeft, arc,
                        style = Stroke(stroke * 1.9f, cap = StrokeCap.Round))
                    drawArc(Brush.sweepGradient(listOf(r.color2, r.color), center = center), 0f, sweep, false, topLeft, arc,
                        style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
        }
    }
}

@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    color: Color,
    goal: Float? = null,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier.fillMaxWidth().height(170.dp)) {
        if (values.isEmpty()) return@Canvas
        val max = maxOf(values.maxOrNull() ?: 0f, goal ?: 0f, 1f) * 1.08f
        val labelH = 20.dp.toPx()
        val chartH = size.height - labelH
        val slot = size.width / values.size
        val barW = slot * 0.46f
        values.forEachIndexed { i, v ->
            val h = chartH * (v / max)
            val x = i * slot + (slot - barW) / 2
            drawRoundRect(Color.White.copy(alpha = 0.04f), Offset(x, 0f), Size(barW, chartH), CornerRadius(barW / 2))
            if (h > 0f) {
                drawRoundRect(
                    Brush.verticalGradient(listOf(color, color.copy(alpha = 0.35f)), startY = chartH - h, endY = chartH),
                    Offset(x, chartH - h), Size(barW, h), CornerRadius(barW / 2)
                )
            }
            val tl = measurer.measure(labels.getOrElse(i) { "" }, TextStyle(fontSize = 11.sp, color = labelColor))
            drawText(tl, topLeft = Offset(i * slot + (slot - tl.size.width) / 2, chartH + 4.dp.toPx()))
        }
        goal?.let {
            val y = chartH * (1 - it / max)
            drawLine(
                color.copy(alpha = 0.8f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
            )
        }
    }
}

@Composable
fun LineChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(150.dp)) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val span = (max - min).takeIf { it > 0.01f } ?: 1f
        val pad = 10.dp.toPx()
        val pts = values.mapIndexed { i, v ->
            Offset(
                pad + i * (size.width - 2 * pad) / (values.size - 1),
                pad + (size.height - 2 * pad) * (1 - (v - min) / span)
            )
        }
        val line = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(pts.last().x, size.height)
            lineTo(pts.first().x, size.height)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.40f), Color.Transparent)))
        drawPath(line, color.copy(alpha = 0.25f), style = Stroke(9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(line, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        pts.forEach { drawCircle(VP.Bg, 5.dp.toPx(), it); drawCircle(color, 3.5.dp.toPx(), it) }
    }
}

@Composable
fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { s -> onChange(s.filter { it.isDigit() || (decimal && it == '.') }.take(7)) },
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
    )
}

@Composable
fun <T> ChoiceRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (v, label) ->
            FilterChip(
                selected = v == selected, onClick = { onSelect(v) }, label = { Text(label) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VP.Lime, selectedLabelColor = VP.OnLime
                ),
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}
