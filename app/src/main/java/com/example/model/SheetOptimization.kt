package com.example.model

enum class StandardSheetSize(val label: String, val lengthMm: Int, val widthMm: Int) {
    SIZE_8x4("8 x 4 feet (2440 x 1220 mm)", 2440, 1220),
    SIZE_7x4("7 x 4 feet (2135 x 1220 mm)", 2135, 1220),
    SIZE_6x4("6 x 4 feet (1830 x 1220 mm)", 1830, 1220);

    val areaSqMm: Long = lengthMm.toLong() * widthMm.toLong()
    val areaSqFt: Double = (lengthMm / 304.8) * (widthMm / 304.8)
}

data class PlacedCut(
    val partId: String,
    val partName: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val rotated: Boolean,
    val thicknessMm: Int,
    val colorIndex: Int = 0
)

data class WasteRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class OptimizedSheet(
    val sheetIndex: Int,
    val thicknessMm: Int,
    val sheetSize: StandardSheetSize,
    val placedCuts: List<PlacedCut>,
    val wasteRects: List<WasteRect>,
    val usedAreaMm2: Long,
    val totalAreaMm2: Long
) {
    val efficiencyPercent: Double
        get() = if (totalAreaMm2 > 0) (usedAreaMm2.toDouble() / totalAreaMm2.toDouble()) * 100.0 else 0.0

    val wastePercent: Double
        get() = (100.0 - efficiencyPercent).coerceAtLeast(0.0)

    val usedAreaSqFt: Double
        get() = (usedAreaMm2.toDouble() / 1_000_000.0) * 10.7639

    val totalAreaSqFt: Double
        get() = sheetSize.areaSqFt
}

data class OptimizationResult(
    val sheets: List<OptimizedSheet>,
    val totalSheets: Int,
    val bladeKerfMm: Int,
    val sheetSize: StandardSheetSize,
    val totalUsedAreaSqFt: Double,
    val totalSheetAreaSqFt: Double,
    val averageWastagePercent: Double,
    val unplacedParts: List<CuttingPart> = emptyList()
)
