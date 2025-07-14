package com.example.mypdaviesapp.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.core.content.ContextCompat
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.ByteArrayOutputStream
import java.io.File

class PDFGenerator(private val context: Context) {

    fun generateBarcodePDF(
        barcodes: List<String>,
        businessName:String,
        logoResourceId: Int? = null, // Pass your drawable resource ID here
        useQRCode: Boolean = true
    ): String? {
        return try {
            // Create the file in the public Documents directory
            val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PdaviesCleaningCompany")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val fileName = "pdavies_carpet_tags_${System.currentTimeMillis()}.pdf"
            val file = File(documentsDir, fileName)

            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Company colors
            val primaryColor = DeviceRgb(0, 102, 204) // Professional blue
            val secondaryColor = DeviceRgb(51, 51, 51) // Dark gray
            val accentColor = DeviceRgb(220, 20, 60) // Crimson red for important text

            // Add header with logo and company info
            val headerTable = Table(2)
            headerTable.setWidth(UnitValue.createPercentValue(100f))

            // Logo cell
            val logoCell = Cell()
            logoCell.setBorder(null)
            logoCell.setVerticalAlignment(VerticalAlignment.MIDDLE)

            // Use the pdavieslogo.png from drawable folder
            try {
                val logoResourceName = "pdavieslogo" // without .png extension
                val logoResourceId = context.resources.getIdentifier(logoResourceName, "drawable", context.packageName)
                if (logoResourceId != 0) {
                    val logoBitmap = BitmapFactory.decodeResource(context.resources, logoResourceId)
                    if (logoBitmap != null) {
                        val logoBytes = bitmapToByteArray(logoBitmap)
                        val logoImageData = ImageDataFactory.create(logoBytes)
                        val logoImage = Image(logoImageData)
                        logoImage.setWidth(70f)
                        logoImage.setHeight(70f)
                        logoCell.add(logoImage)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Company info cell
            val infoCell = Cell()
            infoCell.setBorder(null)
            infoCell.setVerticalAlignment(VerticalAlignment.MIDDLE)

            val companyName = Paragraph("PDAVIES CLEANING COMPANY")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(20f)
                .setBold()
                .setFontColor(primaryColor)
                .setMarginBottom(5f)
            infoCell.add(companyName)

            val tagline = Paragraph("Professional Cleaning Services")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12f)
                .setFontColor(secondaryColor)
                .setMarginBottom(5f)
            infoCell.add(tagline)

            val phoneNumbers = Paragraph("📞 0759489245 / 0716986935")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12f)
                .setFontColor(primaryColor)
                .setBold()
            infoCell.add(phoneNumbers)

            headerTable.addCell(logoCell)
            headerTable.addCell(infoCell)
            document.add(headerTable)

            // Add separator line
            val separator = Paragraph("━".repeat(60))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(primaryColor)
                .setMarginTop(15f)
                .setMarginBottom(15f)
            document.add(separator)

            // Add instruction text
            val instructions = Paragraph("CARPET CLEANING TAGS - DO NOT REMOVE")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14f)
                .setBold()
                .setFontColor(accentColor)
                .setMarginBottom(8f)
            document.add(instructions)

            val subInstructions = Paragraph("Call for assistance, information or to schedule a pick up with your special code")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(11f)
                .setFontColor(secondaryColor)
                .setMarginBottom(25f)
            document.add(subInstructions)

            // Create table with 3 columns for compact layout
            val table = Table(3)
            table.setWidth(UnitValue.createPercentValue(100f))

            barcodes.forEach { barcode ->
                val cell = Cell()
                cell.setPadding(8f)
                cell.setBorder(SolidBorder(primaryColor, 1.5f))
                cell.setBackgroundColor(DeviceRgb(248, 248, 248)) // Very light gray background

                // Add company logo to each tag (smaller)
                try {
                    val logoResourceName = "pdavieslogo"
                    val logoResourceId = context.resources.getIdentifier(logoResourceName, "drawable", context.packageName)
                    if (logoResourceId != 0) {
                        val logoBitmap = BitmapFactory.decodeResource(context.resources, logoResourceId)
                        if (logoBitmap != null) {
                            val logoBytes = bitmapToByteArray(logoBitmap)
                            val logoImageData = ImageDataFactory.create(logoBytes)
                            val logoImage = Image(logoImageData)
                            logoImage.setWidth(25f)
                            logoImage.setHeight(25f)
                            logoImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
                            cell.add(logoImage)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Company name in tag (smaller font)
                val tagCompanyName = Paragraph("PDAVIES CLEANING")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8f)
                    .setBold()
                    .setFontColor(primaryColor)
                    .setMarginTop(2f)
                    .setMarginBottom(4f)
                cell.add(tagCompanyName)

                // Add barcode/QR code image (smaller)
                val bitmap = if (useQRCode) {
                    BarcodeGenerator.generateQRCodeBitmap(barcode, 100, 100)
                } else {
                    BarcodeGenerator.generateBarcodeBitmap(barcode, 120, 50)
                }

                if (bitmap != null) {
                    val imageBytes = bitmapToByteArray(bitmap)
                    val imageData = ImageDataFactory.create(imageBytes)
                    val image = Image(imageData)
                    image.setWidth(if (useQRCode) 55f else 80f)
                    image.setHeight(if (useQRCode) 55f else 35f)
                    image.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
                    cell.add(image)
                }

                // Add special code text (compact)
                val barcodeText = Paragraph(barcode)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9f)
                    .setBold()
                    .setFontColor(ColorConstants.BLACK)
                    .setMarginTop(3f)
                    .setMarginBottom(4f)
                cell.add(barcodeText)

                // Add contact information (compact)
                val phoneContact = Paragraph("📞 0759 489245 / 0716 986935")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(5f)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setMarginBottom(2f)
                cell.add(phoneContact)

                val serviceNote = Paragraph("Call for assistance, information or to schedule a pick up")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(6f)
                    .setFontColor(secondaryColor)
                    .setMarginBottom(2f)
                cell.add(serviceNote)

                val professionalTag = Paragraph("This is your carpet's identification tag. Thank you for choosing Pdavies Cleaning.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(6f)
                    .setItalic()
                    .setFontColor(primaryColor)
                cell.add(professionalTag)

                table.addCell(cell)
            }

            document.add(table)

            // Add footer
            val footer = Paragraph("Thank you for choosing Pdavies Cleaning Company - Your trusted carpet cleaning professionals!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(11f)
                .setFontColor(primaryColor)
                .setBold()
                .setMarginTop(20f)
            document.add(footer)

            document.close()

            // Make the file visible to media scanner and file managers
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("application/pdf"),
                null
            )

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun shareBarcodePDF(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "application/pdf"
                shareIntent.putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(file))
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Pdavies Cleaning Company - Carpet Tags")
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Professional carpet cleaning tags from Pdavies Cleaning Company")

                val chooserIntent = Intent.createChooser(shareIntent, "Share Carpet Tags PDF")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}