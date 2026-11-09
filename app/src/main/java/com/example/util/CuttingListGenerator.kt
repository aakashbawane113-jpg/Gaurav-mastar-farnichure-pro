package com.example.util

import com.example.model.CuttingPart
import com.example.model.FurnitureCategory
import com.example.model.FurnitureConfig

object CuttingListGenerator {

    fun generateCuttingList(config: FurnitureConfig): List<CuttingPart> {
        val parts = mutableListOf<CuttingPart>()
        val h = config.heightMm
        val w = config.widthMm
        val d = config.depthMm
        val t = config.carcassThicknessMm
        val backT = config.backThicknessMm
        val shutterT = config.shutterThicknessMm

        when (config.category) {
            FurnitureCategory.WARDROBE -> {
                val carcassH = if (config.hasLoft) (h - config.loftHeightMm) else h

                // Carcass Sides
                parts.add(
                    CuttingPart(
                        name = "Wardrobe Side Panels (Left & Right)",
                        lengthMm = carcassH,
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L + 2W)"
                    )
                )

                // Top & Bottom Panels
                val topBottomWidth = (w - (2 * t)).coerceAtLeast(300)
                parts.add(
                    CuttingPart(
                        name = "Wardrobe Top & Bottom Panels",
                        lengthMm = topBottomWidth,
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )

                // Back Panel
                parts.add(
                    CuttingPart(
                        name = "Wardrobe Back Panel",
                        lengthMm = (carcassH - (t / 2)).coerceAtLeast(300),
                        widthMm = (w - t).coerceAtLeast(300),
                        thicknessMm = backT,
                        quantity = 1,
                        materialType = "${backT}mm Back Ply",
                        edgeBanding = "None"
                    )
                )

                // Vertical Divider (Partition) if doors >= 2
                val partitionsCount = if (config.numberOfDoors >= 3) 2 else if (config.numberOfDoors >= 2) 1 else 0
                if (partitionsCount > 0) {
                    val dividerH = (carcassH - (2 * t) - config.baseSkirtingHeightMm).coerceAtLeast(300)
                    parts.add(
                        CuttingPart(
                            name = "Internal Vertical Partition",
                            lengthMm = dividerH,
                            widthMm = (d - 25).coerceAtLeast(200),
                            thicknessMm = t,
                            quantity = partitionsCount,
                            materialType = "18mm Plywood",
                            edgeBanding = "0.8mm PVC (1L)"
                        )
                    )
                }

                // Shelves
                if (config.shelvesCount > 0) {
                    val compartments = (partitionsCount + 1).coerceAtLeast(1)
                    val shelfW = ((w - (2 * t) - (partitionsCount * t)) / compartments).coerceAtLeast(200)
                    parts.add(
                        CuttingPart(
                            name = "Adjustable & Fixed Shelves",
                            lengthMm = shelfW,
                            widthMm = (d - 40).coerceAtLeast(150),
                            thicknessMm = t,
                            quantity = config.shelvesCount,
                            materialType = "18mm Plywood",
                            edgeBanding = "0.8mm PVC (1L + 2W)"
                        )
                    )
                }

                // Shutter Doors
                if (config.numberOfDoors > 0) {
                    val shutterH = (carcassH - config.baseSkirtingHeightMm - 10).coerceAtLeast(300)
                    val shutterW = ((w - ((config.numberOfDoors - 1) * 3) - 6) / config.numberOfDoors).coerceAtLeast(200)
                    parts.add(
                        CuttingPart(
                            name = "Wardrobe Shutter Doors",
                            lengthMm = shutterH,
                            widthMm = shutterW,
                            thicknessMm = shutterT,
                            quantity = config.numberOfDoors,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }

                // Drawers
                if (config.drawersCount > 0) {
                    val drawerFrontH = 180
                    val drawerFrontW = ((w - (2 * t)) / (if (partitionsCount > 0) (partitionsCount + 1) else 1) - 10).coerceAtLeast(250)
                    val drawerDepth = (d - 80).coerceAtLeast(300)

                    parts.add(
                        CuttingPart(
                            name = "Drawer External Fascia / Fronts",
                            lengthMm = drawerFrontW,
                            widthMm = drawerFrontH,
                            thicknessMm = shutterT,
                            quantity = config.drawersCount,
                            materialType = "18mm Plywood",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                    parts.add(
                        CuttingPart(
                            name = "Drawer Box Sides",
                            lengthMm = drawerDepth,
                            widthMm = 140,
                            thicknessMm = 12,
                            quantity = config.drawersCount * 2,
                            materialType = "12mm Plywood",
                            edgeBanding = "0.8mm PVC (1L)"
                        )
                    )
                    parts.add(
                        CuttingPart(
                            name = "Drawer Box Back & Sub-Front",
                            lengthMm = (drawerFrontW - 50).coerceAtLeast(200),
                            widthMm = 140,
                            thicknessMm = 12,
                            quantity = config.drawersCount * 2,
                            materialType = "12mm Plywood",
                            edgeBanding = "0.8mm PVC (1L)"
                        )
                    )
                    parts.add(
                        CuttingPart(
                            name = "Drawer Bottoms",
                            lengthMm = (drawerFrontW - 30).coerceAtLeast(200),
                            widthMm = drawerDepth,
                            thicknessMm = 9,
                            quantity = config.drawersCount,
                            materialType = "9mm Plywood",
                            edgeBanding = "None"
                        )
                    )
                }

                // Skirting
                parts.add(
                    CuttingPart(
                        name = "Base Skirting Plinth",
                        lengthMm = w,
                        widthMm = config.baseSkirtingHeightMm,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "0.8mm PVC (1L)"
                    )
                )

                // Loft Unit (if present)
                if (config.hasLoft && config.loftHeightMm > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Loft Cabinet Sides",
                            lengthMm = config.loftHeightMm,
                            widthMm = d,
                            thicknessMm = t,
                            quantity = 2,
                            materialType = "18mm Plywood",
                            edgeBanding = "2mm PVC (1L + 2W)"
                        )
                    )
                    parts.add(
                        CuttingPart(
                            name = "Loft Top & Bottom Panels",
                            lengthMm = topBottomWidth,
                            widthMm = d,
                            thicknessMm = t,
                            quantity = 2,
                            materialType = "18mm Plywood",
                            edgeBanding = "2mm PVC (1L)"
                        )
                    )
                    parts.add(
                        CuttingPart(
                            name = "Loft Shutter Doors",
                            lengthMm = (config.loftHeightMm - 10).coerceAtLeast(250),
                            widthMm = ((w - ((config.numberOfDoors - 1) * 3)) / config.numberOfDoors.coerceAtLeast(1)).coerceAtLeast(200),
                            thicknessMm = shutterT,
                            quantity = config.numberOfDoors.coerceAtLeast(2),
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
            }

            FurnitureCategory.MODULAR_KITCHEN -> {
                // Base Cabinet
                parts.add(
                    CuttingPart(
                        name = "Kitchen Base Sides (Carcass)",
                        lengthMm = (h - config.baseSkirtingHeightMm).coerceAtLeast(300),
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm BWP Marine Ply",
                        edgeBanding = "2mm PVC (1L + 2W)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Kitchen Base Bottom Panel",
                        lengthMm = (w - (2 * t)).coerceAtLeast(300),
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 1,
                        materialType = "18mm BWP Marine Ply",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Top Fixing Rails / Stretchers",
                        lengthMm = (w - (2 * t)).coerceAtLeast(300),
                        widthMm = 100,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm BWP Marine Ply",
                        edgeBanding = "0.8mm PVC (1L)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Kitchen Back Panel",
                        lengthMm = (h - config.baseSkirtingHeightMm).coerceAtLeast(300),
                        widthMm = (w - t).coerceAtLeast(300),
                        thicknessMm = backT,
                        quantity = 1,
                        materialType = "${backT}mm Back Ply",
                        edgeBanding = "None"
                    )
                )

                val dividers = (w / 600).coerceIn(1, 4)
                parts.add(
                    CuttingPart(
                        name = "Vertical Cabinet Dividers",
                        lengthMm = (h - config.baseSkirtingHeightMm - t).coerceAtLeast(300),
                        widthMm = (d - 20).coerceAtLeast(200),
                        thicknessMm = t,
                        quantity = dividers,
                        materialType = "18mm BWP Marine Ply",
                        edgeBanding = "0.8mm PVC (1L)"
                    )
                )

                if (config.numberOfDoors > 0) {
                    val doorW = ((w - (config.numberOfDoors * 3)) / config.numberOfDoors).coerceAtLeast(200)
                    parts.add(
                        CuttingPart(
                            name = "Kitchen Shutter Doors (Acrylic/Laminate)",
                            lengthMm = (h - config.baseSkirtingHeightMm - 10).coerceAtLeast(200),
                            widthMm = doorW,
                            thicknessMm = shutterT,
                            quantity = config.numberOfDoors,
                            materialType = "18mm Marine Ply Shutter",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }

                if (config.drawersCount > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Tandem / Basket Drawer Fascia",
                            lengthMm = 590,
                            widthMm = ((h - config.baseSkirtingHeightMm) / config.drawersCount).coerceAtLeast(150),
                            thicknessMm = shutterT,
                            quantity = config.drawersCount,
                            materialType = "18mm Marine Ply Shutter",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
            }

            FurnitureCategory.TV_UNIT -> {
                // TV Backing Wall Panel
                parts.add(
                    CuttingPart(
                        name = "TV Wall Feature Paneling Base",
                        lengthMm = h,
                        widthMm = w,
                        thicknessMm = 12,
                        quantity = 1,
                        materialType = "12mm HDHMR / Ply",
                        edgeBanding = "0.8mm PVC (2L + 2W)"
                    )
                )
                // Floating Bottom Console
                val consoleH = 350
                val consoleW = (w * 0.9).toInt().coerceAtLeast(600)
                parts.add(
                    CuttingPart(
                        name = "Floating Console Top & Bottom",
                        lengthMm = consoleW,
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L + 2W)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Floating Console Sides & Dividers",
                        lengthMm = (consoleH - (2 * t)).coerceAtLeast(150),
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 4,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                if (config.numberOfDoors > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Drop-down Flap Shutters",
                            lengthMm = (consoleW / (config.numberOfDoors + config.drawersCount).coerceAtLeast(1) - 6).coerceAtLeast(200),
                            widthMm = (consoleH - 10).coerceAtLeast(150),
                            thicknessMm = shutterT,
                            quantity = config.numberOfDoors,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
                if (config.drawersCount > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Console Drawer Fronts",
                            lengthMm = (consoleW / (config.numberOfDoors + config.drawersCount).coerceAtLeast(1) - 6).coerceAtLeast(200),
                            widthMm = (consoleH - 10).coerceAtLeast(150),
                            thicknessMm = shutterT,
                            quantity = config.drawersCount,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
                // Wall Ledge Shelf
                parts.add(
                    CuttingPart(
                        name = "Overhead Floating Wall Shelf",
                        lengthMm = (w * 0.7).toInt().coerceAtLeast(500),
                        widthMm = 200,
                        thicknessMm = 25,
                        quantity = 1,
                        materialType = "25mm HDHMR / Ply",
                        edgeBanding = "2mm PVC (2L + 2W)"
                    )
                )
            }

            FurnitureCategory.BED -> {
                // Headboard
                parts.add(
                    CuttingPart(
                        name = "Bed Headboard Panel",
                        lengthMm = w,
                        widthMm = h,
                        thicknessMm = 25,
                        quantity = 1,
                        materialType = "25mm Plywood",
                        edgeBanding = "2mm PVC (2L + 1W)"
                    )
                )
                // Footboard
                parts.add(
                    CuttingPart(
                        name = "Bed Footboard Panel",
                        lengthMm = w,
                        widthMm = 450,
                        thicknessMm = 25,
                        quantity = 1,
                        materialType = "25mm Plywood",
                        edgeBanding = "2mm PVC (1L + 2W)"
                    )
                )
                // Side Rails
                parts.add(
                    CuttingPart(
                        name = "Bed Long Side Stringers",
                        lengthMm = d,
                        widthMm = 450,
                        thicknessMm = 25,
                        quantity = 2,
                        materialType = "25mm Plywood",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                // Internal Longitudinal & Cross Ribs
                parts.add(
                    CuttingPart(
                        name = "Internal Storage Cross Dividers",
                        lengthMm = (w - 50).coerceAtLeast(500),
                        widthMm = 400,
                        thicknessMm = 18,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "0.8mm PVC (1L)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Internal Center Spine Beam",
                        lengthMm = (d - 50).coerceAtLeast(500),
                        widthMm = 400,
                        thicknessMm = 18,
                        quantity = 1,
                        materialType = "18mm Plywood",
                        edgeBanding = "0.8mm PVC (1L)"
                    )
                )
                // Top Mattress Support Flaps (Hydraulic or manual lift)
                parts.add(
                    CuttingPart(
                        name = "Top Storage Flap Lids",
                        lengthMm = (d - 40).coerceAtLeast(500),
                        widthMm = ((w - 40) / 2).coerceAtLeast(300),
                        thicknessMm = 18,
                        quantity = 2,
                        materialType = "18mm Marine Plywood",
                        edgeBanding = "0.8mm PVC (2L + 2W)"
                    )
                )
                // Bottom Floor Liners
                parts.add(
                    CuttingPart(
                        name = "Bed Box Floor Liner",
                        lengthMm = (d - 40).coerceAtLeast(500),
                        widthMm = ((w - 40) / 2).coerceAtLeast(300),
                        thicknessMm = 9,
                        quantity = 2,
                        materialType = "9mm Plywood",
                        edgeBanding = "None"
                    )
                )
            }

            FurnitureCategory.OFFICE_DESK -> {
                // Table Top
                parts.add(
                    CuttingPart(
                        name = "Desk Main Worktop",
                        lengthMm = w,
                        widthMm = d,
                        thicknessMm = 25,
                        quantity = 1,
                        materialType = "25mm Plywood / HDHMR",
                        edgeBanding = "2mm PVC (2L + 2W)"
                    )
                )
                // Side Legs / Gable Ends
                parts.add(
                    CuttingPart(
                        name = "Side Gable Panels (Legs)",
                        lengthMm = (h - 25).coerceAtLeast(400),
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (2L + 1W)"
                    )
                )
                // Modesty Panel
                parts.add(
                    CuttingPart(
                        name = "Privacy Modesty Panel",
                        lengthMm = (w - (2 * t) - 100).coerceAtLeast(400),
                        widthMm = 450,
                        thicknessMm = t,
                        quantity = 1,
                        materialType = "18mm Plywood",
                        edgeBanding = "0.8mm PVC (2L)"
                    )
                )
                // Pedestal Cabinet (Drawers)
                if (config.drawersCount > 0) {
                    val pedW = 400
                    parts.add(
                        CuttingPart(
                            name = "Pedestal Carcass Sides",
                            lengthMm = (h - 50).coerceAtLeast(400),
                            widthMm = (d - 100).coerceAtLeast(300),
                            thicknessMm = t,
                            quantity = 2,
                            materialType = "18mm Plywood",
                            edgeBanding = "0.8mm PVC (1L + 1W)"
                        )
                    )
                    parts.add(
                        CuttingPart(
                            name = "Pedestal Drawer Fascia / Fronts",
                            lengthMm = (pedW - 10),
                            widthMm = ((h - 60) / config.drawersCount).coerceAtLeast(120),
                            thicknessMm = shutterT,
                            quantity = config.drawersCount,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
            }

            FurnitureCategory.SOFA -> {
                // Wooden Frame Sofa with optional bottom drawer storage
                parts.add(
                    CuttingPart(
                        name = "Sofa Armrest Gable Sides",
                        lengthMm = (h * 0.8).toInt().coerceAtLeast(400),
                        widthMm = d,
                        thicknessMm = 25,
                        quantity = 2,
                        materialType = "25mm Plywood",
                        edgeBanding = "2mm PVC (2L + 2W)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Sofa Backrest Structural Frame",
                        lengthMm = (w - 150).coerceAtLeast(600),
                        widthMm = (h - 200).coerceAtLeast(300),
                        thicknessMm = t,
                        quantity = 1,
                        materialType = "18mm Plywood",
                        edgeBanding = "0.8mm PVC (2L)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Seat Base Support Deck",
                        lengthMm = (w - 150).coerceAtLeast(600),
                        widthMm = (d - 120).coerceAtLeast(400),
                        thicknessMm = t,
                        quantity = 1,
                        materialType = "18mm Marine Ply",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Base Front & Rear Plinth Rails",
                        lengthMm = (w - 150).coerceAtLeast(600),
                        widthMm = 150,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                if (config.drawersCount > 0) {
                    val drW = (w - 180) / config.drawersCount
                    parts.add(
                        CuttingPart(
                            name = "Under-Seat Storage Drawer Fronts",
                            lengthMm = drW,
                            widthMm = 140,
                            thicknessMm = shutterT,
                            quantity = config.drawersCount,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
            }

            FurnitureCategory.DINING_TABLE -> {
                // Main Table Top Slab
                parts.add(
                    CuttingPart(
                        name = "Dining Table Top Slab",
                        lengthMm = w,
                        widthMm = d,
                        thicknessMm = 25,
                        quantity = 1,
                        materialType = "25mm HDHMR / Plywood",
                        edgeBanding = "2mm PVC (2L + 2W)"
                    )
                )
                // Apron Rails
                parts.add(
                    CuttingPart(
                        name = "Long Side Apron Rails",
                        lengthMm = (w - 180).coerceAtLeast(400),
                        widthMm = 100,
                        thicknessMm = 25,
                        quantity = 2,
                        materialType = "25mm Plywood",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Short Side Apron Rails",
                        lengthMm = (d - 180).coerceAtLeast(300),
                        widthMm = 100,
                        thicknessMm = 25,
                        quantity = 2,
                        materialType = "25mm Plywood",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                // L-shape / Box Legs (2 panels per corner leg)
                parts.add(
                    CuttingPart(
                        name = "Solid Box Legs (Vertical Faces)",
                        lengthMm = (h - 25).coerceAtLeast(500),
                        widthMm = 90,
                        thicknessMm = 25,
                        quantity = 8,
                        materialType = "25mm Plywood",
                        edgeBanding = "2mm PVC (2L + 1W)"
                    )
                )
                if (config.shelvesCount > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Under-Table Utility Shelf",
                            lengthMm = (w - 300).coerceAtLeast(400),
                            widthMm = (d - 300).coerceAtLeast(300),
                            thicknessMm = t,
                            quantity = config.shelvesCount,
                            materialType = "18mm Plywood",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
            }

            FurnitureCategory.SHOE_RACK -> {
                // Carcass
                parts.add(
                    CuttingPart(
                        name = "Shoe Rack Side Panels",
                        lengthMm = h,
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L + 2W)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Shoe Rack Top & Bottom",
                        lengthMm = (w - (2 * t)).coerceAtLeast(250),
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Shoe Rack Back Panel",
                        lengthMm = (h - t).coerceAtLeast(250),
                        widthMm = (w - t).coerceAtLeast(250),
                        thicknessMm = backT,
                        quantity = 1,
                        materialType = "${backT}mm Back Ply",
                        edgeBanding = "None"
                    )
                )
                if (config.shelvesCount > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Slanted / Flat Footwear Shelves",
                            lengthMm = (w - (2 * t)).coerceAtLeast(250),
                            widthMm = (d - 25).coerceAtLeast(200),
                            thicknessMm = t,
                            quantity = config.shelvesCount,
                            materialType = "18mm Plywood",
                            edgeBanding = "2mm PVC (1L + 2W)"
                        )
                    )
                }
                if (config.numberOfDoors > 0) {
                    val doorH = if (config.drawersCount > 0) (h - 150) else (h - 20)
                    parts.add(
                        CuttingPart(
                            name = "Shoe Rack Shutter Doors (Ventilated)",
                            lengthMm = doorH.coerceAtLeast(300),
                            widthMm = ((w - 6) / config.numberOfDoors).coerceAtLeast(180),
                            thicknessMm = shutterT,
                            quantity = config.numberOfDoors,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
                if (config.drawersCount > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Top Socks & Polish Drawer Fronts",
                            lengthMm = (w - (2 * t) - 8).coerceAtLeast(200),
                            widthMm = 120,
                            thicknessMm = shutterT,
                            quantity = config.drawersCount,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
            }

            FurnitureCategory.STUDY_TABLE -> {
                // Desk Table Top
                parts.add(
                    CuttingPart(
                        name = "Study Worktop Slab",
                        lengthMm = w,
                        widthMm = d,
                        thicknessMm = 25,
                        quantity = 1,
                        materialType = "25mm HDHMR / Ply",
                        edgeBanding = "2mm PVC (2L + 2W)"
                    )
                )
                // Lower Desk Gables
                parts.add(
                    CuttingPart(
                        name = "Lower Side Gable Ends",
                        lengthMm = 725,
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L + 2W)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Desk Back Modesty Panel",
                        lengthMm = (w - (2 * t) - 50).coerceAtLeast(300),
                        widthMm = 350,
                        thicknessMm = t,
                        quantity = 1,
                        materialType = "18mm Plywood",
                        edgeBanding = "0.8mm PVC (1L)"
                    )
                )
                // Overhead Bookshelf Risers (if H > 750)
                if (h > 800) {
                    val shelfH = h - 750
                    parts.add(
                        CuttingPart(
                            name = "Overhead Bookshelf Uprights",
                            lengthMm = shelfH,
                            widthMm = 280,
                            thicknessMm = t,
                            quantity = 2,
                            materialType = "18mm Plywood",
                            edgeBanding = "2mm PVC (1L + 2W)"
                        )
                    )
                    parts.add(
                        CuttingPart(
                            name = "Bookshelf Top Header",
                            lengthMm = (w - (2 * t)).coerceAtLeast(300),
                            widthMm = 280,
                            thicknessMm = t,
                            quantity = 1,
                            materialType = "18mm Plywood",
                            edgeBanding = "2mm PVC (1L)"
                        )
                    )
                    parts.add(
                        CuttingPart(
                            name = "Bookshelf Display Shelves",
                            lengthMm = (w - (2 * t)).coerceAtLeast(300),
                            widthMm = 260,
                            thicknessMm = t,
                            quantity = config.shelvesCount.coerceAtLeast(2),
                            materialType = "18mm Plywood",
                            edgeBanding = "2mm PVC (1L + 2W)"
                        )
                    )
                }
                if (config.drawersCount > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Study Table Drawer Fronts",
                            lengthMm = 380,
                            widthMm = 140,
                            thicknessMm = shutterT,
                            quantity = config.drawersCount,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
            }

            FurnitureCategory.CUSTOM_FURNITURE -> {
                // Generic Custom Cabinet
                parts.add(
                    CuttingPart(
                        name = "Cabinet Sides (Left & Right)",
                        lengthMm = h,
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L + 2W)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Cabinet Top & Bottom",
                        lengthMm = (w - (2 * t)).coerceAtLeast(200),
                        widthMm = d,
                        thicknessMm = t,
                        quantity = 2,
                        materialType = "18mm Plywood",
                        edgeBanding = "2mm PVC (1L)"
                    )
                )
                parts.add(
                    CuttingPart(
                        name = "Back Enclosure Panel",
                        lengthMm = (h - t).coerceAtLeast(200),
                        widthMm = (w - t).coerceAtLeast(200),
                        thicknessMm = backT,
                        quantity = 1,
                        materialType = "${backT}mm Back Ply",
                        edgeBanding = "None"
                    )
                )
                if (config.shelvesCount > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Internal Shelves",
                            lengthMm = (w - (2 * t)).coerceAtLeast(200),
                            widthMm = (d - 30).coerceAtLeast(150),
                            thicknessMm = t,
                            quantity = config.shelvesCount,
                            materialType = "18mm Plywood",
                            edgeBanding = "0.8mm PVC (1L + 2W)"
                        )
                    )
                }
                if (config.numberOfDoors > 0) {
                    val doorW = ((w - 6) / config.numberOfDoors).coerceAtLeast(150)
                    parts.add(
                        CuttingPart(
                            name = "Shutter Doors",
                            lengthMm = (h - 10).coerceAtLeast(200),
                            widthMm = doorW,
                            thicknessMm = shutterT,
                            quantity = config.numberOfDoors,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
                if (config.drawersCount > 0) {
                    parts.add(
                        CuttingPart(
                            name = "Drawer Fronts",
                            lengthMm = (w - (2 * t) - 10).coerceAtLeast(150),
                            widthMm = 180,
                            thicknessMm = shutterT,
                            quantity = config.drawersCount,
                            materialType = "18mm Shutter Board",
                            edgeBanding = "2mm PVC (2L + 2W)"
                        )
                    )
                }
            }
        }

        return parts
    }
}
