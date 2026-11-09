package com.example.model

data class Estimation(
    // Quantities
    val ply18mmSheets: Int = 0,
    val ply9mmSheets: Int = 0,
    val ply6mmSheets: Int = 0,
    val laminateOutsideSheets: Int = 0,
    val laminateInsideSheets: Int = 0,
    val edgeBandingMeters: Double = 0.0,
    val hingesCount: Int = 0,
    val handlesCount: Int = 0,
    val drawerChannelsPairs: Int = 0,
    val fevicolKg: Double = 0.0,
    val totalAreaSqFt: Double = 0.0,

    // Material Costs
    val ply18mmCost: Double = 0.0,
    val ply9mmCost: Double = 0.0,
    val ply6mmCost: Double = 0.0,
    val laminateOutsideCost: Double = 0.0,
    val laminateInsideCost: Double = 0.0,
    val edgeBandingCost: Double = 0.0,
    val fevicolCost: Double = 0.0,

    // Hardware Costs
    val hingesCost: Double = 0.0,
    val handlesCost: Double = 0.0,
    val drawerChannelsCost: Double = 0.0,
    val screwsHardwareCost: Double = 0.0,

    // Services & Operations
    val labourCost: Double = 0.0,
    val transportationCost: Double = 1200.0,
    val installationCost: Double = 2500.0,
    val otherExpenses: Double = 500.0,

    // Financial calculations
    val profitMarginPercent: Double = 15.0,
    val gstPercent: Double = 18.0,
    val advancePaid: Double = 0.0
) {
    val totalBoardCost: Double
        get() = ply18mmCost + ply9mmCost + ply6mmCost

    val totalLaminateCost: Double
        get() = laminateOutsideCost + laminateInsideCost

    val totalHardwareCost: Double
        get() = hingesCost + handlesCost + drawerChannelsCost + screwsHardwareCost

    val totalMaterialCost: Double
        get() = totalBoardCost + totalLaminateCost + edgeBandingCost + fevicolCost + totalHardwareCost

    val totalServicesCost: Double
        get() = labourCost + transportationCost + installationCost + otherExpenses

    val costOfProduction: Double
        get() = totalMaterialCost + totalServicesCost

    val profitAmount: Double
        get() = costOfProduction * (profitMarginPercent / 100.0)

    val netBeforeGst: Double
        get() = costOfProduction + profitAmount

    val gstAmount: Double
        get() = netBeforeGst * (gstPercent / 100.0)

    val grandTotal: Double
        get() = netBeforeGst + gstAmount

    val balanceRemaining: Double
        get() = (grandTotal - advancePaid).coerceAtLeast(0.0)
}
