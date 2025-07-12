package com.example.mypdaviesapp.ui.components

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Environment
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File

class PDFGenerator(private val context: Context) {

    fun generateBarcodePDF(barcodes: List<String>, businessName: String = "Carpet Cleaning Pro"): String? {
        return try {
            // Create the file in the public Documents directory
            val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "CarpetCleaningPro")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val fileName = "barcodes_${System.currentTimeMillis()}.pdf"
            val file = File(documentsDir, fileName)

            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Add title
            val title = Paragraph(businessName)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20f)
                .setBold()
            document.add(title)

            val subtitle = Paragraph("Carpet Cleaning Tags - DO NOT REMOVE")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12f)
                .setMarginBottom(20f)
            document.add(subtitle)

            // Create table with 3 columns
            val table = Table(3)
            table.setWidth(UnitValue.createPercentValue(100f))

            barcodes.forEachIndexed { index, barcode ->
                val cell = Cell()
                cell.add(Paragraph(barcode).setTextAlignment(TextAlignment.CENTER).setFontSize(10f))
                cell.add(Paragraph("Scan Count: ___/10").setTextAlignment(TextAlignment.CENTER).setFontSize(8f))
                cell.setPadding(10f)
                cell.setBorder(SolidBorder(1f))
                table.addCell(cell)
            }

            document.add(table)
            document.close()

            // Make the file visible to media scanner and file managers
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("application/pdf"),
                null
            )

            // Also notify the system about the new file
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = android.net.Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace() // This will help you debug if there are any issues
            null
        }
    }

    fun shareBarcodePDF(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/pdf"
                shareIntent.putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(file))
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Carpet Cleaning Barcodes")
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Generated carpet cleaning barcode tags")

                val chooserIntent = Intent.createChooser(shareIntent, "Share Barcode PDF")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}