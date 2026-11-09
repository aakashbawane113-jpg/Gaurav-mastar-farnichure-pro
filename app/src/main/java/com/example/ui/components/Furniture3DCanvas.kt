package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.FurnitureCategory
import com.example.model.FurnitureConfig
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Float, val y: Float, val z: Float) {
    fun rotateYaw(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(
            x = x * cosA - z * sinA,
            y = y,
            z = x * sinA + z * cosA
        )
    }

    fun rotatePitch(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(
            x = x,
            y = y * cosA - z * sinA,
            z = y * sinA + z * cosA
        )
    }

    fun project(
        centerX: Float,
        centerY: Float,
        yawRad: Float,
        pitchRad: Float,
        zoom: Float,
        cameraDistance: Float = 1400f
    ): ProjectedPoint {
        val rotated = this.rotateYaw(yawRad).rotatePitch(pitchRad)
        val factor = cameraDistance / (cameraDistance + rotated.z)
        val screenX = centerX + rotated.x * factor * zoom
        val screenY = centerY - rotated.y * factor * zoom
        return ProjectedPoint(screenX, screenY, rotated.z)
    }
}

data class ProjectedPoint(val x: Float, val y: Float, val depth: Float)

data class Face3D(
    val vertices: List<Point3D>,
    val baseColor: Color,
    val normalZSign: Float = 1f,
    val isOutlineOnly: Boolean = false
)

@Composable
fun Furniture3DCanvas(
    config: FurnitureConfig,
    isDoorsOpen: Boolean,
    isWireframe: Boolean,
    yawDeg: Float,
    pitchDeg: Float,
    zoom: Float,
    onRotate: (deltaYaw: Float, deltaPitch: Float) -> Unit,
    onZoom: (factor: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (isWireframe) Color(0xFF0F172A) else Color(0xFF1E293B))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    if (gestureZoom != 1f) {
                        onZoom(gestureZoom)
                    }
                    if (pan != Offset.Zero) {
                        onRotate(pan.x * 0.4f, -pan.y * 0.4f)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Auto-scale model to fit nicely in viewport
            val maxDim = maxOf(config.heightMm, config.widthMm, config.depthMm).toFloat()
            val fitScale = (minOf(size.width, size.height) * 0.55f) / maxDim * zoom

            val yawRad = Math.toRadians(yawDeg.toDouble()).toFloat()
            val pitchRad = Math.toRadians(pitchDeg.toDouble()).toFloat()

            // 1. Draw floor grid grid-lines for spatial context
            drawFloorGrid(cx, cy, yawRad, pitchRad, fitScale, config.widthMm.toFloat(), config.depthMm.toFloat(), isWireframe)

            // 2. Generate 3D geometry for the furniture
            val cuboids = generateFurnitureGeometry(config, isDoorsOpen)

            // 3. Collect faces, project, sort by depth (Painter's Algorithm) and render
            val allFaces = mutableListOf<RenderFace>()

            val finishColor = Color(config.finish.hexColor)
            val insideColor = Color(config.finish.insideHexColor)

            cuboids.forEach { cuboid ->
                val faces = cuboid.getFaces(finishColor, insideColor)
                faces.forEach { face ->
                    val projVerts = face.vertices.map {
                        // Scale in mm space
                        val scaledPt = Point3D(it.x * fitScale, it.y * fitScale, it.z * fitScale)
                        scaledPt.project(cx, cy, yawRad, pitchRad, 1f)
                    }
                    val avgZ = projVerts.map { it.depth }.average().toFloat()
                    allFaces.add(RenderFace(projVerts, face.baseColor, avgZ, face.isOutlineOnly))
                }
            }

            // Painter's algorithm: sort faces farthest to nearest
            allFaces.sortBy { it.avgZ }

            // Render faces
            allFaces.forEach { renderFace ->
                val path = Path().apply {
                    if (renderFace.projectedPoints.isNotEmpty()) {
                        moveTo(renderFace.projectedPoints[0].x, renderFace.projectedPoints[0].y)
                        for (i in 1 until renderFace.projectedPoints.size) {
                            lineTo(renderFace.projectedPoints[i].x, renderFace.projectedPoints[i].y)
                        }
                        close()
                    }
                }

                if (isWireframe) {
                    drawPath(
                        path = path,
                        color = Color(0xFF38BDF8),
                        style = Stroke(width = 1.5f)
                    )
                } else {
                    drawPath(
                        path = path,
                        color = renderFace.color,
                        style = Fill
                    )
                    drawPath(
                        path = path,
                        color = Color(0x33000000),
                        style = Stroke(width = 1f)
                    )
                }
            }

            // 4. Draw Dimension Labels & lines
            drawDimensionsOverlay(cx, cy, yawRad, pitchRad, fitScale, config)
        }
    }
}

