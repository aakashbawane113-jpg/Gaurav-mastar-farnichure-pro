package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProjectStatus(val label: String, val hindiLabel: String, val colorHex: Long) {
    DRAFT("Draft", "ड्राफ्ट", 0xFF64748B),
    ESTIMATED("Quotation Sent", "कोटेशन भेजा", 0xFF0284C7),
    IN_PROGRESS("In Production", "काम चालू है", 0xFFD97706),
    COMPLETED("Completed", "पूरा हुआ", 0xFF16A34A)
}

@Entity(tableName = "projects")
data class FurnitureProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val customerAddress: String = "",
    val projectName: String,
    val status: ProjectStatus = ProjectStatus.ESTIMATED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    // Serialized fields for Room
    val configJson: String = "",
    val cuttingPartsJson: String = "",
    val ratesJson: String = "",
    val estimationJson: String = ""
)
