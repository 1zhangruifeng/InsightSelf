package com.example.insightself.utils

/**
 * Parses a small subset of Markdown used by AI reports: headings (# .. ###) and **bold**.
 */
object ReportMarkdownParser {

    data class TextSpan(val text: String, val bold: Boolean)

    data class StyledLine(
        val spans: List<TextSpan>,
        val headingLevel: Int
    )

    fun parse(content: String): List<StyledLine> {
        if (content.isBlank()) return emptyList()
        return content.lineSequence()
            .map { parseLine(it) }
            .filter { it.spans.any { span -> span.text.isNotBlank() } || it.headingLevel > 0 }
            .toList()
    }

    private fun parseLine(raw: String): StyledLine {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return StyledLine(listOf(TextSpan("", false)), headingLevel = 0)
        }

        val headingMatch = Regex("^(#{1,3})\\s+(.+)$").find(trimmed)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val body = headingMatch.groupValues[2].trim()
            return StyledLine(parseInlineSpans(body), headingLevel = level)
        }

        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length.coerceIn(1, 3)
            val body = trimmed.drop(level).trim().trimStart('-', '*').trim()
            return StyledLine(parseInlineSpans(body), headingLevel = level)
        }

        return StyledLine(parseInlineSpans(trimmed), headingLevel = 0)
    }

    fun parseInlineSpans(text: String): List<TextSpan> {
        if (text.isEmpty()) return listOf(TextSpan("", false))
        val spans = mutableListOf<TextSpan>()
        val pattern = Regex("\\*\\*(.+?)\\*\\*")
        var cursor = 0
        for (match in pattern.findAll(text)) {
            if (match.range.first > cursor) {
                spans.add(TextSpan(text.substring(cursor, match.range.first), bold = false))
            }
            spans.add(TextSpan(match.groupValues[1], bold = true))
            cursor = match.range.last + 1
        }
        if (cursor < text.length) {
            spans.add(TextSpan(text.substring(cursor), bold = false))
        }
        if (spans.isEmpty()) {
            spans.add(TextSpan(text, bold = false))
        }
        return spans
    }
}