private data class RenderFace(
    val projectedPoints: List<ProjectedPoint>,
    val color: Color,
    val avgZ: Float,
    val isOutlineOnly: Boolean
)

private data class Cuboid3D(
    val minX: Float, val maxX: Float,
    val minY: Float, val maxY: Float,
    val minZ: Float, val maxZ: Float,
    val isInterior: Boolean = false,
    val customColor: Color? = null
) {
    fun getFaces(exteriorColor: Color, interiorColor: Color): List<Face3D> {
        val color = customColor ?: if (isInterior) interiorColor else exteriorColor

        // 6 faces: Front, Back, Top, Bottom, Left, Right with lighting multipliers
        val topColor = color.copy(red = (color.red * 1.15f).coerceIn(0f, 1f), green = (color.green * 1.15f).coerceIn(0f, 1f), blue = (color.blue * 1.15f).coerceIn(0f, 1f))
        val rightColor = color.copy(red = color.red * 0.85f, green = color.green * 0.85f, blue = color.blue * 0.85f)
        val leftColor = color.copy(red = color.red * 0.75f, green = color.green * 0.75f, blue = color.blue * 0.75f)
        val bottomColor = color.copy(red = color.red * 0.65f, green = color.green * 0.65f, blue = color.blue * 0.65f)
        val backColor = color.copy(red = color.red * 0.70f, green = color.green * 0.70f, blue = color.blue * 0.70f)
        val frontColor = color

        return listOf(
            // Front (+Z)
            Face3D(
                listOf(
                    Point3D(minX, minY, maxZ),
                    Point3D(maxX, minY, maxZ),
                    Point3D(maxX, maxY, maxZ),
                    Point3D(minX, maxY, maxZ)
                ),
                frontColor
            ),
            // Back (-Z)
            Face3D(
                listOf(
                    Point3D(maxX, minY, minZ),
                    Point3D(minX, minY, minZ),
                    Point3D(minX, maxY, minZ),
                    Point3D(maxX, maxY, minZ)
                ),
                backColor
            ),
            // Top (+Y)
            Face3D(
                listOf(
                    Point3D(minX, maxY, maxZ),
                    Point3D(maxX, maxY, maxZ),
                    Point3D(maxX, maxY, minZ),
                    Point3D(minX, maxY, minZ)
                ),
                topColor
            ),
            // Bottom (-Y)
            Face3D(
                listOf(
                    Point3D(minX, minY, minZ),
                    Point3D(maxX, minY, minZ),
                    Point3D(maxX, minY, maxZ),
                    Point3D(minX, minY, maxZ)
                ),
                bottomColor
            ),
            // Right (+X)
            Face3D(
                listOf(
                    Point3D(maxX, minY, maxZ),
                    Point3D(maxX, minY, minZ),
                    Point3D(maxX, maxY, minZ),
                    Point3D(maxX, maxY, maxZ)
                ),
                rightColor
            ),
            // Left (-X)
            Face3D(
                listOf(
                    Point3D(minX, minY, minZ),
                    Point3D(minX, minY, maxZ),
                    Point3D(minX, maxY, maxZ),
                    Point3D(minX, maxY, minZ)
                ),
                leftColor
            )
        )
    }
}

