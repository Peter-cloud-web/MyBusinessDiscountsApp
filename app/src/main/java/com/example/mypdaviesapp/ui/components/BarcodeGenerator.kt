package com.example.mypdaviesapp.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import java.util.*

object BarcodeGenerator {

    /**
     * Generate a QR Code bitmap (recommended for alphanumeric codes)
     */
    fun generateQRCodeBitmap(content: String, width: Int = 200, height: Int = 200): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate a traditional barcode (CODE_128) - works best with numeric codes
     */
    fun generateBarcodeBitmap(content: String, width: Int = 300, height: Int = 100): Bitmap? {
        return try {
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate scannable carpet cleaning codes
     * Format: CC-YYYYMMDD-NNNN (e.g., CC-20250712-0001)
     */
    fun generateCarpetCleaningCodes(count: Int): List<String> {
        val codes = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))

        for (i in 1..count) {
            val sequenceNumber = String.format("%04d", i)
            val code = "CC-$year$month$day-$sequenceNumber"
            codes.add(code)
        }
        return codes
    }

    /**
     * Generate simple numeric codes (works best with traditional barcodes)
     */
    fun generateNumericCodes(count: Int, prefix: String = "CC"): List<String> {
        val codes = mutableListOf<String>()
        val timestamp = System.currentTimeMillis().toString().takeLast(6)

        for (i in 1..count) {
            val sequenceNumber = String.format("%04d", i)
            val code = "$prefix$timestamp$sequenceNumber"
            codes.add(code)
        }
        return codes
    }

    /**
     * Generate UUID-based codes (best for uniqueness, use with QR codes)
     */
    fun generateUUIDCodes(count: Int, prefix: String = "CC"): List<String> {
        val codes = mutableListOf<String>()

        for (i in 1..count) {
            val uuid = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
            val code = "$prefix-$uuid"
            codes.add(code)
        }
        return codes
    }
}