package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import ru.skillbranch.skillarticles.extensions.getLineBottomWithoutPadding

class BlockquotesSpan(
    @Px private val gapWidth: Float,
    @Px private val quoteWidth: Float,
    @ColorInt private val lineColor: Int
) : LeadingMarginSpan {

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, currentMarginLocation: Int, paragraphDirection: Int,
        lineTop: Int, lineBaseline: Int, lineBottom: Int, text: CharSequence?, lineStart: Int,
        lineEnd: Int, isFirstLine: Boolean, layout: Layout
    ) {
        val line = layout.getLineForOffset(lineStart)
        val nextLineLastIndex = layout.getLineVisibleEnd(line + 1)
        val nextLineFirstIndex = layout.getLineStart(line + 1)
        val lastQuote = nextLineLastIndex == nextLineFirstIndex
        val bottom = if (lastQuote) layout.getLineBottomWithoutPadding(line) else lineBottom
        paint.withCustomColor {
            canvas.drawLine(
                quoteWidth / 2,
                lineTop.toFloat(),
                quoteWidth / 2,
                bottom.toFloat(),
                paint)
        }
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return (quoteWidth + gapWidth).toInt()
    }

    private inline fun Paint.withCustomColor(block: () -> Unit) {
        val oldColor = color
        val oldStyle = style
        val oldWidth = strokeWidth

        color = lineColor
        style = Paint.Style.STROKE
        strokeWidth = quoteWidth

        block()

        color = oldColor
        style = oldStyle
        strokeWidth = oldWidth
    }
}