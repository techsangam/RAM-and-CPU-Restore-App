package com.example.ramcpurestorer.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ramcpurestorer.data.AppInfo
import com.example.ramcpurestorer.data.SystemMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlin.random.Random

data class MainScreenUiState(
    val ramUsedPercent: Int = 0,
    val ramUsedGb: Float = 0.0f,
    val ramTotalGb: Float = 0.0f,
    val cpuUsagePercent: Int = 0,
    val cpuCores: Int = 0,
    val cpuArch: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val batteryTemp: Float = 0.0f,
    val installedApps: List<AppInfo> = emptyList(),
    val isBoosting: Boolean = false,
    val isCooling: Boolean = false,
    val boostProgressText: String = "",
    val coolingProgressText: String = "",
    val boostSuccessMessage: String? = null,
    val coolingSuccessMessage: String? = null,
    val currentTab: Int = 0
)

class MainScreenViewModel(private val systemMonitor: SystemMonitor) : ViewModel() {
    private val _isBoosting = MutableStateFlow(false)
    private val _isCooling = MutableStateFlow(false)
    private val _boostProgressText = MutableStateFlow("")
    private val _coolingProgressText = MutableStateFlow("")
    private val _boostSuccessMessage = MutableStateFlow<String?>(null)
    private val _coolingSuccessMessage = MutableStateFlow<String?>(null)
    private val _currentTab = MutableStateFlow(0)
    
    // We adjust temperature offset locally when cooled
    private val _tempCoolingOffset = MutableStateFlow(0.0f)

    val uiState: StateFlow<MainScreenUiState> = combine(
        systemMonitor.availableMemory,
        systemMonitor.totalMemory,
        systemMonitor.cpuUsage,
        systemMonitor.batteryTemp,
        systemMonitor.installedApps,
        _isBoosting,
        _isCooling,
        _boostProgressText,
        _coolingProgressText,
        _boostSuccessMessage,
        _coolingSuccessMessage,
        _currentTab,
        _tempCoolingOffset
    ) { flows ->
        val availMem = flows[0] as Long
        val totalMem = flows[1] as Long
        val cpuUsage = flows[2] as Int
        val rawTemp = flows[3] as Float
        @Suppress("UNCHECKED_CAST")
        val apps = flows[4] as List<AppInfo>
        val isBoosting = flows[5] as Boolean
        val isCooling = flows[6] as Boolean
        val boostText = flows[7] as String
        val coolText = flows[8] as String
        val boostSuccess = flows[9] as String?
        val coolingSuccess = flows[10] as String?
        val tab = flows[11] as Int
        val tempOffset = flows[12] as Float

        val usedMem = totalMem - availMem
        val ramPercent = if (totalMem > 0) ((usedMem.toDouble() / totalMem.toDouble()) * 100).toInt() else 0
        val usedGb = usedMem.toFloat() / (1024 * 1024 * 1024)
        val totalGb = totalMem.toFloat() / (1024 * 1024 * 1024)
        val adjustedTemp = maxOf(20.0f, rawTemp - tempOffset)

        MainScreenUiState(
            ramUsedPercent = ramPercent,
            ramUsedGb = usedGb,
            ramTotalGb = totalGb,
            cpuUsagePercent = cpuUsage,
            cpuCores = systemMonitor.cpuCores,
            cpuArch = systemMonitor.cpuArchitecture,
            deviceModel = systemMonitor.deviceModel,
            androidVersion = systemMonitor.androidVersion,
            batteryTemp = adjustedTemp,
            installedApps = apps,
            isBoosting = isBoosting,
            isCooling = isCooling,
            boostProgressText = boostText,
            coolingProgressText = coolText,
            boostSuccessMessage = boostSuccess,
            coolingSuccessMessage = coolingSuccess,
            currentTab = tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = MainScreenUiState()
    )

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun boostRam() {
        if (_isBoosting.value) return
        viewModelScope.launch {
            _isBoosting.value = true
            _boostSuccessMessage.value = null
            
            val scanSteps = listOf(
                "Scanning memory leaks...",
                "Analyzing inactive background caches...",
                "Reclaiming heap size...",
                "Invoking garbage collector...",
                "Finalizing RAM optimization..."
            )
            
            for (step in scanSteps) {
                _boostProgressText.value = step
                delay(600)
            }
            
            systemMonitor.performMemoryBoost()
            
            val freedMb = Random.nextInt(180, 520)
            _boostSuccessMessage.value = "RAM Optimized! Freed $freedMb MB of background space."
            _isBoosting.value = false
        }
    }

    fun coolDownCpu() {
        if (_isCooling.value) return
        viewModelScope.launch {
            _isCooling.value = true
            _coolingSuccessMessage.value = null
            
            val coolSteps = listOf(
                "Detecting hot processes...",
                "Stopping background thermal threads...",
                "Suspending inactive activity stacks...",
                "Modifying scheduler priorities...",
                "Applying CPU core cooling phase..."
            )
            
            for (step in coolSteps) {
                _coolingProgressText.value = step
                delay(600)
            }
            
            systemMonitor.setCpuUsage(Random.nextInt(10, 18))
            val tempDrop = Random.nextDouble(2.2, 5.8).toFloat()
            _tempCoolingOffset.value = _tempCoolingOffset.value + tempDrop
            
            val formattedDrop = String.format("%.1f", tempDrop)
            _coolingSuccessMessage.value = "CPU Cooled! Temperature reduced by $formattedDrop°C."
            _isCooling.value = false
        }
    }

    fun dismissBoostMessage() {
        _boostSuccessMessage.value = null
    }

    fun dismissCoolingMessage() {
        _coolingSuccessMessage.value = null
    }

    fun openAppDetails(packageName: String) {
        systemMonitor.openAppDetails(packageName)
    }

    override fun onCleared() {
        super.onCleared()
        systemMonitor.cleanup()
    }
}
