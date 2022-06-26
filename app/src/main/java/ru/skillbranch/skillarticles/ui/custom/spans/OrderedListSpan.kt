package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting


class OrderedListSpan(
    @Px private val gapWidth: Float,
    private val order: String,
    @ColorInt private val orderColor: Int
) : LeadingMarginSpan {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)

    private var measuredOrderText = 0f

    override fun getLeadingMargin(first: Boolean): Int {
        //val margin = if (first) (measuredOrderText + gapWidth).toInt() else 0
        val margin = if (first) (order.length.inc() * gapWidth).toInt() else 0
        return margin
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, currentMarginLocation: Int, paragraphDirection: Int,
        lineTop: Int, lineBaseline: Int, lineBottom: Int, text: CharSequence?, lineStart: Int,
        lineEnd: Int, isFirstLine: Boolean, layout: Layout?
    ) {
        //measuredOrderText = paint.measureText(order)
        if (isFirstLine) {
            paint.forText {
                canvas.drawText(
                    order,
                    currentMarginLocation + gapWidth,
                    lineBaseline.toFloat(),
                    paint
                )
            }
        }
    }

    private fun Paint.forText(block: () -> Unit) {
        val oldColor = color

        color = orderColor

        block()

        color = oldColor
    }
}