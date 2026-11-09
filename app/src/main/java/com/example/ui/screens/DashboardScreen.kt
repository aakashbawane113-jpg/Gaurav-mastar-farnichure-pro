package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SingleBed
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Converters
import com.example.model.Customer
import com.example.model.FurnitureCategory
import com.example.model.FurnitureProject
import com.example.model.ProjectStatus
import com.example.util.AppLanguage
import com.example.util.Localization
import com.example.util.PdfReportGenerator
import com.example.util.ShareHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    projects: List<FurnitureProject>,
    customers: List<Customer>,
    language: AppLanguage,
    onOpenProject: (FurnitureProject) -> Unit,
    onNewProject: (FurnitureCategory) -> Unit,
    onDuplicateProject: (FurnitureProject) -> Unit,
    onDeleteProject: (FurnitureProject) -> Unit,
    onNavigateToCustomers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val converters = remember { Converters() }

    // Dialog state for delete confirmation
    var projectToDelete by remember { mutableStateOf<FurnitureProject?>(null) }

    // Calculated metrics
    val totalProjects = projects.size
    val totalCustomers = customers.size
    val completedProjects = projects.count { it.status == ProjectStatus.COMPLETED }
    val inProgressProjects = projects.count { it.status == ProjectStatus.IN_PROGRESS }

    val totalSalesValue = projects.sumOf { p ->
        val est = converters.toEstimation(p.estimationJson)
        est.grandTotal
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Metric Stats Banner Grid
            item {
                Text(
                    text = Localization.get("dashboard", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Primary Sales Value Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sales_stat_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = Localization.get("total_sales_value", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "₹ ${inrFormat.format(totalSalesValue).replace("₹", "").trim()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary 3 Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatCard(
                        title = Localization.get("total_projects", language),
                        value = totalProjects.toString(),
                        icon = Icons.Default.Folder,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = Localization.get("in_progress", language),
                        value = inProgressProjects.toString(),
                        icon = Icons.Default.PendingActions,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = Localization.get("completed_projects", language),
                        value = completedProjects.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Quick Create Category Row
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = Localization.get("new_project", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToCustomers) {
                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Localization.get("customers", language), fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Wardrobe" else "अलमारी",
                            icon = Icons.Default.Weekend,
                            onClick = { onNewProject(FurnitureCategory.WARDROBE) },
                            testTag = "quick_new_wardrobe"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Kitchen" else "किचन",
                            icon = Icons.Default.Kitchen,
                            onClick = { onNewProject(FurnitureCategory.MODULAR_KITCHEN) },
                            testTag = "quick_new_kitchen"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "TV Unit" else "टीवी यूनिट",
                            icon = Icons.Default.Tv,
                            onClick = { onNewProject(FurnitureCategory.TV_UNIT) },
                            testTag = "quick_new_tv"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Bed" else "पलंग",
                            icon = Icons.Default.SingleBed,
                            onClick = { onNewProject(FurnitureCategory.BED) },
                            testTag = "quick_new_bed"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Sofa" else "सोफा",
                            icon = Icons.Default.Weekend,
                            onClick = { onNewProject(FurnitureCategory.SOFA) },
                            testTag = "quick_new_sofa"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Dining Table" else "डाइनिंग टेबल",
                            icon = Icons.Default.TableRestaurant,
                            onClick = { onNewProject(FurnitureCategory.DINING_TABLE) },
                            testTag = "quick_new_dining"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Shoe Rack" else "शू रैक",
                            icon = Icons.Default.Folder,
                            onClick = { onNewProject(FurnitureCategory.SHOE_RACK) },
                            testTag = "quick_new_shoerack"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Study Table" else "स्टडी टेबल",
                            icon = Icons.Default.TableRestaurant,
                            onClick = { onNewProject(FurnitureCategory.STUDY_TABLE) },
                            testTag = "quick_new_studytable"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Office Desk" else "ऑफिस टेबल",
                            icon = Icons.Default.TableRestaurant,
                            onClick = { onNewProject(FurnitureCategory.OFFICE_DESK) },
                            testTag = "quick_new_desk"
                        )
                    }
                    item {
                        CategoryQuickButton(
                            title = if (language == AppLanguage.ENGLISH) "Custom" else "कस्टम",
                            icon = Icons.Default.ViewInAr,
                            onClick = { onNewProject(FurnitureCategory.CUSTOM_FURNITURE) },
                            testTag = "quick_new_custom"
                        )
                    }
                }
            }

            // 3. Projects List Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(
                        text = Localization.get("projects", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${projects.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4. Projects Cards
            if (projects.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp).fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Weekend,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Projects Yet",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap any category above to create your first 3D furniture project with automatic cutting list & quotation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    val est = remember(project.estimationJson) { converters.toEstimation(project.estimationJson) }
                    val cfg = remember(project.configJson) { converters.toConfig(project.configJson) }

                    ProjectItemCard(
                        project = project,
                        estimation = est,
                        config = cfg,
                        language = language,
                        inrFormat = inrFormat,
                        onOpen = { onOpenProject(project) },
                        onDuplicate = { onDuplicateProject(project) },
                        onDelete = { projectToDelete = project },
                        onShareWhatsApp = {
                            val parts = converters.toCuttingParts(project.cuttingPartsJson)
                            val pdfFile = PdfReportGenerator.generateQuotationPdf(context, project, cfg, parts, est)
                            ShareHelper.shareQuotationViaWhatsApp(context, pdfFile, project, est)
                        }
                    )
                }
            }
        }

        // Floating Action Button to quickly start new project
        FloatingActionButton(
            onClick = { onNewProject(FurnitureCategory.WARDROBE) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_new_project")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create New Project")
        }
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { prj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text(Localization.get("delete_project", language)) },
            text = { Text("Are you sure you want to delete '${prj.projectName}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(prj)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun CategoryQuickButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProjectItemCard(
    project: FurnitureProject,
    estimation: com.example.model.Estimation,
    config: com.example.model.FurnitureConfig,
    language: AppLanguage,
    inrFormat: NumberFormat,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val dateStr = remember(project.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(project.createdAt))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("project_card_${project.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category & Status Chips
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = onOpen,
                        label = { Text(config.category.englishName.split(" ")[0], fontSize = 11.sp) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(project.status.colorHex).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ENGLISH) project.status.label else project.status.hindiLabel,
                            color = Color(project.status.colorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = project.projectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Client: ${project.customerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (project.customerAddress.isNotEmpty()) {
                    Text(
                        text = " • ${project.customerAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Quotation Estimate",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹ ${inrFormat.format(estimation.grandTotal).replace("₹", "").trim()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShareWhatsApp, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Project",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Project",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    FilledTonalButton(
                        onClick = onOpen,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("3D", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
