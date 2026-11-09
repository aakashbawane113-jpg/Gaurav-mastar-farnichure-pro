package com.example.util

import com.example.model.CuttingPart
import com.example.model.Estimation
import com.example.model.FurnitureConfig
import com.example.model.MaterialRates
import kotlin.math.ceil

object EstimationCalculator {

    fun calculateEstimation(
        config: FurnitureConfig,
        parts: List<CuttingPart>,
        rates: MaterialRates,
        profitMarginPercent: Double = 15.0,
        gstPercent: Double = 18.0,
        advancePaid: Double = 0.0,
        transportCost: Double = 1200.0,
        installCost: Double = 2500.0,
        otherExpenses: Double = 500.0
    ): Estimation {
        // Area per sheet in sq.ft (8x4 ft = 32 sq.ft)
        val sheetAreaSqFt = 32.0
        val wasteMultiplier = 1.15 // 15% safety/wastage margin

        // Group parts by thickness to estimate raw board sheets
        val parts18mm = parts.filter { it.thicknessMm >= 16 && it.thicknessMm <= 19 }
        val parts9mm = parts.filter { it.thicknessMm in 8..12 }
        val parts6mm = parts.filter { it.thicknessMm < 8 || it.thicknessMm == 25 }

        val area18mmSqFt = parts18mm.sumOf { it.totalAreaSqFt }
        val area9mmSqFt = parts9mm.sumOf { it.totalAreaSqFt }
        val area6mmSqFt = parts6mm.sumOf { it.totalAreaSqFt }

        val ply18mmSheets = ceil((area18mmSqFt * wasteMultiplier) / sheetAreaSqFt).toInt().coerceAtLeast(if (parts18mm.isNotEmpty()) 1 else 0)
        val ply9mmSheets = ceil((area9mmSqFt * wasteMultiplier) / sheetAreaSqFt).toInt().coerceAtLeast(if (parts9mm.isNotEmpty()) 1 else 0)
        val ply6mmSheets = ceil((area6mmSqFt * wasteMultiplier) / sheetAreaSqFt).toInt().coerceAtLeast(if (parts6mm.isNotEmpty()) 1 else 0)

        // Laminate:
        // Outside laminate needed for visible shutters and exposed sides
        val exposedAreaSqFt = parts.filter {
            it.name.contains("Shutter", ignoreCase = true) ||
            it.name.contains("Door", ignoreCase = true) ||
            it.name.contains("Fascia", ignoreCase = true) ||
            it.name.contains("Worktop", ignoreCase = true) ||
            it.name.contains("Side", ignoreCase = true)
        }.sumOf { it.totalAreaSqFt }
        val laminateOutsideSheets = ceil((exposedAreaSqFt * 1.10) / sheetAreaSqFt).toInt().coerceAtLeast(1)

        // Inside liner laminate for carcass interior
        val totalCarcassAreaSqFt = parts.sumOf { it.totalAreaSqFt }
        val laminateInsideSheets = ceil((totalCarcassAreaSqFt * 0.9) / sheetAreaSqFt).toInt().coerceAtLeast(1)

        // Edge banding running meters
        val totalEdgeBandingMeters = ceil(parts.sumOf { it.edgeBandingPerimeterMeters } * 1.1)

        // Hardware quantities
        val hingesPerDoor = if (config.heightMm >= 1600) 3 else 2
        val hingesCount = config.numberOfDoors * hingesPerDoor
        val handlesCount = config.numberOfDoors + config.drawersCount
        val drawerChannelsPairs = config.drawersCount
        val fevicolKg = ceil((laminateOutsideSheets + laminateInsideSheets) * 1.8)

        // Calculations with rates
        val ply18Cost = ply18mmSheets * rates.ply18mmPerSheet
        val ply9Cost = ply9mmSheets * rates.ply9mmPerSheet
        val ply6Cost = ply6mmSheets * rates.ply6mmPerSheet
        val lamOutCost = laminateOutsideSheets * rates.laminateOutsidePerSheet
        val lamInCost = laminateInsideSheets * rates.laminateInsidePerSheet
        val ebCost = totalEdgeBandingMeters * rates.edgeBandingPerMeter
        val fevCost = fevicolKg * rates.fevicolPerKg

        val hingesCost = hingesCount * rates.hingePerPiece
        val handlesCost = handlesCount * rates.handlePerPiece
        val drawerChCost = drawerChannelsPairs * rates.drawerChannelPerPair
        val screwsHardwareCost = rates.screwsFastenersLump

        // Labour cost based on carcass area (width * height in sq.ft)
        val furnitureFacingSqFt = (config.widthMm / 304.8) * (config.heightMm / 304.8)
        val labourCost = ceil(furnitureFacingSqFt * rates.labourPerSqFt).coerceAtLeast(2500.0)

        return Estimation(
            ply18mmSheets = ply18mmSheets,
            ply9mmSheets = ply9mmSheets,
            ply6mmSheets = ply6mmSheets,
            laminateOutsideSheets = laminateOutsideSheets,
            laminateInsideSheets = laminateInsideSheets,
            edgeBandingMeters = totalEdgeBandingMeters,
            hingesCount = hingesCount,
            handlesCount = handlesCount,
            drawerChannelsPairs = drawerChannelsPairs,
            fevicolKg = fevicolKg,
            totalAreaSqFt = totalCarcassAreaSqFt,

            ply18mmCost = ply18Cost,
            ply9mmCost = ply9Cost,
            ply6mmCost = ply6Cost,
            laminateOutsideCost = lamOutCost,
            laminateInsideCost = lamInCost,
            edgeBandingCost = ebCost,
            fevicolCost = fevCost,

            hingesCost = hingesCost,
            handlesCost = handlesCost,
            drawerChannelsCost = drawerChCost,
            screwsHardwareCost = screwsHardwareCost,

            labourCost = labourCost,
            transportationCost = transportCost,
            installationCost = installCost,
            otherExpenses = otherExpenses,

            profitMarginPercent = profitMarginPercent,
            gstPercent = gstPercent,
            advancePaid = advancePaid
        )
    }
}
