package com.example.data

import com.example.model.Customer
import com.example.model.FurnitureCategory
import com.example.model.FurnitureConfig
import com.example.model.FurnitureProject
import com.example.model.MaterialRates
import com.example.model.ProjectStatus
import com.example.model.WoodFinish
import com.example.util.CuttingListGenerator
import com.example.util.EstimationCalculator

object SampleData {

    fun getSampleCustomers(): List<Customer> {
        return listOf(
            Customer(
                id = 1,
                name = "Rajesh Sharma",
                phone = "9822014589",
                address = "Flat 402, Royal Palms, Baner, Pune",
                email = "rajesh.sharma@example.com",
                notes = "Master bedroom wardrobe with loft requirement"
            ),
            Customer(
                id = 2,
                name = "Priya Patel",
                phone = "9930412890",
                address = "B-12, Greenfield Heights, Andheri West, Mumbai",
                email = "priya.patel@example.com",
                notes = "Modular kitchen with acrylic finish & tandem drawers"
            ),
            Customer(
                id = 3,
                name = "Amit Verma",
                phone = "9741235678",
                address = "Villa 8, Silver Oak Residency, Whitefield, Bangalore",
                email = "amit.v@example.com",
                notes = "Living room floating TV console with back paneling"
            )
        )
    }

    fun getSampleProjects(converters: Converters): List<FurnitureProject> {
        val defaultRates = MaterialRates()

        // 1. Sample Wardrobe
        val wardrobeConfig = FurnitureConfig(
            category = FurnitureCategory.WARDROBE,
            heightMm = 2100,
            widthMm = 1800,
            depthMm = 600,
            carcassThicknessMm = 18,
            backThicknessMm = 9,
            shutterThicknessMm = 18,
            numberOfDoors = 3,
            shelvesCount = 5,
            drawersCount = 3,
            finish = WoodFinish.TEAK_WOOD,
            hasLoft = true,
            loftHeightMm = 450,
            baseSkirtingHeightMm = 75
        )
        val wardrobeParts = CuttingListGenerator.generateCuttingList(wardrobeConfig)
        val wardrobeEstimation = EstimationCalculator.calculateEstimation(
            config = wardrobeConfig,
            parts = wardrobeParts,
            rates = defaultRates,
            profitMarginPercent = 15.0,
            gstPercent = 18.0,
            advancePaid = 35000.0
        )

        // 2. Sample Modular Kitchen
        val kitchenConfig = FurnitureConfig(
            category = FurnitureCategory.MODULAR_KITCHEN,
            heightMm = 850,
            widthMm = 2400,
            depthMm = 600,
            carcassThicknessMm = 18,
            backThicknessMm = 9,
            shutterThicknessMm = 18,
            numberOfDoors = 4,
            shelvesCount = 4,
            drawersCount = 4,
            finish = WoodFinish.WHITE_GLOSS,
            hasLoft = false
        )
        val kitchenParts = CuttingListGenerator.generateCuttingList(kitchenConfig)
        val kitchenEstimation = EstimationCalculator.calculateEstimation(
            config = kitchenConfig,
            parts = kitchenParts,
            rates = defaultRates,
            profitMarginPercent = 18.0,
            gstPercent = 18.0,
            advancePaid = 50000.0
        )

        // 3. Sample TV Unit
        val tvConfig = FurnitureConfig(
            category = FurnitureCategory.TV_UNIT,
            heightMm = 1800,
            widthMm = 1800,
            depthMm = 400,
            carcassThicknessMm = 18,
            backThicknessMm = 9,
            shutterThicknessMm = 18,
            numberOfDoors = 2,
            shelvesCount = 3,
            drawersCount = 2,
            finish = WoodFinish.WALNUT_DARK,
            hasLoft = false
        )
        val tvParts = CuttingListGenerator.generateCuttingList(tvConfig)
        val tvEstimation = EstimationCalculator.calculateEstimation(
            config = tvConfig,
            parts = tvParts,
            rates = defaultRates,
            profitMarginPercent = 15.0,
            gstPercent = 18.0,
            advancePaid = 15000.0
        )

        return listOf(
            FurnitureProject(
                id = 1,
                customerId = 1,
                customerName = "Rajesh Sharma",
                customerPhone = "9822014589",
                customerAddress = "Flat 402, Royal Palms, Baner, Pune",
                projectName = "Master Bedroom 3-Door Wardrobe with Loft",
                status = ProjectStatus.IN_PROGRESS,
                createdAt = System.currentTimeMillis() - 86400000L * 3,
                updatedAt = System.currentTimeMillis() - 86400000L * 1,
                notes = "Client requested soft-close hinges and golden profile handles.",
                configJson = converters.fromConfig(wardrobeConfig),
                cuttingPartsJson = converters.fromCuttingParts(wardrobeParts),
                ratesJson = converters.fromRates(defaultRates),
                estimationJson = converters.fromEstimation(wardrobeEstimation)
            ),
            FurnitureProject(
                id = 2,
                customerId = 2,
                customerName = "Priya Patel",
                customerPhone = "9930412890",
                customerAddress = "B-12, Greenfield Heights, Andheri West, Mumbai",
                projectName = "Modular Kitchen Acrylic Base Cabinets",
                status = ProjectStatus.ESTIMATED,
                createdAt = System.currentTimeMillis() - 86400000L * 7,
                updatedAt = System.currentTimeMillis() - 86400000L * 2,
                notes = "Marine ply BWP 710 grade carcass with high gloss white acrylic shutter.",
                configJson = converters.fromConfig(kitchenConfig),
                cuttingPartsJson = converters.fromCuttingParts(kitchenParts),
                ratesJson = converters.fromRates(defaultRates),
                estimationJson = converters.fromEstimation(kitchenEstimation)
            ),
            FurnitureProject(
                id = 3,
                customerId = 3,
                customerName = "Amit Verma",
                customerPhone = "9741235678",
                customerAddress = "Villa 8, Silver Oak Residency, Whitefield, Bangalore",
                projectName = "Minimalist Floating TV Media Unit",
                status = ProjectStatus.COMPLETED,
                createdAt = System.currentTimeMillis() - 86400000L * 14,
                updatedAt = System.currentTimeMillis() - 86400000L * 5,
                notes = "Installation done with hidden wall anchor bolts and cable raceways.",
                configJson = converters.fromConfig(tvConfig),
                cuttingPartsJson = converters.fromCuttingParts(tvParts),
                ratesJson = converters.fromRates(defaultRates),
                estimationJson = converters.fromEstimation(tvEstimation)
            )
        )
    }
}
