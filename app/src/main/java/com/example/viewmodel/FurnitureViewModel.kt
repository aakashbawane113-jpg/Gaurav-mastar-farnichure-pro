package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Converters
import com.example.data.ProjectRepository
import com.example.model.Customer
import com.example.model.CuttingPart
import com.example.model.Estimation
import com.example.model.FurnitureCategory
import com.example.model.FurnitureConfig
import com.example.model.FurnitureProject
import com.example.model.MaterialRates
import com.example.model.OptimizationResult
import com.example.model.ProjectStatus
import com.example.model.StandardSheetSize
import com.example.model.WoodFinish
import com.example.util.AppLanguage
import com.example.util.CuttingListGenerator
import com.example.util.CuttingOptimizer
import com.example.util.EstimationCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Dashboard : Screen()
    data object Design3D : Screen()
    data object CuttingList : Screen()
    data object SheetOptimizer : Screen()
    data object EstimationView : Screen()
    data object QuotationPreview : Screen()
    data object Customers : Screen()
}

class FurnitureViewModel(application: Application) : AndroidViewModel(application) {

    private val converters = Converters()
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val projectRepository = ProjectRepository(database.projectDao(), database.customerDao())
    private val repository = projectRepository

    val allProjects: StateFlow<List<FurnitureProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    // App Language
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Active project editing
    private val _activeProject = MutableStateFlow<FurnitureProject?>(null)
    val activeProject: StateFlow<FurnitureProject?> = _activeProject.asStateFlow()

    private val _activeConfig = MutableStateFlow(FurnitureConfig())
    val activeConfig: StateFlow<FurnitureConfig> = _activeConfig.asStateFlow()

    private val _activeCuttingParts = MutableStateFlow<List<CuttingPart>>(emptyList())
    val activeCuttingParts: StateFlow<List<CuttingPart>> = _activeCuttingParts.asStateFlow()

    private val _activeRates = MutableStateFlow(MaterialRates())
    val activeRates: StateFlow<MaterialRates> = _activeRates.asStateFlow()

    private val _activeEstimation = MutableStateFlow(Estimation())
    val activeEstimation: StateFlow<Estimation> = _activeEstimation.asStateFlow()

    // Sheet Optimization
    private val _optimizationResult = MutableStateFlow<OptimizationResult?>(null)
    val optimizationResult: StateFlow<OptimizationResult?> = _optimizationResult.asStateFlow()

    private val _selectedSheetSize = MutableStateFlow(StandardSheetSize.SIZE_8x4)
    val selectedSheetSize: StateFlow<StandardSheetSize> = _selectedSheetSize.asStateFlow()

    private val _bladeKerfMm = MutableStateFlow(3)
    val bladeKerfMm: StateFlow<Int> = _bladeKerfMm.asStateFlow()

    private val _activeSheetIndex = MutableStateFlow(0)
    val activeSheetIndex: StateFlow<Int> = _activeSheetIndex.asStateFlow()

    // 3D Visualizer controls
    private val _isDoorsOpen = MutableStateFlow(false)
    val isDoorsOpen: StateFlow<Boolean> = _isDoorsOpen.asStateFlow()

    private val _isWireframe = MutableStateFlow(false)
    val isWireframe: StateFlow<Boolean> = _isWireframe.asStateFlow()

    private val _cameraYaw = MutableStateFlow(35f)
    val cameraYaw: StateFlow<Float> = _cameraYaw.asStateFlow()

    private val _cameraPitch = MutableStateFlow(20f)
    val cameraPitch: StateFlow<Float> = _cameraPitch.asStateFlow()

