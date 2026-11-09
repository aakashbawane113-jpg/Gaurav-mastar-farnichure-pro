package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.model.CuttingPart
import com.example.model.Estimation
import com.example.model.FurnitureConfig
import com.example.model.FurnitureProject
import com.example.model.OptimizationResult
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun generateQuotationPdf(
        context: Context,
        project: FurnitureProject,
        config: FurnitureConfig,
        parts: List<CuttingPart>,
        estimation: Estimation
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72 dpi
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(29, 78, 216) // Royal Blue
            textSize = 18f
            isFakeBoldText = true
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 16f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 65, 85)
            textSize = 10f
        }
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            isFakeBoldText = true
        }

        // 1. Header Banner (Beautiful Blue and White Theme)
        paint.color = Color.rgb(29, 78, 216) // Royal Blue
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Accent cyan/ice blue stripe
        paint.color = Color.rgb(2, 132, 199)
        canvas.drawRect(0f, 72f, 595f, 76f, paint)

        canvas.drawText("GAURAV FURNITURE PRO", 24f, 34f, headerPaint)
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(224, 242, 254)
            textSize = 9.5f
        }
        canvas.drawText("Gaurav Furniture Pro • Precision Modular Design, Cutting & Cost Estimation", 24f, 52f, subHeaderPaint)

        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(project.createdAt))
        val quoteNum = "Q-${project.id.toString().padStart(4, '0')}"
        val rightAlignPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(254, 243, 199)
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            isFakeBoldText = true
        }
        canvas.drawText("QUOTATION: $quoteNum", 571f, 32f, rightAlignPaint)
        canvas.drawText("Date: $dateStr", 571f, 50f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            textSize = 9f
            textAlign = Paint.Align.RIGHT
        })

        // 2. Customer & Project Info Box
        var curY = 95f
        paint.color = Color.rgb(239, 246, 255) // Soft ice blue tint
        val infoRect = RectF(24f, curY, 571f, curY + 65f)
        canvas.drawRoundRect(infoRect, 6f, 6f, paint)

        paint.color = Color.rgb(191, 219, 254)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(infoRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        canvas.drawText("Client Information:", 36f, curY + 18f, boldBodyPaint)
        canvas.drawText("Name: ${project.customerName}", 36f, curY + 34f, bodyPaint)
        canvas.drawText("Mobile: ${project.customerPhone.ifEmpty { "N/A" }}", 36f, curY + 48f, bodyPaint)

        canvas.drawText("Project Details:", 320f, curY + 18f, boldBodyPaint)
        canvas.drawText("Project: ${project.projectName}", 320f, curY + 34f, bodyPaint)
        canvas.drawText("Site: ${project.customerAddress.ifEmpty { "Standard Site Installation" }}", 320f, curY + 48f, bodyPaint)

        curY += 80f

        // 3. Furniture Specifications Section
        canvas.drawText("FURNITURE SPECIFICATIONS & 3D SCHEMATIC", 24f, curY, titlePaint.apply { textSize = 12f })
        paint.color = Color.rgb(29, 78, 216)
        canvas.drawRect(24f, curY + 4f, 180f, curY + 6f, paint)

        curY += 18f

        // Draw small 3D schematic box
        val schemLeft = 24f
        val schemTop = curY
        val schemW = 150f
        val schemH = 110f
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(schemLeft, schemTop, schemLeft + schemW, schemTop + schemH), 4f, 4f, paint)
        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(RectF(schemLeft, schemTop, schemLeft + schemW, schemTop + schemH), 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        // Draw 3D Isometric schematic cabinet
        paint.color = Color.rgb(29, 78, 216) // Royal blue lines
        paint.strokeWidth = 1.5f
        paint.style = Paint.Style.STROKE

        val cx = schemLeft + 60f
        val cy = schemTop + 75f
        val wIso = 45f
        val hIso = 55f
        val dIso = 28f
        // Front face
        canvas.drawRect(cx, cy - hIso, cx + wIso, cy, paint)
        // Top face
        canvas.drawLine(cx, cy - hIso, cx + dIso, cy - hIso - (dIso * 0.5f), paint)
        canvas.drawLine(cx + wIso, cy - hIso, cx + wIso + dIso, cy - hIso - (dIso * 0.5f), paint)
        canvas.drawLine(cx + dIso, cy - hIso - (dIso * 0.5f), cx + wIso + dIso, cy - hIso - (dIso * 0.5f), paint)
        // Right face
        canvas.drawLine(cx + wIso + dIso, cy - hIso - (dIso * 0.5f), cx + wIso + dIso, cy - (dIso * 0.5f), paint)
        canvas.drawLine(cx + wIso, cy, cx + wIso + dIso, cy - (dIso * 0.5f), paint)
        // Doors split
        if (config.numberOfDoors > 1) {
            val step = wIso / config.numberOfDoors
            for (i in 1 until config.numberOfDoors) {
                canvas.drawLine(cx + (i * step), cy - hIso, cx + (i * step), cy, paint)
            }
        }
        paint.style = Paint.Style.FILL

        // Specifications text next to schematic
        val specLeft = 190f
        canvas.drawText("Category: ${config.category.englishName}", specLeft, curY + 16f, boldBodyPaint)
        canvas.drawText("Overall Size: ${config.widthMm}mm (W) x ${config.heightMm}mm (H) x ${config.depthMm}mm (D)", specLeft, curY + 32f, bodyPaint)
        canvas.drawText("Material / Carcass: ${config.carcassThicknessMm}mm Board, ${config.backThicknessMm}mm Backing", specLeft, curY + 48f, bodyPaint)
        canvas.drawText("Configuration: ${config.numberOfDoors} Doors, ${config.shelvesCount} Shelves, ${config.drawersCount} Drawers", specLeft, curY + 64f, bodyPaint)
        canvas.drawText("Finish: ${config.finish.displayName}", specLeft, curY + 80f, bodyPaint)
        canvas.drawText("Loft: ${if (config.hasLoft) "Included (${config.loftHeightMm}mm)" else "None"}", specLeft, curY + 96f, bodyPaint)

        curY += 125f

        // 4. Cutting & Board Summary
        canvas.drawText("CUTTING & MATERIAL REQUIREMENTS", 24f, curY, titlePaint.apply { textSize = 12f })
        paint.color = Color.rgb(29, 78, 216)
        canvas.drawRect(24f, curY + 4f, 160f, curY + 6f, paint)

        curY += 18f
        // Mini stats
        paint.color = Color.rgb(239, 246, 255)
        canvas.drawRoundRect(RectF(24f, curY, 571f, curY + 36f), 4f, 4f, paint)
        val cutSummary = "Total Cut Panels: ${parts.sumOf { it.quantity }} pcs   |   Estimated 18mm Ply: ${estimation.ply18mmSheets} sheets (8x4)   |   9mm Ply: ${estimation.ply9mmSheets} sheets   |   Edge Banding: ${estimation.edgeBandingMeters.toInt()}m"
        canvas.drawText(cutSummary, 34f, curY + 22f, boldBodyPaint)

        curY += 50f

        // 5. Itemized Cost Estimation Table
        canvas.drawText("DETAILED COST ESTIMATION & SUMMARY", 24f, curY, titlePaint.apply { textSize = 12f })
        paint.color = Color.rgb(29, 78, 216)
        canvas.drawRect(24f, curY + 4f, 160f, curY + 6f, paint)

        curY += 16f

        // Table Header in Royal Blue
        paint.color = Color.rgb(29, 78, 216)
        canvas.drawRect(24f, curY, 571f, curY + 22f, paint)
        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f
            isFakeBoldText = true
        }
        canvas.drawText("Item Description", 34f, curY + 15f, thPaint)
        canvas.drawText("Quantity / Unit", 280f, curY + 15f, thPaint)
        canvas.drawText("Amount (INR)", 490f, curY + 15f, thPaint)

        curY += 22f

        val lineItems = listOf(
            Triple("18mm Plywood / HDHMR Board", "${estimation.ply18mmSheets} sheets (8x4)", estimation.ply18mmCost),
            Triple("9mm Back Panel Plywood", "${estimation.ply9mmSheets} sheets (8x4)", estimation.ply9mmCost),
            Triple("Exterior Decorative Laminate", "${estimation.laminateOutsideSheets} sheets", estimation.laminateOutsideCost),
            Triple("Interior Liner Laminate", "${estimation.laminateInsideSheets} sheets", estimation.laminateInsideCost),
            Triple("PVC Edge Banding Tape (0.8mm & 2mm)", "${estimation.edgeBandingMeters.toInt()} running meters", estimation.edgeBandingCost),
            Triple("Hardware: Soft-close Hinges", "${estimation.hingesCount} pieces", estimation.hingesCost),
            Triple("Hardware: Handles & Telescopic Channels", "${estimation.handlesCount} handles + ${estimation.drawerChannelsPairs} channel pairs", estimation.handlesCost + estimation.drawerChannelsCost),
            Triple("Adhesives, Screws & Assembly Fasteners", "Fevicol & Hardware Set", estimation.fevicolCost + estimation.screwsHardwareCost),
            Triple("Carpentry & Fabrication Labour", "Skilled Workshop & Site Work", estimation.labourCost),
            Triple("Transportation & Site Delivery", "Safe Transit to Client Location", estimation.transportationCost),
            Triple("Site Installation & Leveling", "Professional Fitting Crew", estimation.installationCost)
        )

        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = Color.rgb(30, 41, 59) }
        val rightRowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = Color.rgb(30, 41, 59); textAlign = Paint.Align.RIGHT }

        lineItems.forEachIndexed { idx, item ->
            if (idx % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(24f, curY, 571f, curY + 18f, paint)
            }
            canvas.drawText(item.first, 34f, curY + 13f, rowPaint)
            canvas.drawText(item.second, 280f, curY + 13f, rowPaint)
            canvas.drawText(formatInr(item.third), 560f, curY + 13f, rightRowPaint)
            curY += 18f
        }

        // Subtotal, Profit, GST, Grand Total Box
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(24f, curY + 2f, 571f, curY + 2f, paint)
        curY += 8f

        canvas.drawText("Production & Hardware Cost:", 320f, curY + 10f, bodyPaint)
        canvas.drawText(formatInr(estimation.costOfProduction), 560f, curY + 10f, rightRowPaint)
        curY += 14f

        canvas.drawText("Profit Margin (${estimation.profitMarginPercent.toInt()}%):", 320f, curY + 10f, bodyPaint)
        canvas.drawText(formatInr(estimation.profitAmount), 560f, curY + 10f, rightRowPaint)
        curY += 14f

        canvas.drawText("GST (${estimation.gstPercent.toInt()}%):", 320f, curY + 10f, bodyPaint)
        canvas.drawText(formatInr(estimation.gstAmount), 560f, curY + 10f, rightRowPaint)
        curY += 18f

        // Prominent Grand Total Box in Royal Blue
        val totalBoxRect = RectF(280f, curY, 571f, curY + 40f)
        paint.color = Color.rgb(29, 78, 216)
        canvas.drawRoundRect(totalBoxRect, 6f, 6f, paint)

        val totalTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 11f; isFakeBoldText = true }
        val totalAmtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(254, 240, 138); textSize = 15f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        canvas.drawText("FINAL TOTAL AMOUNT:", 295f, curY + 25f, totalTitlePaint)
        canvas.drawText(formatInr(estimation.grandTotal), 555f, curY + 26f, totalAmtPaint)

        // Advance & Balance breakdown
        curY += 46f
        canvas.drawText("Advance Received: ${formatInr(estimation.advancePaid)}   |   Remaining Balance: ${formatInr(estimation.balanceRemaining)}", 280f, curY + 10f, boldBodyPaint)

        // 6. Terms & Conditions
        val termsY = 740f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(RectF(24f, termsY, 380f, termsY + 65f), 4f, 4f, paint)

        canvas.drawText("Terms & Conditions:", 32f, termsY + 14f, boldBodyPaint.apply { textSize = 8.5f })
        val termsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 7.5f; color = Color.rgb(71, 85, 105) }
        canvas.drawText("1. 50% advance along with confirmed drawing approval to initiate procurement.", 32f, termsY + 26f, termsPaint)
        canvas.drawText("2. 40% payment on delivery of carcass panels at site before installation.", 32f, termsY + 38f, termsPaint)
        canvas.drawText("3. Remaining 10% payment upon final completion & hardware handover.", 32f, termsY + 50f, termsPaint)
        canvas.drawText("4. Delivery timeline: 15-20 working days from confirmation.", 32f, termsY + 62f, termsPaint)

        // Signatory
        canvas.drawText("For GAURAV FURNITURE PRO", 420f, termsY + 20f, boldBodyPaint.apply { textSize = 9.5f })
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(420f, termsY + 55f, 555f, termsY + 55f, paint)
        canvas.drawText("Authorized Signatory", 445f, termsY + 68f, bodyPaint.apply { textSize = 8.5f })

        pdfDoc.finishPage(page)

        // Write to Cache file
        val outputDir = File(context.cacheDir, "quotations").apply { mkdirs() }
        val sanitizedName = project.projectName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val file = File(outputDir, "Quotation_${sanitizedName}_${project.id}.pdf")
        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return file
    }

    fun generateCuttingListPdf(
        context: Context,
        project: FurnitureProject,
        config: FurnitureConfig,
        parts: List<CuttingPart>,
        optimizationResult: OptimizationResult?
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72 dpi
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 15f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 65, 85)
            textSize = 9.5f
        }
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            isFakeBoldText = true
        }

        // 1. Header Banner
        paint.color = Color.rgb(29, 78, 216) // Royal Blue
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Accent gold/cyan stripe
        paint.color = Color.rgb(2, 132, 199)
        canvas.drawRect(0f, 72f, 595f, 76f, paint)

        canvas.drawText("GAURAV FURNITURE PRO — CUTTING LIST & FABRICATION", 24f, 34f, headerPaint)
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(224, 242, 254)
            textSize = 9.5f
        }
        canvas.drawText("Workshop Panel Sizing, Joinery Deductions & Edge Banding Specifications", 24f, 52f, subHeaderPaint)

        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(project.createdAt))
        val rightAlignPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(254, 243, 199)
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            isFakeBoldText = true
        }
        canvas.drawText("JOB: CUT-${project.id.toString().padStart(4, '0')}", 571f, 32f, rightAlignPaint)
        canvas.drawText("Date: $dateStr", 571f, 50f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            textSize = 9f
            textAlign = Paint.Align.RIGHT
        })

        // 2. Project Details Box
        var curY = 92f
        paint.color = Color.rgb(239, 246, 255)
        val infoRect = RectF(24f, curY, 571f, curY + 60f)
        canvas.drawRoundRect(infoRect, 6f, 6f, paint)
        paint.color = Color.rgb(191, 219, 254)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(infoRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        canvas.drawText("Project: ${project.projectName}", 36f, curY + 18f, boldBodyPaint)
        canvas.drawText("Client: ${project.customerName} (${project.customerPhone.ifEmpty { "N/A" }})", 36f, curY + 34f, bodyPaint)
        canvas.drawText("Furniture Specs: ${config.category.englishName} | ${config.widthMm}W x ${config.heightMm}H x ${config.depthMm}D mm", 36f, curY + 48f, bodyPaint)

        val sheetsNeeded = optimizationResult?.totalSheets ?: 3
        val wastePercent = optimizationResult?.averageWastagePercent ?: 12.0
        val optSummary = "Sheet Size: 8x4 ft (2440x1220mm)  |  Sheets Needed: $sheetsNeeded  |  Est. Wastage: ${String.format(Locale.US, "%.1f", wastePercent)}%"
        canvas.drawText(optSummary, 280f, curY + 48f, boldBodyPaint.apply { color = Color.rgb(29, 78, 216) })

        curY += 75f

        // 3. Cutting List Table Header
        paint.color = Color.rgb(29, 78, 216)
        canvas.drawRect(24f, curY, 571f, curY + 22f, paint)
        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9f
            isFakeBoldText = true
        }

        canvas.drawText("#", 32f, curY + 15f, thPaint)
        canvas.drawText("Part Name / Component", 55f, curY + 15f, thPaint)
        canvas.drawText("Length (mm)", 230f, curY + 15f, thPaint)
        canvas.drawText("Width (mm)", 300f, curY + 15f, thPaint)
        canvas.drawText("Thick", 365f, curY + 15f, thPaint)
        canvas.drawText("Qty", 405f, curY + 15f, thPaint)
        canvas.drawText("Material Type", 435f, curY + 15f, thPaint)
        canvas.drawText("Edge Banding", 505f, curY + 15f, thPaint)

        curY += 22f

        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; color = Color.rgb(30, 41, 59) }
        val boldCellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; color = Color.rgb(15, 23, 42); isFakeBoldText = true }

        parts.forEachIndexed { idx, part ->
            if (curY > 740f) return@forEachIndexed // Cap for single page A4 printable table
            if (idx % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(24f, curY, 571f, curY + 18f, paint)
            }
            canvas.drawText("${idx + 1}", 32f, curY + 13f, cellPaint)
            canvas.drawText(part.name.take(28), 55f, curY + 13f, boldCellPaint)
            canvas.drawText("${part.lengthMm}", 230f, curY + 13f, cellPaint)
            canvas.drawText("${part.widthMm}", 300f, curY + 13f, cellPaint)
            canvas.drawText("${part.thicknessMm}mm", 365f, curY + 13f, cellPaint)
            canvas.drawText("${part.quantity}", 405f, curY + 13f, boldCellPaint)
            canvas.drawText(part.materialType.take(13), 435f, curY + 13f, cellPaint)
            canvas.drawText(part.edgeBanding.take(14), 505f, curY + 13f, cellPaint)

            curY += 18f
        }

        // Summary Total Row
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(24f, curY + 2f, 571f, curY + 2f, paint)
        curY += 16f

        val totalPcs = parts.sumOf { it.quantity }
        val totalArea = parts.sumOf { it.totalAreaSqFt }
        val totalEb = parts.sumOf { it.edgeBandingPerimeterMeters }
        val summaryNote = "Total Cut Pieces: $totalPcs pcs   |   Total Surface Area: ${String.format(Locale.US, "%.1f", totalArea)} sq.ft   |   Edge Banding Length: ${totalEb.toInt()}m"
        canvas.drawText(summaryNote, 32f, curY, boldBodyPaint.apply { color = Color.rgb(29, 78, 216); textSize = 10f })

        // 4. Workshop Instructions & Signatures
        val footY = 750f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(RectF(24f, footY, 350f, footY + 55f), 4f, 4f, paint)
        canvas.drawText("Saw Operator & Cutting Instructions:", 32f, footY + 15f, boldBodyPaint.apply { textSize = 8.5f })
        val instPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 7.5f; color = Color.rgb(71, 85, 105) }
        canvas.drawText("• Maintain 3mm saw blade kerf allowance on all primary rip and cross cuts.", 32f, footY + 28f, instPaint)
        canvas.drawText("• Check veneer/laminate grain direction along length before scoring.", 32f, footY + 40f, instPaint)
        canvas.drawText("• Label each panel with corresponding part number & edge tape code.", 32f, footY + 52f, instPaint)

        // Signatures
        canvas.drawText("Saw Operator:", 380f, footY + 20f, bodyPaint)
        canvas.drawLine(440f, footY + 20f, 555f, footY + 20f, paint)
        canvas.drawText("Quality Inspector:", 380f, footY + 46f, bodyPaint)
        canvas.drawLine(460f, footY + 46f, 555f, footY + 46f, paint)

        pdfDoc.finishPage(page)

        // Write to Cache file
        val outputDir = File(context.cacheDir, "quotations").apply { mkdirs() }
        val sanitizedName = project.projectName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val file = File(outputDir, "CuttingList_${sanitizedName}_${project.id}.pdf")
        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return file
    }

    private fun formatInr(amount: Double): String {
        return "₹ ${inrFormat.format(amount).replace("₹", "").trim()}"
    }
}
