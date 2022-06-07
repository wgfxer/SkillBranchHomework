package ru.skillbranch.skillarticles.markdown

import ru.skillbranch.skillarticles.markdown.ParseGroup.BLOCK_CODE_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.BOLD_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.HEADER_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.IMAGE_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.INLINE_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.ITALIC_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.LINK_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.ORDERED_LIST_ITEM_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.QUOTE_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.RULE_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.STRIKE_GROUP
import ru.skillbranch.skillarticles.markdown.ParseGroup.UNORDERED_LIST_ITEM_GROUP
import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = "\n"

    private val MARKDOWN_GROUPS = ParseGroup.values().toMarkdownGroups()

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }
    private val elementsRegex by lazy { elementsPattern.toRegex() }

    /**
     * parse markdown text to elements
     */
    fun parse(string: String): MarkdownText =
        MarkdownText(findElements(string))

    /**
     * clear markdown text to string without markdown characters
     */
    fun clear(string: String): String {
        return elementsRegex.replace(string) {
            it.value.cleared()
        }
    }

    private fun String.cleared(): String {
        val parseGroup = ParseGroup.values().find { parseGroup -> parseGroup.regex.matches(this) } ?: return this
        val clearedString = when(parseGroup) {
            UNORDERED_LIST_ITEM_GROUP -> this.substring(2)
            HEADER_GROUP -> {
                val reg = "^#{1,6}".toRegex().find(this)
                val level = reg!!.value.length
                this.substring(level.inc())
            }
            QUOTE_GROUP -> this.substring(2)
            RULE_GROUP -> " "
            STRIKE_GROUP -> this.substring(2, this.length - 2)
            INLINE_GROUP -> this.substring(1, this.length - 1)
            LINK_GROUP -> {
                val (title:String, _:String) = "\\[(.*)]\\((.*)\\)".toRegex().find(this)!!.destructured
                title
            }
            ITALIC_GROUP -> this.substring(1, this.length - 1)
            BOLD_GROUP -> this.substring(2, this.length - 2)
            BLOCK_CODE_GROUP -> this.substring(3, this.length - 3)
            ORDERED_LIST_ITEM_GROUP -> {
                val (_, title) = "(^\\d+\\.) (.*)\$".toRegex().find(this)!!.destructured
                title
            }
            else -> this
        }
        if (elementsRegex.find(clearedString) != null) {
            return clear(clearedString)
        }
        return clearedString
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

data class MarkdownText(val elements: List<Element>)

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