@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier.fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(MaterialTheme.colorScheme.surfaceContainer)
    val m = (if (onClick != null) base.clickable(onClick = onClick) else base).padding(16.dp)
    Column(m, content = content)
}

@Composable
fun CardHeader(icon: ImageVector, title: String, color: Color, trailing: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            title, style = MaterialTheme.typography.titleSmall, color = color,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProgressLine(progress: Float, color: Color, height: Dp = 6.dp) {
    val a by animateFloatAsState(progress.coerceIn(0f, 1f), tween(800), label = "progress")
    Box(Modifier.fillMaxWidth().height(height).clip(CircleShape).background(color.copy(alpha = 0.18f))) {
        Box(Modifier.fillMaxWidth(a).fillMaxHeight().clip(CircleShape).background(color))
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
        CardHeader(icon, title, color)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(
                unit, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        if (progress != null) {
            Spacer(Modifier.height(10.dp))
            ProgressLine(progress, color)
        }
        if (action != null) {
            Spacer(Modifier.height(8.dp))
            action()
        }
    }
}

data class Ring(val progress: Float, val color: Color)

/** Concentric activity rings (Apple-style), animated. */
@Composable
fun ActivityRings(rings: List<Ring>, modifier: Modifier = Modifier) {
    val animated = rings.map { r ->
        animateFloatAsState(r.progress.coerceIn(0f, 1f), tween(1000), label = "ring").value
    }
    Canvas(modifier) {
        val stroke = size.minDimension * 0.1f
        rings.forEachIndexed { i, r ->
            val inset = stroke / 2 + i * stroke * 1.22f
            val topLeft = Offset(inset, inset)
            val arc = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(r.color.copy(alpha = 0.18f), -90f, 360f, false, topLeft, arc,
                style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(r.color, -90f, 360f * animated[i], false, topLeft, arc,
                style = Stroke(stroke, cap = StrokeCap.Round))
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
        val barW = slot * 0.5f
        values.forEachIndexed { i, v ->
            val h = chartH * (v / max)
            val x = i * slot + (slot - barW) / 2
            drawRoundRect(color.copy(alpha = 0.15f), Offset(x, 0f), Size(barW, chartH), CornerRadius(barW / 2))
            if (h > 0f) drawRoundRect(color, Offset(x, chartH - h), Size(barW, h), CornerRadius(barW / 2))
            val tl = measurer.measure(labels.getOrElse(i) { "" }, TextStyle(fontSize = 11.sp, color = labelColor))
            drawText(tl, topLeft = Offset(i * slot + (slot - tl.size.width) / 2, chartH + 4.dp.toPx()))
        }
        goal?.let {
            val y = chartH * (1 - it / max)
            drawLine(
                color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5.dp.toPx(),
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
        drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent)))
        drawPath(line, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        pts.forEach { drawCircle(color, 4.dp.toPx(), it) }
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
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
    )
}

@Composable
fun <T> ChoiceRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (v, label) ->
            FilterChip(selected = v == selected, onClick = { onSelect(v) }, label = { Text(label) })
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}