private fun generateFurnitureGeometry(config: FurnitureConfig, isDoorsOpen: Boolean): List<Cuboid3D> {
    val cuboids = mutableListOf<Cuboid3D>()

    val h = config.heightMm.toFloat()
    val w = config.widthMm.toFloat()
    val d = config.depthMm.toFloat()
    val t = config.carcassThicknessMm.toFloat()
    val backT = config.backThicknessMm.toFloat()
    val shutterT = config.shutterThicknessMm.toFloat()
    val skirtingH = config.baseSkirtingHeightMm.toFloat()

    val halfW = w / 2f
    val halfH = h / 2f
    val halfD = d / 2f

    when (config.category) {
        FurnitureCategory.WARDROBE -> {
            val carcassTop = if (config.hasLoft) (halfH - config.loftHeightMm.toFloat()) else halfH

            // 1. Left Side Carcass
            cuboids.add(Cuboid3D(-halfW, -halfW + t, -halfH, carcassTop, -halfD, halfD))
            // 2. Right Side Carcass
            cuboids.add(Cuboid3D(halfW - t, halfW, -halfH, carcassTop, -halfD, halfD))
            // 3. Top Panel
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, carcassTop - t, carcassTop, -halfD, halfD))
            // 4. Bottom Panel
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, -halfH + skirtingH, -halfH + skirtingH + t, -halfD, halfD))
            // 5. Skirting Plinth
            cuboids.add(Cuboid3D(-halfW, halfW, -halfH, -halfH + skirtingH, halfD - t - 20f, halfD - 20f))
            // 6. Back Panel
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, -halfH + skirtingH + t, carcassTop - t, -halfD, -halfD + backT, isInterior = true))

            // 7. Partitions (if doors >= 2)
            val partitions = if (config.numberOfDoors >= 3) 2 else if (config.numberOfDoors >= 2) 1 else 0
            val compartmentWidth = (w - (2 * t) - (partitions * t)) / (partitions + 1)

            for (p in 1..partitions) {
                val posX = -halfW + t + (p * compartmentWidth) + ((p - 1) * t)
                cuboids.add(Cuboid3D(posX, posX + t, -halfH + skirtingH + t, carcassTop - t, -halfD + backT, halfD - 15f, isInterior = true))
            }

            // 8. Shelves
            val availableHeight = carcassTop - (-halfH + skirtingH + t)
            val shelfSpacing = availableHeight / (config.shelvesCount + 1).coerceAtLeast(2)
            for (s in 1..config.shelvesCount) {
                val shelfY = -halfH + skirtingH + t + (s * shelfSpacing)
                cuboids.add(Cuboid3D(-halfW + t, halfW - t, shelfY, shelfY + t, -halfD + backT, halfD - 25f, isInterior = true))
            }

            // 9. Drawers
            if (config.drawersCount > 0) {
                val drawerH = 160f
                val drawerW = compartmentWidth - 10f
                for (dr in 0 until config.drawersCount) {
                    val drY = -halfH + skirtingH + t + (dr * (drawerH + 8f)) + 10f
                    val pullZ = if (isDoorsOpen) 220f else 0f
                    // Drawer fascia
                    cuboids.add(
                        Cuboid3D(
                            -halfW + t + 5f,
                            -halfW + t + 5f + drawerW,
                            drY,
                            drY + drawerH,
                            halfD - 5f + pullZ,
                            halfD + shutterT + pullZ,
                            customColor = Color(0xFFD97706)
                        )
                    )
                    // Drawer Handle
                    cuboids.add(
                        Cuboid3D(
                            -halfW + t + 5f + (drawerW / 2f) - 40f,
                            -halfW + t + 5f + (drawerW / 2f) + 40f,
                            drY + (drawerH / 2f) - 6f,
                            drY + (drawerH / 2f) + 6f,
                            halfD + shutterT + pullZ,
                            halfD + shutterT + 12f + pullZ,
                            customColor = Color(0xFFFBBF24)
                        )
                    )
                }
            }

            // 10. Shutter Doors
            if (config.numberOfDoors > 0) {
                val doorW = (w - 6f) / config.numberOfDoors
                val doorH = carcassTop - (-halfH + skirtingH) - 10f
                for (dIdx in 0 until config.numberOfDoors) {
                    val doorLeftX = -halfW + 3f + (dIdx * doorW)
                    if (isDoorsOpen) {
                        // Project open door rotated at 75 degrees forward
                        val openDepth = doorW * 0.9f
                        val openWidth = doorW * 0.35f
                        val isLeftHinged = dIdx % 2 == 0
                        val doorX1 = if (isLeftHinged) doorLeftX else doorLeftX + doorW - openWidth
                        val doorX2 = if (isLeftHinged) doorLeftX + openWidth else doorLeftX + doorW
                        cuboids.add(
                            Cuboid3D(
                                doorX1,
                                doorX2,
                                -halfH + skirtingH + 5f,
                                carcassTop - 5f,
                                halfD,
                                halfD + openDepth,
                                customColor = Color(config.finish.hexColor)
                            )
                        )
                    } else {
                        // Closed Shutter Door
                        cuboids.add(
                            Cuboid3D(
                                doorLeftX + 2f,
                                doorLeftX + doorW - 2f,
                                -halfH + skirtingH + 5f,
                                carcassTop - 5f,
                                halfD,
                                halfD + shutterT,
                                customColor = Color(config.finish.hexColor)
                            )
                        )
                        // Handle on each door
                        val handleX = if (dIdx % 2 == 0) doorLeftX + doorW - 25f else doorLeftX + 25f
                        cuboids.add(
                            Cuboid3D(
                                handleX - 4f,
                                handleX + 4f,
                                -halfH + skirtingH + (doorH * 0.45f),
                                -halfH + skirtingH + (doorH * 0.55f),
                                halfD + shutterT,
                                halfD + shutterT + 15f,
                                customColor = Color(0xFFFBBF24) // Gold metallic handle
                            )
                        )
                    }
                }
            }

            // 11. Loft Unit (if enabled)
            if (config.hasLoft && config.loftHeightMm > 0) {
                cuboids.add(Cuboid3D(-halfW, halfW, carcassTop, halfH, -halfD, halfD, customColor = Color(config.finish.hexColor)))
            }
        }

        FurnitureCategory.MODULAR_KITCHEN -> {
            // Kitchen Base Cabinet
            cuboids.add(Cuboid3D(-halfW, -halfW + t, -halfH, halfH, -halfD, halfD))
            cuboids.add(Cuboid3D(halfW - t, halfW, -halfH, halfH, -halfD, halfD))
            cuboids.add(Cuboid3D(-halfW, halfW, halfH - t - 30f, halfH, -halfD - 20f, halfD + 20f, customColor = Color(0xFF334155))) // Countertop
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, -halfH + skirtingH, -halfH + skirtingH + t, -halfD, halfD))

            // Shutters / Baskets
            val doorW = w / config.numberOfDoors.coerceAtLeast(1)
            for (i in 0 until config.numberOfDoors) {
                val dx = -halfW + (i * doorW)
                cuboids.add(
                    Cuboid3D(
                        dx + 3f,
                        dx + doorW - 3f,
                        -halfH + skirtingH + 5f,
                        halfH - 40f,
                        halfD,
                        halfD + shutterT,
                        customColor = Color(config.finish.hexColor)
                    )
                )
                // Profile handle
                cuboids.add(Cuboid3D(dx + 20f, dx + doorW - 20f, halfH - 48f, halfH - 42f, halfD + shutterT, halfD + shutterT + 12f, customColor = Color(0xFFE2E8F0)))
            }
        }

        FurnitureCategory.TV_UNIT -> {
            // Wall paneling
            cuboids.add(Cuboid3D(-halfW, halfW, -halfH, halfH, -halfD, -halfD + 25f, customColor = Color(config.finish.hexColor)))
            // Floating bottom console
            val consoleH = halfH * 0.5f
            cuboids.add(Cuboid3D(-halfW + 40f, halfW - 40f, -halfH + 100f, -halfH + 100f + consoleH, -halfD + 25f, halfD, customColor = Color(0xFF1E293B)))
            // Wall shelf
            cuboids.add(Cuboid3D(-halfW + 150f, halfW - 150f, halfH - 120f, halfH - 95f, -halfD + 25f, halfD - 100f, customColor = Color(0xFFD97706)))
        }

        FurnitureCategory.BED -> {
            // Headboard
            cuboids.add(Cuboid3D(-halfW, halfW, -halfH, halfH, -halfD, -halfD + 60f, customColor = Color(config.finish.hexColor)))
            // Bed frame box
            cuboids.add(Cuboid3D(-halfW + 10f, halfW - 10f, -halfH, -halfH + 350f, -halfD + 60f, halfD, customColor = Color(config.finish.hexColor)))
            // Mattress
            cuboids.add(Cuboid3D(-halfW + 25f, halfW - 25f, -halfH + 250f, -halfH + 450f, -halfD + 80f, halfD - 20f, customColor = Color(0xFFF1F5F9)))
        }

        FurnitureCategory.OFFICE_DESK -> {
            // Worktop
            cuboids.add(Cuboid3D(-halfW, halfW, halfH - 30f, halfH, -halfD, halfD, customColor = Color(config.finish.hexColor)))
            // Left & Right legs
            cuboids.add(Cuboid3D(-halfW, -halfW + t, -halfH, halfH - 30f, -halfD, halfD))
            cuboids.add(Cuboid3D(halfW - t, halfW, -halfH, halfH - 30f, -halfD, halfD))
            // Modesty panel
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, halfH - 350f, halfH - 30f, -halfD + 80f, -halfD + 80f + t, isInterior = true))
            // Pedestal Drawers on right
            if (config.drawersCount > 0) {
                cuboids.add(Cuboid3D(halfW - 350f, halfW - t, -halfH, halfH - 40f, -halfD + 50f, halfD - 20f, customColor = Color(0xFF334155)))
            }
        }

        FurnitureCategory.SOFA -> {
            // Armrests
            val armW = 120f
            cuboids.add(Cuboid3D(-halfW, -halfW + armW, -halfH, halfH * 0.75f, -halfD, halfD, customColor = Color(config.finish.hexColor)))
            cuboids.add(Cuboid3D(halfW - armW, halfW, -halfH, halfH * 0.75f, -halfD, halfD, customColor = Color(config.finish.hexColor)))
            // Backrest
            cuboids.add(Cuboid3D(-halfW + armW, halfW - armW, -halfH + 150f, halfH, -halfD, -halfD + 180f, customColor = Color(config.finish.hexColor)))
            // Seat Base & Cushions
            val pullZ = if (isDoorsOpen) 150f else 0f
            cuboids.add(Cuboid3D(-halfW + armW + 5f, halfW - armW - 5f, -halfH + 120f, -halfH + 320f, -halfD + 180f, halfD + pullZ, customColor = Color(0xFFF1F5F9)))
            // Base Plinth / Storage
            if (config.drawersCount > 0) {
                cuboids.add(Cuboid3D(-halfW + armW, halfW - armW, -halfH, -halfH + 120f, -halfD + 50f, halfD - 20f + pullZ, customColor = Color(0xFF334155)))
            }
        }

        FurnitureCategory.DINING_TABLE -> {
            // Main tabletop slab
            cuboids.add(Cuboid3D(-halfW, halfW, halfH - 35f, halfH, -halfD, halfD, customColor = Color(config.finish.hexColor)))
            // 4 Corner Solid Box Legs
            val legW = 75f
            cuboids.add(Cuboid3D(-halfW + 30f, -halfW + 30f + legW, -halfH, halfH - 35f, halfD - 30f - legW, halfD - 30f, customColor = Color(config.finish.hexColor)))
            cuboids.add(Cuboid3D(halfW - 30f - legW, halfW - 30f, -halfH, halfH - 35f, halfD - 30f - legW, halfD - 30f, customColor = Color(config.finish.hexColor)))
            cuboids.add(Cuboid3D(-halfW + 30f, -halfW + 30f + legW, -halfH, halfH - 35f, -halfD + 30f, -halfD + 30f + legW, customColor = Color(config.finish.hexColor)))
            cuboids.add(Cuboid3D(halfW - 30f - legW, halfW - 30f, -halfH, halfH - 35f, -halfD + 30f, -halfD + 30f + legW, customColor = Color(config.finish.hexColor)))
            // Long Aprons
            cuboids.add(Cuboid3D(-halfW + 30f + legW, halfW - 30f - legW, halfH - 120f, halfH - 35f, halfD - 40f, halfD - 15f, customColor = Color(config.finish.hexColor)))
            cuboids.add(Cuboid3D(-halfW + 30f + legW, halfW - 30f - legW, halfH - 120f, halfH - 35f, -halfD + 15f, -halfD + 40f, customColor = Color(config.finish.hexColor)))
            // Under-shelf
            if (config.shelvesCount > 0) {
                cuboids.add(Cuboid3D(-halfW + 120f, halfW - 120f, -halfH + 180f, -halfH + 200f, -halfD + 120f, halfD - 120f, isInterior = true))
            }
        }

        FurnitureCategory.SHOE_RACK -> {
            // Shoe Rack Carcass
            cuboids.add(Cuboid3D(-halfW, -halfW + t, -halfH, halfH, -halfD, halfD))
            cuboids.add(Cuboid3D(halfW - t, halfW, -halfH, halfH, -halfD, halfD))
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, halfH - t, halfH, -halfD, halfD))
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, -halfH + skirtingH, -halfH + skirtingH + t, -halfD, halfD))
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, -halfH + skirtingH + t, halfH - t, -halfD, -halfD + backT, isInterior = true))

            // Shoe Shelves
            val shelfRange = (halfH - t) - (-halfH + skirtingH + t)
            val step = shelfRange / (config.shelvesCount + 1).coerceAtLeast(2)
            for (i in 1..config.shelvesCount) {
                val sy = -halfH + skirtingH + t + (i * step)
                cuboids.add(Cuboid3D(-halfW + t, halfW - t, sy, sy + t, -halfD + backT, halfD - 20f, isInterior = true))
            }

            // Shutters / Doors
            if (config.numberOfDoors > 0) {
                val doorW = (w - 6f) / config.numberOfDoors
                for (dIdx in 0 until config.numberOfDoors) {
                    val doorX = -halfW + 3f + (dIdx * doorW)
                    if (isDoorsOpen) {
                        cuboids.add(Cuboid3D(doorX, doorX + (doorW * 0.35f), -halfH + skirtingH + 5f, halfH - 5f, halfD, halfD + (doorW * 0.85f), customColor = Color(config.finish.hexColor)))
                    } else {
                        cuboids.add(Cuboid3D(doorX + 2f, doorX + doorW - 2f, -halfH + skirtingH + 5f, halfH - 5f, halfD, halfD + shutterT, customColor = Color(config.finish.hexColor)))
                        // Handle
                        val hX = if (dIdx % 2 == 0) doorX + doorW - 20f else doorX + 20f
                        cuboids.add(Cuboid3D(hX - 3f, hX + 3f, (halfH * 0.2f), (halfH * 0.3f), halfD + shutterT, halfD + shutterT + 10f, customColor = Color(0xFFFBBF24)))
                    }
                }
            }
        }

        FurnitureCategory.STUDY_TABLE -> {
            val deskH = 750f - halfH // Desk surface Y
            // Main worktop
            cuboids.add(Cuboid3D(-halfW, halfW, deskH - 25f, deskH, -halfD, halfD, customColor = Color(config.finish.hexColor)))
            // Desk lower legs / gables
            cuboids.add(Cuboid3D(-halfW, -halfW + t, -halfH, deskH - 25f, -halfD, halfD))
            cuboids.add(Cuboid3D(halfW - t, halfW, -halfH, deskH - 25f, -halfD, halfD))
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, deskH - 250f, deskH - 25f, -halfD + 80f, -halfD + 80f + t, isInterior = true))

            // Overhead Bookshelf
            if (h > 800) {
                cuboids.add(Cuboid3D(-halfW, -halfW + t, deskH, halfH, -halfD, -halfD + 280f))
                cuboids.add(Cuboid3D(halfW - t, halfW, deskH, halfH, -halfD, -halfD + 280f))
                cuboids.add(Cuboid3D(-halfW + t, halfW - t, halfH - t, halfH, -halfD, -halfD + 280f))
                // Shelves
                val bRange = halfH - deskH
                val bStep = bRange / (config.shelvesCount + 1).coerceAtLeast(2)
                for (i in 1..config.shelvesCount) {
                    val sy = deskH + (i * bStep)
                    cuboids.add(Cuboid3D(-halfW + t, halfW - t, sy, sy + t, -halfD, -halfD + 270f, isInterior = true))
                }
            }

            // Pedestal Drawers
            if (config.drawersCount > 0) {
                val pedW = 350f
                val pullZ = if (isDoorsOpen) 150f else 0f
                cuboids.add(Cuboid3D(halfW - pedW, halfW - t, -halfH, deskH - 30f, halfD - 15f + pullZ, halfD + shutterT + pullZ, customColor = Color(0xFF334155)))
            }
        }

        FurnitureCategory.CUSTOM_FURNITURE -> {
            cuboids.add(Cuboid3D(-halfW, -halfW + t, -halfH, halfH, -halfD, halfD))
            cuboids.add(Cuboid3D(halfW - t, halfW, -halfH, halfH, -halfD, halfD))
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, halfH - t, halfH, -halfD, halfD))
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, -halfH, -halfH + t, -halfD, halfD))
            cuboids.add(Cuboid3D(-halfW + t, halfW - t, -halfH + t, halfH - t, -halfD, -halfD + backT, isInterior = true))
            for (i in 1..config.shelvesCount) {
                val sy = -halfH + (i * (h / (config.shelvesCount + 1)))
                cuboids.add(Cuboid3D(-halfW + t, halfW - t, sy, sy + t, -halfD + backT, halfD - 20f, isInterior = true))
            }
        }
    }

    return cuboids
}

