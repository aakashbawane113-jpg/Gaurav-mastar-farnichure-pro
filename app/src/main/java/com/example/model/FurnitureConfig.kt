package com.example.model

data class FurnitureConfig(
    val category: FurnitureCategory = FurnitureCategory.WARDROBE,
    val heightMm: Int = 2100,
    val widthMm: Int = 1800,
    val depthMm: Int = 600,
    val carcassThicknessMm: Int = 18,
    val backThicknessMm: Int = 9,
    val shutterThicknessMm: Int = 18,
    val numberOfDoors: Int = 3,
    val shelvesCount: Int = 5,
    val drawersCount: Int = 3,
    val finish: WoodFinish = WoodFinish.TEAK_WOOD,
    val hasLoft: Boolean = false,
    val loftHeightMm: Int = 450,
    val baseSkirtingHeightMm: Int = 75
)
