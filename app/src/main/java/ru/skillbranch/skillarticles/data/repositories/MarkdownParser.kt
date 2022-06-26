package ru.skillbranch.skillarticles.data.repositories

import ru.skillbranch.skillarticles.data.repositories.ParseGroup.BLOCK_CODE_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.BOLD_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.HEADER_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.IMAGE_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.INLINE_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.ITALIC_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.LINK_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.ORDERED_LIST_ITEM_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.QUOTE_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.RULE_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.STRIKE_GROUP
import ru.skillbranch.skillarticles.data.repositories.ParseGroup.UNORDERED_LIST_ITEM_GROUP
import java.util.regex.Pattern

object MarkdownParser {
    private val MARKDOWN_GROUPS = ParseGroup.values().toMarkdownGroups()

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    /**
     * parse markdown text to elements
     */
    fun parse(string: String): List<MarkdownElement> {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return elements.fold(mutableListOf()) { acc, element ->
            val last = acc.lastOrNull()
            when(element) {
                is Element.Image -> acc.add(MarkdownElement.Image(element, last?.bounds?.second ?: 0))
                is Element.BlockCode -> acc.add(MarkdownElement.Scroll(element, last?.bounds?.second ?: 0))
                else -> {
                    if (last is MarkdownElement.Text) last.elements.add(element)
                    else acc.add(MarkdownElement.Text(mutableListOf(element), last?.bounds?.second ?: 0))
                }
            }
            acc
        }
    }

    /**
     * find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            //if something is found then everything before - TEXT
            if (lastStartIndex < startIndex) {
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            //groups range for iterate by groups
            var currentGroup: ParseGroup? = null
            for (gr in 1..matcher.groupCount()) {
                if (matcher.group(gr) != null) {
                    currentGroup = ParseGroup.values()[gr - 1]
                    break
                }
            }

            when (currentGroup) {
                UNORDERED_LIST_ITEM_GROUP -> {
                    val text = string.subSequence(startIndex.plus(2), endIndex)

                    //find inner elements
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    //next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                }

                QUOTE_GROUP -> {
                    //text without "> "
                    val text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements = findElements(text)
                    val element = Element.Quote(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //HEADER
                HEADER_GROUP -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length

                    //text without "{#} "
                    val text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                ITALIC_GROUP -> {
                    //text without "*{}*"
                    val text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)
                    val element = Element.Italic(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                BOLD_GROUP -> {
                    //text without "**{}**"
                    val text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subelements = findElements(text)
                    val element = Element.Bold(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                STRIKE_GROUP -> {
                    //text without "~~{}~~"
                    val text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subelements = findElements(text)
                    val element = Element.Strike(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                RULE_GROUP -> {
                    //text without "***" insert empty character
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //INLINE CODE
                INLINE_GROUP -> {
                    //text without "`{}`"
                    val text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                LINK_GROUP -> {
                    //full text for regex
                    val text = string.subSequence(startIndex, endIndex)
                    val (title:String, link:String) = "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                BLOCK_CODE_GROUP -> {
                    val text = string.subSequence(startIndex.plus(3), endIndex.minus(3))
                    val element = Element.BlockCode(text = text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                ORDERED_LIST_ITEM_GROUP -> {
                    val text = string.subSequence(startIndex, endIndex)
                    val (order, title) = "(^\\d+\\.) (.*)\$".toRegex().find(text)!!.destructured
                    val element = Element.OrderedListItem(order, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                IMAGE_GROUP -> {
                    val text = string.subSequence(startIndex, endIndex)
                    val (altRaw: String?, link, _, title) = "!\\[(.*)]\\((.*?)( \"(.*)\")?\\)".toRegex().find(text)!!.destructured
                    val alt = altRaw.ifEmpty { null }
                    val element = Element.Image(link, alt, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                else -> break@loop
            }

        }

        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }

        return parents
    }
}

sealed class MarkdownElement {
    abstract val offset: Int

    val bounds: Pair<Int, Int> by lazy {
        when(this) {
            is Text -> {
                val end = elements.fold(offset) { acc, el ->
                    acc + el.spread().sumOf { it.text.length }
                }
                offset to end
            }
            is Image -> offset to offset + image.text.length
            is Scroll -> offset to offset + blockCode.text.length
        }
    }

    data class Text(
        val elements: MutableList<Element>,
        override val offset: Int = 0
    ): MarkdownElement()

    data class Image(
        val image: Element.Image,
        override val offset: Int = 0
    ): MarkdownElement()

    data class Scroll(
        val blockCode: Element.BlockCode,
        override val offset: Int = 0
    ): MarkdownElement()
}

sealed class Element {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ", //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element() {
        enum class Type { START, END, MIDDLE, SINGLE }
    }

    data class Image(
        val url: String,
        val alt: String?,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()
}

enum class ParseGroup(val expression: String) {
    UNORDERED_LIST_ITEM_GROUP("(^[*+-] .+$)"),
    HEADER_GROUP("(^#{1,6} .+?$)"),
    QUOTE_GROUP("(^> .+?$)"),
    RULE_GROUP("(^_{3}|^-{3}|^\\*{3})"),
    STRIKE_GROUP("((?<!~)~~[^~].*?[^~]~~(?!~))"),
    INLINE_GROUP("((?<!`)`[^`\\s][^`\\n]*?[^`\\s]`(?!`))"),
    LINK_GROUP("(\\[[^\\[\\]]*?]\\(.+?\\))"),
    ITALIC_GROUP("((?<!\\*)\\*[^*].*?[^*]\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"),
    BOLD_GROUP("((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"),
    BLOCK_CODE_GROUP("((?<!`)`{3}[^\\s][.\\s\\S]*?[^`]`{3}(?!`))"),
    ORDERED_LIST_ITEM_GROUP("(^[\\d]*\\..*?\$)"),
    IMAGE_GROUP("(!\\[.*\\]\\(.*\\))");

    val regex = expression.toRegex()
}

private fun Array<ParseGroup>.toMarkdownGroups(): String {
    val result = StringBuilder()
    this.foldIndexed(result) { index: Int, acc: StringBuilder, group: ParseGroup ->
        val delimiter = if (index == this.lastIndex) "" else "|"
        acc.append("${group.expression}$delimiter")
    }
    return result.toString()
}

private fun Element.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty()) elements.addAll(this.elements.spread())
    else elements.add(this)
    return elements
}

private fun List<Element>.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    forEach { elements.addAll(it.spread()) }
    return elements
}

fun List<MarkdownElement>.clearContent(): String {
    return StringBuilder().apply {
        this@clearContent.forEach {
            when(it) {
                is MarkdownElement.Text -> it.elements.forEach { el -> append(el.clearContent()) }
                is MarkdownElement.Image -> append(it.image.clearContent())
                is MarkdownElement.Scroll -> append(it.blockCode.clearContent())

            }
        }
    }.toString()
}

private fun Element.clearContent(): String {
    return StringBuilder().apply {
        val element = this@clearContent
        if (element.elements.isEmpty()) append(element.text)
        else element.elements.forEach { append(it.clearContent()) }
    }.toString()
}