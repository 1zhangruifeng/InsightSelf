package com.example.insightself.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportMarkdownParserTest {

    @Test
    fun parseHeading_stripsHashAndMarksLevel() {
        val lines = ReportMarkdownParser.parse("## Conclusion\n\nBody text")
        assertEquals(2, lines.size)
        assertEquals(2, lines[0].headingLevel)
        assertEquals("Conclusion", lines[0].spans.joinToString("") { it.text })
        assertTrue(lines[0].spans.all { it.bold || lines[0].headingLevel > 0 })
        assertEquals(0, lines[1].headingLevel)
        assertEquals("Body text", lines[1].spans.single().text)
    }

    @Test
    fun parseInlineBold_splitsSpans() {
        val spans = ReportMarkdownParser.parseInlineSpans("Score is **high** today")
        assertEquals(3, spans.size)
        assertEquals("Score is ", spans[0].text)
        assertEquals(false, spans[0].bold)
        assertEquals("high", spans[1].text)
        assertEquals(true, spans[1].bold)
        assertEquals(" today", spans[2].text)
    }
}
