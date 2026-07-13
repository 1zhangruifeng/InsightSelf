package com.example.insightself.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan

internal class ReportMarkdownCanvasRenderer(
    private val marginLeft: Float,
    private val marginRight: Float,
    private val pageWidth: Int,
    private val pageHeight: Int,
    private val topMargin: Float,
    private val bottomMargin: Float
) {
    private val contentWidth: Int
        get() = (pageWidth - marginLeft - marginRight).toInt().coerceAtLeast(1)

    fun measureContentHeight(lines: List<ReportMarkdownParser.StyledLine>): Int {
        var height = 0f
        for (line in lines) {
            height += lineBlockHeight(line)
        }
        return height.toInt() + topMargin.toInt() + bottomMargin.toInt()
    }

    fun drawTitle(canvas: Canvas, y: Float, title: String): Float {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title, marginLeft, y, paint)
        return y + 40f
    }

    fun drawAccentLine(canvas: Canvas, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5B67FF")
            strokeWidth = 3f
        }
        canvas.drawLine(marginLeft, y, pageWidth - marginRight, y, paint)
        return y + 24f
    }

    fun drawOnCanvas(
        canvas: Canvas,
        startY: Float,
        lines: List<ReportMarkdownParser.StyledLine>
    ): Float {
        var y = startY
        for (line in lines) {
            val layout = buildLayout(line)
            canvas.save()
            canvas.translate(marginLeft, y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + lineSpacingAfter(line)
        }
        return y
    }

    fun renderPdf(pdfDocument: PdfDocument, title: String, lines: List<ReportMarkdownParser.StyledLine>) {
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = topMargin

        y = drawTitle(canvas, y, title)
        y = drawAccentLine(canvas, y)

        for (line in lines) {
            val blockHeight = lineBlockHeight(line)
            if (y + blockHeight > pageHeight - bottomMargin) {
                pdfDocument.finishPage(page)
                pageNumber += 1
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = topMargin
            }
            val layout = buildLayout(line)
            canvas.save()
            canvas.translate(marginLeft, y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + lineSpacingAfter(line)
        }

        pdfDocument.finishPage(page)
    }

    private fun lineBlockHeight(line: ReportMarkdownParser.StyledLine): Float {
        return buildLayout(line).height + lineSpacingAfter(line)
    }

    private fun lineSpacingAfter(line: ReportMarkdownParser.StyledLine): Float {
        return when (line.headingLevel) {
            1 -> 18f
            2 -> 14f
            3 -> 12f
            else -> 10f
        }
    }

    private fun buildLayout(line: ReportMarkdownParser.StyledLine): StaticLayout {
        val textSize = when (line.headingLevel) {
            1 -> 22f
            2 -> 18f
            3 -> 16f
            else -> 14f
        }
        val textColor = if (line.headingLevel > 0) Color.BLACK else Color.DKGRAY
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            this.textSize = textSize
        }
        val spannable = buildSpannable(line)
        return StaticLayout.Builder.obtain(spannable, 0, spannable.length, paint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(true)
            .build()
    }

    private fun buildSpannable(line: ReportMarkdownParser.StyledLine): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val headingBold = line.headingLevel > 0
        for (span in line.spans) {
            if (span.text.isEmpty()) continue
            val start = builder.length
            builder.append(span.text)
            if (span.bold || headingBold) {
                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return builder
    }
}
