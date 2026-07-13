package com.example.insightself.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ReportExporter(private val context: Context) {

    suspend fun exportAsPdf(content: String, title: String): File? = withContext(Dispatchers.IO) {
        runCatching {
            val lines = ReportMarkdownParser.parse(content)
            val pageWidth = 595
            val pageHeight = 842
            val renderer = ReportMarkdownCanvasRenderer(
                marginLeft = 50f,
                marginRight = 50f,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                topMargin = 56f,
                bottomMargin = 56f
            )

            val pdfDocument = PdfDocument()
            renderer.renderPdf(pdfDocument, title, lines)

            val fileName = "InsightSelf_Report_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            file
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

    suspend fun exportAsImage(content: String, title: String): File? = withContext(Dispatchers.IO) {
        runCatching {
            val lines = ReportMarkdownParser.parse(content)
            val width = 1080
            val renderer = ReportMarkdownCanvasRenderer(
                marginLeft = 50f,
                marginRight = 50f,
                pageWidth = width,
                pageHeight = 1920,
                topMargin = 80f,
                bottomMargin = 80f
            )

            val titleBlockHeight = 130
            val contentHeight = renderer.measureContentHeight(lines)
            val height = (titleBlockHeight + contentHeight).coerceIn(900, 12000)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)

            var y = renderer.drawTitle(canvas, 80f, title)
            y = renderer.drawAccentLine(canvas, y)
            renderer.drawOnCanvas(canvas, y, lines)

            val fileName = "InsightSelf_Report_${System.currentTimeMillis()}.png"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            file
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

    fun shareFile(file: File) {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, when (file.extension) {
                "pdf" -> "application/pdf"
                else -> "image/png"
            })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "打开报告"))
    }
}
