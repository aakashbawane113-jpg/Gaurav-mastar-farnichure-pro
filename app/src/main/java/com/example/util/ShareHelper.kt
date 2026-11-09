package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.Estimation
import com.example.model.FurnitureProject
import java.io.File
import java.text.NumberFormat
import java.util.Locale

object ShareHelper {

    private val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun shareQuotationViaWhatsApp(
        context: Context,
        pdfFile: File,
        project: FurnitureProject,
        estimation: Estimation
    ) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val totalStr = "₹ ${inrFormat.format(estimation.grandTotal).replace("₹", "").trim()}"
            val advStr = "₹ ${inrFormat.format(estimation.advancePaid).replace("₹", "").trim()}"
            val balStr = "₹ ${inrFormat.format(estimation.balanceRemaining).replace("₹", "").trim()}"

            val message = """
                *GAURAV FURNITURE PRO*
                *Project Quotation: ${project.projectName}*
                -----------------------------------
                👤 Client: ${project.customerName}
                📍 Site: ${project.customerAddress.ifEmpty { "On Site" }}
                
                💰 *Total Estimate:* $totalStr
                💵 Advance Paid: $advStr
                💳 Remaining Balance: $balStr
                
                📄 Please find the attached official 3D design specifications and detailed quotation PDF.
                
                _Thank you for choosing Gaurav Furniture Pro!_
            """.trimIndent()

            val cleanPhone = project.customerPhone.replace("[^0-9]".toRegex(), "")

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // If phone is available, try targeting WhatsApp package
                if (cleanPhone.isNotEmpty()) {
                    setPackage("com.whatsapp")
                }
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If specific whatsapp package fails or isn't installed, fall back to chooser
                val chooser = Intent.createChooser(intent.apply { `package` = null }, "Share Quotation PDF")
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing quotation: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareCuttingListViaWhatsApp(
        context: Context,
        pdfFile: File,
        project: FurnitureProject,
        partsCount: Int
    ) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val message = """
                *GAURAV FURNITURE PRO*
                *Workshop Cutting List & Sheet Nesting*
                -----------------------------------
                📋 Project: ${project.projectName}
                👤 Client: ${project.customerName}
                🔢 Total Panels: $partsCount items
                
                📄 Please find the attached official Cutting List & Material Specification PDF for workshop fabrication.
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Workshop Cutting List PDF")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing cutting list: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun viewOrPrintPdf(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Quotation PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app found or error opening PDF", Toast.LENGTH_SHORT).show()
        }
    }

    fun dialCustomerPhone(context: Context, phone: String) {
        val cleanPhone = phone.replace("[^0-9+]".toRegex(), "")
        if (cleanPhone.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsAppChat(context: Context, phone: String, clientName: String) {
        val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
        if (cleanPhone.isEmpty()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }
        val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        val message = "Hello $clientName, greeting from Gaurav Furniture Pro regarding your furniture project."
        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }
    }
}
