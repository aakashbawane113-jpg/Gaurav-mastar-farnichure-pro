package com.example.model

data class CuttingPart(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val lengthMm: Int,
    val widthMm: Int,
    val thicknessMm: Int,
    val quantity: Int,
    val materialType: String, // e.g. "Commercial Ply 18mm", "BWP Marine Ply 18mm", "Back Panel 9mm", "MDF 18mm"
    val edgeBanding: String, // e.g. "0.8mm PVC (2L + 1W)", "2mm PVC (2L + 2W)", "None"
    val isCustom: Boolean = false,
    val notes: String = ""
) {
    val totalAreaSqM: Double
        get() = (lengthMm.toDouble() * widthMm.toDouble() * quantity.toDouble()) / 1_000_000.0

    val totalAreaSqFt: Double
        get() = totalAreaSqM * 10.7639

    // Edge banding perimeter in meters (e.g. 2L + 2W or 2L + 1W)
    val edgeBandingPerimeterMeters: Double
        get() {
            val lengthM = lengthMm / 1000.0
            val widthM = widthMm / 1000.0
            return when {
                edgeBanding.contains("2L + 2W") -> (2 * lengthM + 2 * widthM) * quantity
                edgeBanding.contains("2L + 1W") -> (2 * lengthM + 1 * widthM) * quantity
                edgeBanding.contains("1L + 2W") -> (1 * lengthM + 2 * widthM) * quantity
                edgeBanding.contains("1L + 1W") -> (1 * lengthM + 1 * widthM) * quantity
                edgeBanding.contains("None") -> 0.0
                else -> (2 * lengthM + 1 * widthM) * quantity
            }
        }
}
