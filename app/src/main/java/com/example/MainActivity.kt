package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ProjectStatus
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.screens.CustomerListScreen
import com.example.ui.screens.CuttingListScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.Design3DScreen
import com.example.ui.screens.EstimationScreen
import com.example.ui.screens.QuotationPdfScreen
import com.example.ui.screens.SheetOptimizerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FurnitureViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartFurnitureApp()
            }
        }
    }
}

@Composable
fun SmartFurnitureApp(
    viewModel: FurnitureViewModel = viewModel()
) {
    val context = LocalContext.current

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()

    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val activeConfig by viewModel.activeConfig.collectAsStateWithLifecycle()
    val activeCuttingParts by viewModel.activeCuttingParts.collectAsStateWithLifecycle()
    val activeRates by viewModel.activeRates.collectAsStateWithLifecycle()
    val activeEstimation by viewModel.activeEstimation.collectAsStateWithLifecycle()

    val optimizationResult by viewModel.optimizationResult.collectAsStateWithLifecycle()
    val selectedSheetSize by viewModel.selectedSheetSize.collectAsStateWithLifecycle()
    val bladeKerfMm by viewModel.bladeKerfMm.collectAsStateWithLifecycle()
    val activeSheetIndex by viewModel.activeSheetIndex.collectAsStateWithLifecycle()

    val isDoorsOpen by viewModel.isDoorsOpen.collectAsStateWithLifecycle()
    val isWireframe by viewModel.isWireframe.collectAsStateWithLifecycle()
    val cameraYaw by viewModel.cameraYaw.collectAsStateWithLifecycle()
    val cameraPitch by viewModel.cameraPitch.collectAsStateWithLifecycle()
    val cameraZoom by viewModel.cameraZoom.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                currentScreen = currentScreen,
                activeProjectName = activeProject?.projectName,
                language = language,
                onNavigateBack = { viewModel.navigateBack() },
                onToggleLanguage = { viewModel.toggleLanguage() },
                onOpenBackupRestore = { showBackupRestoreDialog = true },
                onSaveProject = { showSaveDialog = true }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                hasActiveProject = activeProject != null,
                language = language,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        modifier = Modifier.fillMaxSize().testTag("main_app_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Dashboard -> {
                    DashboardScreen(
                        projects = allProjects,
                        customers = allCustomers,
                        language = language,
                        onOpenProject = { viewModel.openProject(it) },
                        onNewProject = { viewModel.createNewProject(it) },
                        onDuplicateProject = { viewModel.duplicateProject(it) },
                        onDeleteProject = { viewModel.deleteProject(it) },
                        onNavigateToCustomers = { viewModel.navigateTo(Screen.Customers) }
                    )
                }

                Screen.Design3D -> {
                    Design3DScreen(
                        project = activeProject,
                        config = activeConfig,
                        cuttingParts = activeCuttingParts,
                        estimation = activeEstimation,
                        language = language,
                        isDoorsOpen = isDoorsOpen,
                        isWireframe = isWireframe,
                        cameraYaw = cameraYaw,
                        cameraPitch = cameraPitch,
                        cameraZoom = cameraZoom,
                        onRotateCamera = { dy, dp -> viewModel.updateCameraRotation(dy, dp) },
                        onZoomCamera = { f -> viewModel.updateCameraZoom(f) },
                        onResetCamera = { viewModel.resetCamera() },
                        onToggleDoors = { viewModel.toggleDoorsOpen() },
                        onToggleWireframe = { viewModel.toggleWireframe() },
                        onUpdateConfig = { viewModel.updateConfig(it) },
                        onSaveProjectDetails = { pName, cName, ph, addr ->
                            viewModel.saveCurrentProject(projectName = pName, customerName = cName, customerPhone = ph, customerAddress = addr)
                        },
                        onNavigate = { viewModel.navigateTo(it) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.CuttingList -> {
                    CuttingListScreen(
                        cuttingParts = activeCuttingParts,
                        language = language,
                        onAddPart = { viewModel.addCuttingPart(it) },
                        onUpdatePart = { viewModel.updateCuttingPart(it) },
                        onDeletePart = { viewModel.deleteCuttingPart(it) },
                        onNavigateToOptimizer = { viewModel.navigateTo(Screen.SheetOptimizer) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.SheetOptimizer -> {
                    SheetOptimizerScreen(
                        optimizationResult = optimizationResult,
                        selectedSheetSize = selectedSheetSize,
                        bladeKerfMm = bladeKerfMm,
                        activeSheetIndex = activeSheetIndex,
                        language = language,
                        onSelectSheetSize = { viewModel.setSheetSize(it) },
                        onSelectBladeKerf = { viewModel.setBladeKerf(it) },
                        onSelectSheetIndex = { viewModel.setActiveSheetIndex(it) },
                        onNavigateToEstimation = { viewModel.navigateTo(Screen.EstimationView) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.EstimationView -> {
                    EstimationScreen(
                        project = activeProject,
                        config = activeConfig,
                        cuttingParts = activeCuttingParts,
                        estimation = activeEstimation,
                        rates = activeRates,
                        language = language,
                        onUpdateRates = { viewModel.updateRates(it) },
                        onUpdateFinancials = { pm, gst, adv -> viewModel.updateFinancials(pm, gst, adv) },
                        onNavigateToQuotation = { viewModel.navigateTo(Screen.QuotationPreview) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.QuotationPreview -> {
                    QuotationPdfScreen(
                        project = activeProject,
                        config = activeConfig,
                        cuttingParts = activeCuttingParts,
                        estimation = activeEstimation,
                        language = language,
                        onNavigateToEstimation = { viewModel.navigateTo(Screen.EstimationView) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                Screen.Customers -> {
                    CustomerListScreen(
                        customers = allCustomers,
                        language = language,
                        onAddCustomer = { viewModel.addCustomer(it) },
                        onUpdateCustomer = { viewModel.updateCustomer(it) },
                        onDeleteCustomer = { viewModel.deleteCustomer(it) },
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }
        }
    }

    // Save Project Details Dialog
    if (showSaveDialog) {
        var saveName by remember { mutableStateOf(activeProject?.projectName ?: "Furniture Project") }
        var saveClient by remember { mutableStateOf(activeProject?.customerName ?: "Client") }
        var savePhone by remember { mutableStateOf(activeProject?.customerPhone ?: "") }
        var saveAddress by remember { mutableStateOf(activeProject?.customerAddress ?: "") }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Furniture Project") },
            text = {
                Column {
                    OutlinedTextField(
                        value = saveName,
                        onValueChange = { saveName = it },
                        label = { Text("Project Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveClient,
                        onValueChange = { saveClient = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = savePhone,
                        onValueChange = { savePhone = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveAddress,
                        onValueChange = { saveAddress = it },
                        label = { Text("Site Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentProject(
                            projectName = saveName,
                            customerName = saveClient,
                            customerPhone = savePhone,
                            customerAddress = saveAddress
                        )
                        Toast.makeText(context, "Project saved successfully!", Toast.LENGTH_SHORT).show()
                        showSaveDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            repository = viewModel.projectRepository,
            onDismiss = { showBackupRestoreDialog = false }
        )
    }
}
