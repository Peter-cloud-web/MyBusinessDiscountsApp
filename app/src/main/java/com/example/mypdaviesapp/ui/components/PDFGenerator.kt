package com.example.mypdaviesapp.ui.components

import android.content.Context
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
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "barcodes_${System.currentTimeMillis()}.pdf")
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

            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}