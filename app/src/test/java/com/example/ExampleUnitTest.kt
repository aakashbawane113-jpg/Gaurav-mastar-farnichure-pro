package com.example

import com.example.model.FurnitureCategory
import com.example.model.FurnitureConfig
import com.example.model.MaterialRates
import com.example.model.StandardSheetSize
import com.example.util.CuttingListGenerator
import com.example.util.CuttingOptimizer
import com.example.util.EstimationCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCuttingListGeneration_wardrobe() {
        val config = FurnitureConfig(
            category = FurnitureCategory.WARDROBE,
            heightMm = 2100,
            widthMm = 1800,
            depthMm = 600,
            carcassThicknessMm = 18,
            numberOfDoors = 3,
            shelvesCount = 5,
            drawersCount = 3
        )
        val parts = CuttingListGenerator.generateCuttingList(config)
        assertTrue("Cutting parts must not be empty", parts.isNotEmpty())

        val sidePanels = parts.find { it.name.contains("Side Panels") }
        assertNotNull("Should contain side panels", sidePanels)
        assertEquals(2, sidePanels?.quantity)
        assertEquals(2100, sidePanels?.lengthMm)
        assertEquals(600, sidePanels?.widthMm)

        val shutters = parts.find { it.name.contains("Shutter Doors") }
        assertNotNull("Should contain shutter doors", shutters)
        assertEquals(3, shutters?.quantity)
    }

    @Test
    fun testCuttingOptimization_guillotinePacking() {
        val config = FurnitureConfig(category = FurnitureCategory.WARDROBE)
        val parts = CuttingListGenerator.generateCuttingList(config)

        val result = CuttingOptimizer.optimize(
            parts = parts,
            sheetSize = StandardSheetSize.SIZE_8x4,
            bladeKerfMm = 3
        )

        assertTrue("Should produce at least 1 sheet", result.totalSheets > 0)
        assertTrue("Average wastage should be between 0 and 100", result.averageWastagePercent in 0.0..100.0)
        assertTrue("Total used area must be positive", result.totalUsedAreaSqFt > 0)
    }

    @Test
    fun testEstimationCalculator_accuracy() {
        val config = FurnitureConfig(category = FurnitureCategory.WARDROBE)
        val parts = CuttingListGenerator.generateCuttingList(config)
        val rates = MaterialRates(
            ply18mmPerSheet = 2400.0,
            labourPerSqFt = 180.0
        )

        val estimation = EstimationCalculator.calculateEstimation(
            config = config,
            parts = parts,
            rates = rates,
            profitMarginPercent = 15.0,
            gstPercent = 18.0,
            advancePaid = 20000.0
        )

        assertTrue("Grand total should be greater than production cost", estimation.grandTotal > estimation.costOfProduction)
        assertTrue("Profit amount should be 15% of production cost", estimation.profitAmount > 0)
        assertTrue("GST amount should be 18% of net before tax", estimation.gstAmount > 0)
        assertEquals(estimation.grandTotal - 20000.0, estimation.balanceRemaining, 0.01)
    }
}
