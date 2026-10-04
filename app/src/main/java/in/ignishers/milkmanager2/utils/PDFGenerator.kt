package `in`.ignishers.milkmanager2.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import `in`.ignishers.milkmanager2.models.Customer
import `in`.ignishers.milkmanager2.models.DailyTransaction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PDFGenerator(
    private val context: Context?,
    private val customer: Customer,
    private val transactions: MutableList<DailyTransaction>,
    private val initialOpeningBalance: Double,
    private val headerTotalDue: Double
) {
    // Colors
    private val colorPrimary = Color.parseColor("#0288D1")
    private val colorPrimaryDark = Color.parseColor("#01579B")
    private val colorAccent = Color.parseColor("#03A9F4")
    private val colorBgAlt = Color.parseColor("#E1F5FE")
    private val colorTextDark = Color.parseColor("#212121")
    private val colorTextLight = Color.parseColor("#757575")
    private val colorBorder = Color.parseColor("#BDBDBD")
    private val colorWhite = Color.WHITE

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPrimaryDark
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorWhite
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorTextDark
        textSize = 8.5f
    }

    private val boldTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorTextDark
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    
    private val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPrimaryDark
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    
    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorTextLight
        textSize = 8f
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorBorder
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val monthHeaderFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    private class DayData {
        var mornQ = 0.0
        var mornA = 0.0
        var eveQ = 0.0
        var eveA = 0.0
        var extQ = 0.0
        var extA = 0.0
        var payA = 0.0
    }

    fun generate(fileName: String): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()

        // 1. Pre-calculate Opening Balances
        val sortedTrans = transactions.sortedBy { it.date ?: "" }
        val sortedGroup = sortedTrans.groupBy { it.date!!.substring(0, 7) }
        
        val monthStartBalances = mutableMapOf<String, Double>()
        var currentBal = initialOpeningBalance

        for ((monthKey, monthTrans) in sortedGroup) {
            monthStartBalances[monthKey] = currentBal
            var bill = 0.0
            var pay = 0.0
            for (t in monthTrans) {
                if (t.session?.startsWith("Payment") == true) pay += t.amount?.toDouble() ?: 0.0
                else bill += t.amount?.toDouble() ?: 0.0
            }
            currentBal += (bill - pay)
        }

        // 2. Iterate Months
        for ((monthKey, monthTrans) in sortedGroup) {
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            drawPageContent(canvas, monthKey, monthTrans, monthStartBalances[monthKey] ?: 0.0)

            document.finishPage(page)
        }

        // Save
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var file = File(downloadsDir, fileName)
        var counter = 1
        val baseName = if (fileName.endsWith(".pdf")) fileName.substring(0, fileName.length - 4) else fileName

        while (file.exists()) {
            file = File(downloadsDir, "$baseName($counter).pdf")
            counter++
        }

        try {
            document.writeTo(FileOutputStream(file))
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            document.close()
        }
    }

    private fun drawPageContent(
        canvas: Canvas,
        monthKey: String,
        monthTrans: List<DailyTransaction>,
        startBal: Double
    ) {
        // --- HEADER ---
        var y = 40f
        
        // App/Shop Title inside a rounded box
        fillPaint.color = colorBgAlt
        canvas.drawRoundRect(MARGIN, 20f, PAGE_WIDTH - MARGIN, 65f, 10f, 10f, fillPaint)
        canvas.drawRoundRect(MARGIN, 20f, PAGE_WIDTH - MARGIN, 65f, 10f, 10f, borderPaint)
        canvas.drawText("MILK BILL STATEMENT", PAGE_WIDTH / 2f, 48f, titlePaint)

        y = 85f
        // Customer Info (Left)
        canvas.drawText("Customer: ${customer.name}", MARGIN + 5, y, boldTextPaint)
        canvas.drawText("Mobile: ${customer.mobile}", MARGIN + 5, y + 15, textPaint)
        val addr = if (customer.address.isNullOrEmpty()) "N/A" else customer.address
        canvas.drawText("Address: $addr", MARGIN + 5, y + 30, textPaint)

        // Month Info (Right)
        val monthDate = LocalDate.parse("$monthKey-01")
        val monthTitle = monthDate.format(monthHeaderFormatter)
        
        val rightAlign = Paint(boldTextPaint).apply {
            textAlign = Paint.Align.RIGHT
            textSize = 12f
            color = colorPrimaryDark
        }
        canvas.drawText("Month: $monthTitle", PAGE_WIDTH - MARGIN - 5, y + 15, rightAlign)

        // --- TABLE ---
        y = 135f
        val tableTop = y
        val rowHeight = 26f
        val headerHeight = 20f
        
        // Draw Header Bg
        fillPaint.color = colorPrimary
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + headerHeight, fillPaint)
        
        // Draw Header Text
        val headerY = y + 14f
        drawRowText(canvas, headerY, "Date", "Morn", "Eve", "Extra", "Pay", headerPaint, true)
        drawRowText(canvas, headerY, "Date", "Morn", "Eve", "Extra", "Pay", headerPaint, false)

        y += headerHeight

        // Prepare Day Data
        val daysInMonth = monthDate.lengthOfMonth()
        val days = Array(32) { DayData() }
        
        for (t in monthTrans) {
            val dayOfMonth = t.date!!.substring(8, 10).toInt()
            val d = days[dayOfMonth]
            val amt = t.amount?.toDouble() ?: 0.0
            val qty = t.quantity?.toDouble() ?: 0.0
            
            when {
                t.session?.startsWith("Payment") == true -> d.payA += amt
                t.session.equals("Morning", true) -> { d.mornQ += qty; d.mornA += amt }
                t.session.equals("Evening", true) -> { d.eveQ += qty; d.eveA += amt }
                else -> { d.extQ += qty; d.extA += amt }
            }
        }

        // Draw Rows
        val maxRows = 16
        for (i in 1..maxRows) {
            val rowY = y + (i - 1) * rowHeight
            
            // Alternate Bg
            if (i % 2 == 0) {
                fillPaint.color = colorBgAlt
                canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, fillPaint)
            }

            // Left Side (Day i)
            drawDayCell(canvas, days[i], i, rowY, true)
            
            // Right Side (Day i + 16)
            if (i + 16 <= daysInMonth) {
                drawDayCell(canvas, days[i + 16], i + 16, rowY, false)
            }
            
            // Horizontal line
            canvas.drawLine(MARGIN, rowY + rowHeight, PAGE_WIDTH - MARGIN, rowY + rowHeight, borderPaint)
        }
        
        val tableBottom = y + maxRows * rowHeight
        
        // Draw Grid Lines (Vertical)
        canvas.drawRect(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableBottom, borderPaint) // Outer
        val midX = PAGE_WIDTH / 2f
        canvas.drawLine(midX, tableTop, midX, tableBottom, borderPaint) // Center Split
        
        // Left Column Dividers
        canvas.drawLine(MARGIN + 30f, tableTop, MARGIN + 30f, tableBottom, borderPaint)
        canvas.drawLine(MARGIN + 85f, tableTop, MARGIN + 85f, tableBottom, borderPaint)
        canvas.drawLine(MARGIN + 140f, tableTop, MARGIN + 140f, tableBottom, borderPaint)
        canvas.drawLine(MARGIN + 195f, tableTop, MARGIN + 195f, tableBottom, borderPaint)

        // Right Column Dividers
        val rm = midX + 5f
        canvas.drawLine(rm + 30f, tableTop, rm + 30f, tableBottom, borderPaint)
        canvas.drawLine(rm + 85f, tableTop, rm + 85f, tableBottom, borderPaint)
        canvas.drawLine(rm + 140f, tableTop, rm + 140f, tableBottom, borderPaint)
        canvas.drawLine(rm + 195f, tableTop, rm + 195f, tableBottom, borderPaint)

        // --- SUMMARY SECTION ---
        y = tableBottom + 20f
        
        // Calculate Totals
        var totalMilk = 0.0
        var totalBill = 0.0
        var totalPay = 0.0
        for (i in 1..daysInMonth) {
            val d = days[i]
            totalMilk += d.mornQ + d.eveQ + d.extQ
            totalBill += d.mornA + d.eveA + d.extA
            totalPay += d.payA
        }
        
        val finalDue = startBal + totalBill - totalPay

        // Summary Box
        val sumBoxTop = y
        fillPaint.color = colorBgAlt
        canvas.drawRoundRect(MARGIN, sumBoxTop, PAGE_WIDTH - MARGIN, sumBoxTop + 110f, 10f, 10f, fillPaint)
        canvas.drawRoundRect(MARGIN, sumBoxTop, PAGE_WIDTH - MARGIN, sumBoxTop + 110f, 10f, 10f, borderPaint)
        
        // Summary Header
        fillPaint.color = colorPrimary
        canvas.drawRoundRect(MARGIN, sumBoxTop, PAGE_WIDTH - MARGIN, sumBoxTop + 25f, 10f, 10f, fillPaint)
        canvas.drawRect(MARGIN, sumBoxTop + 15f, PAGE_WIDTH - MARGIN, sumBoxTop + 25f, fillPaint)
        
        val summaryTitlePaint = Paint(headerPaint).apply { textSize = 11f; textAlign = Paint.Align.CENTER }
        canvas.drawText("MONTHLY SUMMARY", PAGE_WIDTH / 2f, sumBoxTop + 16f, summaryTitlePaint)
        
        y += 45f
        val col1X = MARGIN + 15f
        val col2X = PAGE_WIDTH / 2f + 15f
        
        canvas.drawText("Previous Dues:", col1X, y, boldTextPaint)
        canvas.drawText(String.format("₹ %.2f", startBal), col1X + 110f, y, textPaint)
        
        canvas.drawText("Total Milk:", col2X, y, boldTextPaint)
        canvas.drawText(formatMilk(totalMilk), col2X + 110f, y, textPaint)
        
        y += 20f
        canvas.drawText("This Month Bill:", col1X, y, boldTextPaint)
        canvas.drawText(String.format("+ ₹ %.2f", totalBill), col1X + 110f, y, textPaint)
        
        canvas.drawText("Payment Received:", col2X, y, boldTextPaint)
        canvas.drawText(String.format("- ₹ %.2f", totalPay), col2X + 110f, y, textPaint)
        
        y += 25f
        // Final Line
        canvas.drawLine(MARGIN, y - 15f, PAGE_WIDTH - MARGIN, y - 15f, borderPaint)
        
        val finalPaint = Paint(boldTextPaint).apply { textSize = 14f; color = colorPrimaryDark }
        canvas.drawText("FINAL DUE AMOUNT:", col1X, y, finalPaint)
        
        val finalAmtPaint = Paint(amountPaint).apply { textSize = 16f; textAlign = Paint.Align.RIGHT }
        canvas.drawText(String.format("₹ %.2f", finalDue), PAGE_WIDTH - MARGIN - 15f, y, finalAmtPaint)
        
        // Watermark
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorBorder
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            textAlign = Paint.Align.CENTER
            alpha = 60
        }
        canvas.drawText("IGNISHERS", PAGE_WIDTH / 2f, PAGE_HEIGHT - 30f, watermarkPaint)
    }

    private fun drawRowText(
        canvas: Canvas, y: Float,
        date: String, morn: String, eve: String, ext: String, pay: String,
        paint: Paint, isLeft: Boolean
    ) {
        val startX = if (isLeft) MARGIN else (PAGE_WIDTH / 2f) + 5f
        canvas.drawText(date, startX + 5f, y, paint)
        canvas.drawText(morn, startX + 35f, y, paint)
        canvas.drawText(eve, startX + 90f, y, paint)
        canvas.drawText(ext, startX + 145f, y, paint)
        canvas.drawText(pay, startX + 200f, y, paint)
    }

    private fun drawDayCell(
        canvas: Canvas, d: DayData, day: Int, rowY: Float, isLeft: Boolean
    ) {
        val startX = if (isLeft) MARGIN else (PAGE_WIDTH / 2f) + 5f
        
        // Date
        val dateDay = String.format("%02d", day)
        canvas.drawText(dateDay, startX + 7f, rowY + 17f, boldTextPaint)
        
        fun drawCell(qty: Double, amt: Double, xOffset: Float) {
            if (qty > 0 || amt > 0) {
                canvas.drawText(formatMilk(qty), startX + xOffset, rowY + 11f, textPaint)
                canvas.drawText(String.format("₹%.0f", amt), startX + xOffset, rowY + 22f, amountPaint)
            } else {
                canvas.drawText("-", startX + xOffset + 20f, rowY + 17f, smallTextPaint)
            }
        }
        
        drawCell(d.mornQ, d.mornA, 32f)
        drawCell(d.eveQ, d.eveA, 87f)
        drawCell(d.extQ, d.extA, 142f)
        
        if (d.payA > 0) {
            canvas.drawText(String.format("₹%.0f", d.payA), startX + 197f, rowY + 17f, amountPaint)
        } else {
            canvas.drawText("-", startX + 225f, rowY + 17f, smallTextPaint)
        }
    }

    private fun formatMilk(qty: Double): String {
        return if (qty >= 1.0) String.format("%.1f L", qty)
        else String.format("%d ml", (qty * 1000).toInt())
    }

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 20f
    }
}