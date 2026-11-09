package com.example.util

import com.example.model.CuttingPart
import com.example.model.OptimizationResult
import com.example.model.OptimizedSheet
import com.example.model.PlacedCut
import com.example.model.StandardSheetSize
import com.example.model.WasteRect

object CuttingOptimizer {

    private data class FreeRect(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    fun optimize(
        parts: List<CuttingPart>,
        sheetSize: StandardSheetSize = StandardSheetSize.SIZE_8x4,
        bladeKerfMm: Int = 3
    ): OptimizationResult {
        // Group parts by thickness
        val groupedByThickness = parts.groupBy { it.thicknessMm }
        val allOptimizedSheets = mutableListOf<OptimizedSheet>()
        val unplaced = mutableListOf<CuttingPart>()

        var globalSheetIndex = 1

        for ((thickness, thicknessParts) in groupedByThickness) {
            // Expand each part by quantity
            val itemsToPlace = mutableListOf<SinglePart>()
            thicknessParts.forEachIndexed { pIndex, part ->
                repeat(part.quantity) {
                    itemsToPlace.add(
                        SinglePart(
                            partId = "${part.id}_$it",
                            partName = part.name,
                            lengthMm = part.lengthMm,
                            widthMm = part.widthMm,
                            thicknessMm = thickness,
                            colorIndex = pIndex % 6
                        )
                    )
                }
            }

            // Sort by larger dimension descending (heuristic for better packing)
            itemsToPlace.sortByDescending { maxOf(it.lengthMm, it.widthMm) * minOf(it.lengthMm, it.widthMm) }

            val sheetsForThickness = mutableListOf<OptimizedSheet>()
            var remainingItems = itemsToPlace.toList()

            while (remainingItems.isNotEmpty()) {
                val freeRects = mutableListOf(FreeRect(0, 0, sheetSize.lengthMm, sheetSize.widthMm))
                val placedInThisSheet = mutableListOf<PlacedCut>()
                val unplacedInThisSheet = mutableListOf<SinglePart>()

                for (item in remainingItems) {
                    var bestRectIndex = -1
                    var bestRotated = false
                    var minShortSideFit = Int.MAX_VALUE

                    for (i in freeRects.indices) {
                        val rect = freeRects[i]
                        // Try normal orientation
                        if (item.lengthMm <= rect.width && item.widthMm <= rect.height) {
                            val shortSideFit = minOf(rect.width - item.lengthMm, rect.height - item.widthMm)
                            if (shortSideFit < minShortSideFit) {
                                minShortSideFit = shortSideFit
                                bestRectIndex = i
                                bestRotated = false
                            }
                        }
                        // Try rotated orientation
                        if (item.widthMm <= rect.width && item.lengthMm <= rect.height) {
                            val shortSideFit = minOf(rect.width - item.widthMm, rect.height - item.lengthMm)
                            if (shortSideFit < minShortSideFit) {
                                minShortSideFit = shortSideFit
                                bestRectIndex = i
                                bestRotated = true
                            }
                        }
                    }

                    if (bestRectIndex != -1) {
                        val targetRect = freeRects.removeAt(bestRectIndex)
                        val placedW = if (bestRotated) item.widthMm else item.lengthMm
                        val placedH = if (bestRotated) item.lengthMm else item.widthMm

                        placedInThisSheet.add(
                            PlacedCut(
                                partId = item.partId,
                                partName = item.partName,
                                x = targetRect.x,
                                y = targetRect.y,
                                width = placedW,
                                height = placedH,
                                rotated = bestRotated,
                                thicknessMm = thickness,
                                colorIndex = item.colorIndex
                            )
                        )

                        // Guillotine split: split the remaining targetRect into two sub-rectangles with kerf
                        val rightW = targetRect.width - placedW - bladeKerfMm
                        val rightH = targetRect.height
                        val bottomW = targetRect.width
                        val bottomH = targetRect.height - placedH - bladeKerfMm

                        // Choose cut direction to maximize larger area
                        if (rightW > 50 && targetRect.height - placedH - bladeKerfMm > 0) {
                            // Split horizontally
                            if (rightW > 0) {
                                freeRects.add(
                                    FreeRect(
                                        x = targetRect.x + placedW + bladeKerfMm,
                                        y = targetRect.y,
                                        width = rightW,
                                        height = placedH
                                    )
                                )
                            }
                            if (bottomH > 0) {
                                freeRects.add(
                                    FreeRect(
                                        x = targetRect.x,
                                        y = targetRect.y + placedH + bladeKerfMm,
                                        width = bottomW,
                                        height = bottomH
                                    )
                                )
                            }
                        } else {
                            // Split vertically
                            if (rightW > 0) {
                                freeRects.add(
                                    FreeRect(
                                        x = targetRect.x + placedW + bladeKerfMm,
                                        y = targetRect.y,
                                        width = rightW,
                                        height = rightH
                                    )
                                )
                            }
                            if (bottomH > 0) {
                                freeRects.add(
                                    FreeRect(
                                        x = targetRect.x,
                                        y = targetRect.y + placedH + bladeKerfMm,
                                        width = placedW,
                                        height = bottomH
                                    )
                                )
                            }
                        }
                    } else {
                        // Could not place on this sheet
                        unplacedInThisSheet.add(item)
                    }
                }

                if (placedInThisSheet.isEmpty()) {
                    // Item exceeds sheet dimensions entirely!
                    unplaced.addAll(unplacedInThisSheet.map {
                        CuttingPart(
                            name = it.partName,
                            lengthMm = it.lengthMm,
                            widthMm = it.widthMm,
                            thicknessMm = it.thicknessMm,
                            quantity = 1,
                            materialType = "${it.thicknessMm}mm Sheet",
                            edgeBanding = "None"
                        )
                    })
                    break
                }

                val usedArea = placedInThisSheet.sumOf { it.width.toLong() * it.height.toLong() }
                val wasteRects = freeRects.filter { it.width > 20 && it.height > 20 }.map {
                    WasteRect(it.x, it.y, it.width, it.height)
                }

                val optimizedSheet = OptimizedSheet(
                    sheetIndex = globalSheetIndex++,
                    thicknessMm = thickness,
                    sheetSize = sheetSize,
                    placedCuts = placedInThisSheet,
                    wasteRects = wasteRects,
                    usedAreaMm2 = usedArea,
                    totalAreaMm2 = sheetSize.areaSqMm
                )

                sheetsForThickness.add(optimizedSheet)
                remainingItems = unplacedInThisSheet
            }

            allOptimizedSheets.addAll(sheetsForThickness)
        }

        val totalUsedAreaMm2 = allOptimizedSheets.sumOf { it.usedAreaMm2 }
        val totalSheetAreaMm2 = allOptimizedSheets.sumOf { it.totalAreaMm2 }
        val totalUsedSqFt = (totalUsedAreaMm2.toDouble() / 1_000_000.0) * 10.7639
        val totalSheetSqFt = (totalSheetAreaMm2.toDouble() / 1_000_000.0) * 10.7639
        val avgEfficiency = if (totalSheetAreaMm2 > 0) (totalUsedAreaMm2.toDouble() / totalSheetAreaMm2.toDouble()) * 100.0 else 0.0
        val avgWaste = (100.0 - avgEfficiency).coerceAtLeast(0.0)

        return OptimizationResult(
            sheets = allOptimizedSheets,
            totalSheets = allOptimizedSheets.size,
            bladeKerfMm = bladeKerfMm,
            sheetSize = sheetSize,
            totalUsedAreaSqFt = totalUsedSqFt,
            totalSheetAreaSqFt = totalSheetSqFt,
            averageWastagePercent = avgWaste,
            unplacedParts = unplaced
        )
    }

    private data class SinglePart(
        val partId: String,
        val partName: String,
        val lengthMm: Int,
        val widthMm: Int,
        val thicknessMm: Int,
        val colorIndex: Int
    )
}