private fun DrawScope.drawFloorGrid(
    cx: Float,
    cy: Float,
    yawRad: Float,
    pitchRad: Float,
    scale: Float,
    furnitureW: Float,
    furnitureD: Float,
    isWireframe: Boolean
) {
    val gridColor = if (isWireframe) Color(0x2238BDF8) else Color(0x2294A3B8)
    val gridRange = 1500f
    val step = 300f
    val floorY = -1100f // slightly below furniture base

    var x = -gridRange
    while (x <= gridRange) {
        val p1 = Point3D(x * scale, floorY * scale, -gridRange * scale).project(cx, cy, yawRad, pitchRad, 1f)
        val p2 = Point3D(x * scale, floorY * scale, gridRange * scale).project(cx, cy, yawRad, pitchRad, 1f)
        drawLine(gridColor, Offset(p1.x, p1.y), Offset(p2.x, p2.y), strokeWidth = 1f)
        x += step
    }

    var z = -gridRange
    while (z <= gridRange) {
        val p1 = Point3D(-gridRange * scale, floorY * scale, z * scale).project(cx, cy, yawRad, pitchRad, 1f)
        val p2 = Point3D(gridRange * scale, floorY * scale, z * scale).project(cx, cy, yawRad, pitchRad, 1f)
        drawLine(gridColor, Offset(p1.x, p1.y), Offset(p2.x, p2.y), strokeWidth = 1f)
        z += step
    }
}

