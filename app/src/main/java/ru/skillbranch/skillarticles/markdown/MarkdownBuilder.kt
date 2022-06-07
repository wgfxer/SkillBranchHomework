package ru.skillbranch.skillarticles.markdown

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.markdown.spans.*

class MarkdownBuilder(context: Context) {
    private val colorSecondary = context.attrValue(R.attr.colorSecondary)
    private val colorOnArticleBar = context.attrValue(R.attr.colorOnArticleBar)
    private val colorOnSurface = context.attrValue(R.attr.colorOnSurface)
    private val opacityColorSurface = context.getColor(R.color.color_surface)
    private val colorDivider = context.getColor(R.color.color_divider)
    private val gap: Float = context.dpToPx(8)
    private val bulletRadius = context.dpToPx(4)
    private val strikeWidth = context.dpToPx(4)
    private val quoteWidth = context.dpToPx(4)
    private val headerMarginTop = context.dpToPx(12)
    private val headerMarginBottom = context.dpToPx(8)
    private val ruleWidth = context.dpToPx(2)
    private val cornerRadius = context.dpToPx(8)
    private val linkIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_link_24)!!

    fun markdownToSpan(string: String): SpannedString {
        val markdown = MarkdownParser.parse(string)
        return buildSpannedString {
            markdown.elements.forEach { buildElement(it, this) }
        }
    }

    private fun buildElement(element: Element, builder: SpannableStringBuilder) {
        builder.apply {
            when(element) {
                is Element.Text -> append(element.text)
                is Element.UnorderedListItem -> {
                    inSpans(UnorderedListSpan(gap, bulletRadius, colorSecondary)) {
                        element.buildChildren(builder)
                    }
                }
                is Element.Quote -> {
                    inSpans(BlockquotesSpan(gap, quoteWidth, colorSecondary), StyleSpan(Typeface.ITALIC)) {
                        element.buildChildren(builder)
                    }
                }
                is Element.Header -> {
                    inSpans(HeaderSpan(element.level, colorOnArticleBar, colorDivider, headerMarginTop, headerMarginBottom)){
                        append(element.text)
                    }
                }
                is Element.Italic -> {
                    inSpans(StyleSpan(Typeface.ITALIC)) {
                        element.buildChildren(builder)
                    }
                }
                is Element.Bold -> {
                    inSpans(StyleSpan(Typeface.BOLD)) {
                        element.buildChildren(builder)
                    }
                }
                is Element.Strike -> {
                    inSpans(StrikethroughSpan()) {
                        element.buildChildren(builder)
                    }
                }
                is Element.Rule -> {
                    inSpans(HorizontalRuleSpan(ruleWidth, colorDivider)) {
                        append(element.text)
                    }
                }
                is Element.InlineCode -> {
                    inSpans(InlineCodeSpan(colorOnSurface, opacityColorSurface, cornerRadius, gap)) {
                        append(element.text)
                    }
                }
                is Element.Link -> {
                    inSpans(IconLinkSpan(linkIcon, gap, colorOnArticleBar, strikeWidth), URLSpan(element.link)) {
                        append(element.text)
                    }
                }
                is Element.OrderedListItem -> {
                    inSpans(OrderedListSpan(gap, element.order, colorSecondary)) {
                        append(element.text)
                    }
                }
                else -> append(element.text)
            }
        }
    }

    private fun Element.buildChildren(builder: SpannableStringBuilder) {
        elements.forEach { buildElement(it, builder) }
    }
}