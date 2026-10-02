package com.viniciusrio.dengo.ui.mural

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.DrawingStroke
import com.viniciusrio.dengo.model.MuralContent
import kotlin.math.min

/** Coleta as posições de um gesto antes da normalização para o modelo do Mural. */
internal class StrokeSamples(start: Offset) {
    private val points = mutableListOf(start)

    fun append(position: Offset, pressed: Boolean): Boolean {
        val distance = (position - points.last()).getDistance()
        if ((pressed && distance >= 2f) || (!pressed && distance > 0f)) {
            points.add(position)
            return true
        }
        return false
    }

    fun completed(): List<Offset> = points.toList()
}

@Composable
internal fun MuralDrawing(
    drawing: MuralContent.Drawing,
    description: String,
    modifier: Modifier = Modifier,
    onStroke: ((List<DrawingPoint>, Float, Int) -> Unit)? = null,
    selectedArgb: Int = 0xFFA63854.toInt(),
) {
    val active = remember { mutableStateListOf<Offset>() }
    var activeColor by remember { mutableStateOf(selectedArgb) }
    val latestOnStroke by rememberUpdatedState(onStroke)
    val latestColor by rememberUpdatedState(selectedArgb)
    val drawingModifier = if (onStroke == null) modifier else modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val strokeColor = latestColor
            activeColor = strokeColor
            val surface = size
            val samples = StrokeSamples(down.position)
            active.clear()
            active.add(down.position)
            down.consume()
            var pressed = true
            var completed = false
            while (pressed) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (samples.append(change.position, change.pressed)) {
                    active.add(change.position)
                }
                pressed = change.pressed
                if (!pressed) completed = true
                change.consume()
            }
            if (completed && surface.width > 0 && surface.height > 0) {
                latestOnStroke?.invoke(
                    samples.completed().map { DrawingPoint(
                        (it.x / surface.width).coerceIn(0f, 1f),
                        (it.y / surface.height).coerceIn(0f, 1f),
                    ) },
                    surface.width.toFloat() / surface.height,
                    strokeColor,
                )
            }
            active.clear()
        }
    }
    Canvas(
        modifier = drawingModifier
            .background(Color.White)
            .border(1.dp, Color(0xFFE9DEE1))
            .semantics { contentDescription = description },
    ) {
        drawing.strokes.forEach { stroke -> drawStroke(stroke, stroke.aspectRatio ?: drawing.aspectRatio) }
        if (active.isNotEmpty()) {
            drawStroke(
                DrawingStroke(
                    activeColor,
                    active.map { DrawingPoint(it.x / size.width, it.y / size.height) },
                ),
                size.width / size.height,
            )
        }
    }
}

private fun DrawScope.drawStroke(stroke: DrawingStroke, aspectRatio: Float) {
    val scale = min(size.width / aspectRatio, size.height) * 0.96f
    val xOffset = (size.width - aspectRatio * scale) / 2f
    val yOffset = (size.height - scale) / 2f
    fun DrawingPoint.toOffset() = Offset(xOffset + x * aspectRatio * scale, yOffset + y * scale)
    val color = Color(stroke.argb)
    val width = (scale * 0.012f).coerceAtLeast(1.5f)
    if (stroke.points.size == 1) {
        drawCircle(color, radius = width / 2f, center = stroke.points.first().toOffset())
    } else {
        val path = Path().apply {
            moveTo(stroke.points.first().toOffset().x, stroke.points.first().toOffset().y)
            stroke.points.drop(1).forEach { point ->
                val offset = point.toOffset()
                lineTo(offset.x, offset.y)
            }
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = width,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
        ))
    }
}