private fun DrawScope.drawDimensionsOverlay(
    cx: Float,
    cy: Float,
    yawRad: Float,
    pitchRad: Float,
    scale: Float,
    config: FurnitureConfig
) {
    val dimColor = Color(0xFFF59E0B) // Amber gold dimension marks
    val halfW = config.widthMm / 2f
    val halfH = config.heightMm / 2f
    val halfD = config.depthMm / 2f

    // Width dimension line (at top front)
    val wOffset = 60f
    val ptW1 = Point3D((-halfW) * scale, (halfH + wOffset) * scale, halfD * scale).project(cx, cy, yawRad, pitchRad, 1f)
    val ptW2 = Point3D((halfW) * scale, (halfH + wOffset) * scale, halfD * scale).project(cx, cy, yawRad, pitchRad, 1f)
    drawLine(dimColor, Offset(ptW1.x, ptW1.y), Offset(ptW2.x, ptW2.y), strokeWidth = 2f)

    // Height dimension line (at left front)
    val hOffset = 60f
    val ptH1 = Point3D((-halfW - hOffset) * scale, (-halfH) * scale, halfD * scale).project(cx, cy, yawRad, pitchRad, 1f)
    val ptH2 = Point3D((-halfW - hOffset) * scale, (halfH) * scale, halfD * scale).project(cx, cy, yawRad, pitchRad, 1f)
    drawLine(dimColor, Offset(ptH1.x, ptH1.y), Offset(ptH2.x, ptH2.y), strokeWidth = 2f)

    // Depth dimension line (at right bottom)
    val dOffset = 60f
    val ptD1 = Point3D((halfW + dOffset) * scale, (-halfH) * scale, (-halfD) * scale).project(cx, cy, yawRad, pitchRad, 1f)
    val ptD2 = Point3D((halfW + dOffset) * scale, (-halfH) * scale, (halfD) * scale).project(cx, cy, yawRad, pitchRad, 1f)
    drawLine(dimColor, Offset(ptD1.x, ptD1.y), Offset(ptD2.x, ptD2.y), strokeWidth = 2f)
}
