package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DoorBack
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CuttingPart
import com.example.model.Estimation
import com.example.model.FurnitureCategory
import com.example.model.FurnitureConfig
import com.example.model.FurnitureProject
import com.example.model.WoodFinish
import com.example.ui.components.Furniture3DCanvas
import com.example.util.AppLanguage
import com.example.util.Localization
import com.example.viewmodel.Screen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Design3DScreen(
    project: FurnitureProject?,
    config: FurnitureConfig,
    cuttingParts: List<CuttingPart>,
    estimation: Estimation,
    language: AppLanguage,
    isDoorsOpen: Boolean,
    isWireframe: Boolean,
    cameraYaw: Float,
    cameraPitch: Float,
    cameraZoom: Float,
    onRotateCamera: (Float, Float) -> Unit,
    onZoomCamera: (Float) -> Unit,
    onResetCamera: () -> Unit,
    onToggleDoors: () -> Unit,
    onToggleWireframe: () -> Unit,
    onUpdateConfig: (FurnitureConfig) -> Unit,
    onSaveProjectDetails: (projectName: String, customerName: String, phone: String, address: String) -> Unit,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    var selectedCategoryDropdown by remember { mutableStateOf(false) }
    var showScreenshotDialog by remember { mutableStateOf(false) }

    // Project metadata dialog/editor states
    var projectName by remember(project?.projectName) { mutableStateOf(project?.projectName ?: "New Project") }
    var customerName by remember(project?.customerName) { mutableStateOf(project?.customerName ?: "Client Name") }
    var customerPhone by remember(project?.customerPhone) { mutableStateOf(project?.customerPhone ?: "") }
    var customerAddress by remember(project?.customerAddress) { mutableStateOf(project?.customerAddress ?: "") }

    Column(modifier = modifier.fillMaxSize()) {
        // 1. 3D Visualization Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
        ) {
            Furniture3DCanvas(
                config = config,
                isDoorsOpen = isDoorsOpen,
                isWireframe = isWireframe,
                yawDeg = cameraYaw,
                pitchDeg = cameraPitch,
                zoom = cameraZoom,
                onRotate = onRotateCamera,
                onZoom = onZoomCamera,
                modifier = Modifier.fillMaxSize().testTag("furniture_3d_viewport")
            )

            // 3D Control overlay pill (Top Right)
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onToggleDoors,
                        modifier = Modifier.size(34.dp).testTag("toggle_doors_3d")
                    ) {
                        Icon(
                            imageVector = if (isDoorsOpen) Icons.Default.DoorBack else Icons.Default.DoorFront,
                            contentDescription = "Toggle Doors",
                            tint = if (isDoorsOpen) Color(0xFFFBBF24) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleWireframe,
                        modifier = Modifier.size(34.dp).testTag("toggle_wireframe_3d")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Toggle Wireframe",
                            tint = if (isWireframe) Color(0xFF38BDF8) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onResetCamera,
                        modifier = Modifier.size(34.dp).testTag("reset_camera_3d")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Camera",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showScreenshotDialog = true },
                        modifier = Modifier.size(34.dp).testTag("take_screenshot_3d_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take 3D Screenshot",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Dimensions badge (Bottom Left)
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = "${config.widthMm} x ${config.heightMm} x ${config.depthMm} mm",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 2. Parametric Controls & Configuration Panel
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.15f)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category selector & Quick Action shortcuts
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExposedDropdownMenuBox(
                        expanded = selectedCategoryDropdown,
                        onExpandedChange = { selectedCategoryDropdown = !selectedCategoryDropdown },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (language == AppLanguage.ENGLISH) config.category.englishName else config.category.hindiName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Localization.get("furniture_category", language)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = selectedCategoryDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = selectedCategoryDropdown,
                            onDismissRequest = { selectedCategoryDropdown = false }
                        ) {
                            FurnitureCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(if (language == AppLanguage.ENGLISH) cat.englishName else cat.hindiName) },
                                    onClick = {
                                        onUpdateConfig(
                                            config.copy(
                                                category = cat,
                                                heightMm = cat.defaultHeightMm,
                                                widthMm = cat.defaultWidthMm,
                                                depthMm = cat.defaultDepthMm,
                                                numberOfDoors = cat.defaultDoors,
                                                shelvesCount = cat.defaultShelves,
                                                drawersCount = cat.defaultDrawers,
                                                hasLoft = cat.defaultLoft
                                            )
                                        )
                                        selectedCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Dimensions Sliders (Height, Width, Depth)
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Dimensions & Structure (in mm)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Height Slider
                        DimensionSlider(
                            label = Localization.get("height_mm", language),
                            value = config.heightMm,
                            min = 600,
                            max = 2800,
                            step = 50,
                            onValueChange = { onUpdateConfig(config.copy(heightMm = it)) }
                        )

                        // Width Slider
                        DimensionSlider(
                            label = Localization.get("width_mm", language),
                            value = config.widthMm,
                            min = 400,
                            max = 3600,
                            step = 50,
                            onValueChange = { onUpdateConfig(config.copy(widthMm = it)) }
                        )

                        // Depth Slider
                        DimensionSlider(
                            label = Localization.get("depth_mm", language),
                            value = config.depthMm,
                            min = 300,
                            max = 1000,
                            step = 25,
                            onValueChange = { onUpdateConfig(config.copy(depthMm = it)) }
                        )
                    }
                }
            }

            // Internal Components: Doors, Shelves, Drawers, Loft
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Internal Partitions & Hardware",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ComponentCounter(
                                label = Localization.get("doors_count", language),
                                count = config.numberOfDoors,
                                min = 0,
                                max = 6,
                                onCountChange = { onUpdateConfig(config.copy(numberOfDoors = it)) }
                            )
                            ComponentCounter(
                                label = Localization.get("shelves_count", language),
                                count = config.shelvesCount,
                                min = 0,
                                max = 10,
                                onCountChange = { onUpdateConfig(config.copy(shelvesCount = it)) }
                            )
                            ComponentCounter(
                                label = Localization.get("drawers_count", language),
                                count = config.drawersCount,
                                min = 0,
                                max = 6,
                                onCountChange = { onUpdateConfig(config.copy(drawersCount = it)) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Loft unit toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = Localization.get("loft_unit", language),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = config.hasLoft,
                                onCheckedChange = { onUpdateConfig(config.copy(hasLoft = it)) }
                            )
                        }
                    }
                }
            }

            // Wood Finish & Laminate Swatches
            item {
                Text(
                    text = Localization.get("wood_finish", language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WoodFinish.entries) { finish ->
                        val isSelected = config.finish == finish
                        Surface(
                            onClick = { onUpdateConfig(config.copy(finish = finish)) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(finish.hexColor))
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = finish.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Quick summary footer action buttons
            item {
                OutlinedCard(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Estimate Total: ₹ ${inrFormat.format(estimation.grandTotal).replace("₹", "").trim()}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${cuttingParts.sumOf { it.quantity }} cut panels • ${estimation.ply18mmSheets} sheets",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onNavigate(Screen.CuttingList) },
                            modifier = Modifier.testTag("go_to_cutting_list_btn")
                        ) {
                            Text("Cutting List")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showScreenshotDialog) {
        AlertDialog(
            onDismissRequest = { showScreenshotDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("3D Design Snapshot Captured")
                }
            },
            text = {
                Column {
                    Text(
                        text = "3D model snapshot generated for project '${projectName}':",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Category: ${config.category.englishName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "Dimensions: ${config.widthMm} x ${config.heightMm} x ${config.depthMm} mm", fontSize = 11.sp)
                            Text(text = "Finish: ${config.finish.displayName}", fontSize = 11.sp)
                            Text(text = "Angle: Yaw ${cameraYaw.toInt()}°, Pitch ${cameraPitch.toInt()}°", fontSize = 11.sp)
                            Text(text = "Status: View saved for Quotation PDF", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showScreenshotDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun DimensionSlider(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    step: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "$value mm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange((it.toInt() / step) * step) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = ((max - min) / step) - 1,
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun ComponentCounter(
    label: String,
    count: Int,
    min: Int,
    max: Int,
    onCountChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = { if (count > min) onCountChange(count - 1) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Surface(
                onClick = { if (count < max) onCountChange(count + 1) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
