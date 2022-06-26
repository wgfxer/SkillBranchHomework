package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.text.style.ReplacementSpan
import androidx.annotation.VisibleForTesting

class UnderlineSpan(
    private val underlineColor: Int,
    dotWidth: Float = 6f
) : ReplacementSpan() {
    private val dashs = DashPathEffect(floatArrayOf(dotWidth, dotWidth), 0f)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val textWidth = paint.measureText(text.toString(), start, end).toInt()
        paint.forLine {
            path.reset()
            path.moveTo(x, bottom.toFloat())
            path.lineTo(x + textWidth, bottom.toFloat())
            canvas.drawPath(path, paint)
        }

        //check draw text
        canvas.drawText(
            text, start, end,
            x,
            y.toFloat(),
            paint
        )
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ) = paint.measureText(text.toString(), start, end).toInt()

    private inline fun Paint.forLine(block: () -> Unit) {
        val oldPathEffect = pathEffect
        val oldColor = color
        val oldStyle = style
        val oldStrokeWidth = strokeWidth

        pathEffect = dashs
        color = underlineColor
        style = Paint.Style.STROKE
        strokeWidth = 0f

        block()

        pathEffect = oldPathEffect
        color = oldColor
        style = oldStyle
        strokeWidth = oldStrokeWidth
    }
}