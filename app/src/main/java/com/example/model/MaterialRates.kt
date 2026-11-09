package com.example.model

data class MaterialRates(
    val ply18mmPerSheet: Double = 2400.0,
    val ply9mmPerSheet: Double = 1350.0,
    val ply6mmPerSheet: Double = 950.0,
    val laminateOutsidePerSheet: Double = 1800.0,
    val laminateInsidePerSheet: Double = 850.0,
    val edgeBandingPerMeter: Double = 16.0,
    val hingePerPiece: Double = 180.0,
    val handlePerPiece: Double = 220.0,
    val drawerChannelPerPair: Double = 650.0,
    val fevicolPerKg: Double = 260.0,
    val screwsFastenersLump: Double = 750.0,
    val labourPerSqFt: Double = 180.0
)