    private val _cameraZoom = MutableStateFlow(1f)
    val cameraZoom: StateFlow<Float> = _cameraZoom.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkAndSeedIfEmpty()
        }
    }

    // Navigation methods
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        } else if (_currentScreen.value != Screen.Dashboard) {
            _currentScreen.value = Screen.Dashboard
            return true
        }
        return false
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
    }

    // Project selection & creation
    fun openProject(project: FurnitureProject) {
        _activeProject.value = project
        val config = converters.toConfig(project.configJson)
        _activeConfig.value = config

        val parts = if (project.cuttingPartsJson.isNotEmpty()) {
            converters.toCuttingParts(project.cuttingPartsJson)
        } else {
            CuttingListGenerator.generateCuttingList(config)
        }
        _activeCuttingParts.value = parts

        val rates = if (project.ratesJson.isNotEmpty()) {
            converters.toRates(project.ratesJson)
        } else {
            MaterialRates()
        }
        _activeRates.value = rates

        val est = if (project.estimationJson.isNotEmpty()) {
            converters.toEstimation(project.estimationJson)
        } else {
            EstimationCalculator.calculateEstimation(config, parts, rates)
        }
        _activeEstimation.value = est

        runOptimization()
        navigateTo(Screen.Design3D)
    }

    fun createNewProject(category: FurnitureCategory = FurnitureCategory.WARDROBE) {
        val config = FurnitureConfig(
            category = category,
            heightMm = category.defaultHeightMm,
            widthMm = category.defaultWidthMm,
            depthMm = category.defaultDepthMm,
            numberOfDoors = category.defaultDoors,
            shelvesCount = category.defaultShelves,
            drawersCount = category.defaultDrawers,
            hasLoft = category.defaultLoft
        )
        val parts = CuttingListGenerator.generateCuttingList(config)
        val rates = MaterialRates()
        val est = EstimationCalculator.calculateEstimation(config, parts, rates)

        val newProject = FurnitureProject(
            customerName = "New Customer",
            projectName = "Custom ${category.englishName} Project",
            status = ProjectStatus.DRAFT,
            configJson = converters.fromConfig(config),
            cuttingPartsJson = converters.fromCuttingParts(parts),
            ratesJson = converters.fromRates(rates),
            estimationJson = converters.fromEstimation(est)
        )

        _activeProject.value = newProject
        _activeConfig.value = config
        _activeCuttingParts.value = parts
        _activeRates.value = rates
        _activeEstimation.value = est

        runOptimization()
        navigateTo(Screen.Design3D)
    }

    // Config updates
    fun updateConfig(newConfig: FurnitureConfig) {
        _activeConfig.value = newConfig
        // Auto-regenerate cutting list
        val newParts = CuttingListGenerator.generateCuttingList(newConfig)
        _activeCuttingParts.value = newParts

        // Recalculate estimation
        recalculateEstimation(newParts = newParts)
        runOptimization()
    }

    fun updateDimensions(height: Int, width: Int, depth: Int) {
        updateConfig(_activeConfig.value.copy(heightMm = height, widthMm = width, depthMm = depth))
    }

    fun updateDoorShelves(doors: Int, shelves: Int, drawers: Int) {
        updateConfig(_activeConfig.value.copy(numberOfDoors = doors, shelvesCount = shelves, drawersCount = drawers))
    }

    fun updateFinish(finish: WoodFinish) {
        _activeConfig.value = _activeConfig.value.copy(finish = finish)
    }

    fun updateLoft(hasLoft: Boolean, loftHeight: Int) {
        updateConfig(_activeConfig.value.copy(hasLoft = hasLoft, loftHeightMm = loftHeight))
    }

    // Cutting parts management
    fun addCuttingPart(part: CuttingPart) {
        val updated = _activeCuttingParts.value + part
        _activeCuttingParts.value = updated
        recalculateEstimation(newParts = updated)
        runOptimization()
    }

    fun updateCuttingPart(part: CuttingPart) {
        val updated = _activeCuttingParts.value.map { if (it.id == part.id) part else it }
        _activeCuttingParts.value = updated
        recalculateEstimation(newParts = updated)
        runOptimization()
    }

    fun deleteCuttingPart(partId: String) {
        val updated = _activeCuttingParts.value.filterNot { it.id == partId }
        _activeCuttingParts.value = updated
        recalculateEstimation(newParts = updated)
        runOptimization()
    }

    // Rates & Estimation
    fun updateRates(newRates: MaterialRates) {
        _activeRates.value = newRates
        recalculateEstimation(rates = newRates)
    }

    fun updateFinancials(profitMarginPercent: Double, gstPercent: Double, advancePaid: Double) {
        val est = _activeEstimation.value.copy(
            profitMarginPercent = profitMarginPercent,
            gstPercent = gstPercent,
            advancePaid = advancePaid
        )
        _activeEstimation.value = est
    }

    private fun recalculateEstimation(
        newParts: List<CuttingPart> = _activeCuttingParts.value,
        rates: MaterialRates = _activeRates.value
    ) {
        val curEst = _activeEstimation.value
        val updated = EstimationCalculator.calculateEstimation(
            config = _activeConfig.value,
            parts = newParts,
            rates = rates,
            profitMarginPercent = curEst.profitMarginPercent,
            gstPercent = curEst.gstPercent,
            advancePaid = curEst.advancePaid,
            transportCost = curEst.transportationCost,
            installCost = curEst.installationCost,
            otherExpenses = curEst.otherExpenses
        )
        _activeEstimation.value = updated
    }

    // Sheet Optimization
    fun setSheetSize(size: StandardSheetSize) {
        _selectedSheetSize.value = size
        runOptimization()
    }

    fun setBladeKerf(kerfMm: Int) {
        _bladeKerfMm.value = kerfMm
        runOptimization()
    }

    fun setActiveSheetIndex(index: Int) {
        _activeSheetIndex.value = index
    }

    fun runOptimization() {
        val res = CuttingOptimizer.optimize(
            parts = _activeCuttingParts.value,
            sheetSize = _selectedSheetSize.value,
            bladeKerfMm = _bladeKerfMm.value
        )
        _optimizationResult.value = res
        _activeSheetIndex.value = 0
    }

    // 3D Visualizer controls
    fun toggleDoorsOpen() {
        _isDoorsOpen.value = !_isDoorsOpen.value
    }

    fun toggleWireframe() {
        _isWireframe.value = !_isWireframe.value
    }

    fun updateCameraRotation(deltaYaw: Float, deltaPitch: Float) {
        _cameraYaw.value = (_cameraYaw.value + deltaYaw) % 360f
        _cameraPitch.value = (_cameraPitch.value + deltaPitch).coerceIn(-45f, 80f)
    }

    fun updateCameraZoom(factor: Float) {
        _cameraZoom.value = (_cameraZoom.value * factor).coerceIn(0.5f, 2.5f)
    }

    fun resetCamera() {
        _cameraYaw.value = 35f
        _cameraPitch.value = 20f
        _cameraZoom.value = 1f
    }

    // Save & Project Operations
    fun saveCurrentProject(
        projectName: String = _activeProject.value?.projectName ?: "Furniture Project",
        customerName: String = _activeProject.value?.customerName ?: "Client",
        customerPhone: String = _activeProject.value?.customerPhone ?: "",
        customerAddress: String = _activeProject.value?.customerAddress ?: "",
        status: ProjectStatus = _activeProject.value?.status ?: ProjectStatus.ESTIMATED
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = _activeProject.value?.copy(
                projectName = projectName,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                status = status,
                updatedAt = System.currentTimeMillis(),
                configJson = converters.fromConfig(_activeConfig.value),
                cuttingPartsJson = converters.fromCuttingParts(_activeCuttingParts.value),
                ratesJson = converters.fromRates(_activeRates.value),
                estimationJson = converters.fromEstimation(_activeEstimation.value)
            ) ?: FurnitureProject(
                projectName = projectName,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                status = status,
                configJson = converters.fromConfig(_activeConfig.value),
                cuttingPartsJson = converters.fromCuttingParts(_activeCuttingParts.value),
                ratesJson = converters.fromRates(_activeRates.value),
                estimationJson = converters.fromEstimation(_activeEstimation.value)
            )

            if (project.id == 0L) {
                val newId = repository.insertProject(project)
                _activeProject.value = project.copy(id = newId)
            } else {
                repository.updateProject(project)
                _activeProject.value = project
            }
        }
    }

    fun duplicateProject(project: FurnitureProject) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.duplicateProject(project)
        }
    }

    fun deleteProject(project: FurnitureProject) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProject(project)
            if (_activeProject.value?.id == project.id) {
                _activeProject.value = null
                _currentScreen.value = Screen.Dashboard
            }
        }
    }

    // Customers
    fun addCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCustomer(customer)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomer(customer)
        }
    }
}
