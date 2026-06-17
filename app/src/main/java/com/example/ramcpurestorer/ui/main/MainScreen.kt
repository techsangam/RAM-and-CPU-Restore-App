package com.example.ramcpurestorer.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ramcpurestorer.data.AppInfo
import com.example.ramcpurestorer.data.SystemMonitor
import com.example.ramcpurestorer.theme.*

@Composable
fun MainScreen(
    onItemClick: (androidx.navigation3.runtime.NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    val viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(SystemMonitor(context))
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SlateNavy,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .shadow(16.dp)
            ) {
                NavigationBarItem(
                    selected = state.currentTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = CoolGrey,
                        unselectedTextColor = CoolGrey,
                        indicatorColor = GlassNavy
                    )
                )
                NavigationBarItem(
                    selected = state.currentTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Apps") },
                    label = { Text("Apps") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricTeal,
                        selectedTextColor = ElectricTeal,
                        unselectedIconColor = CoolGrey,
                        unselectedTextColor = CoolGrey,
                        indicatorColor = GlassNavy
                    )
                )
                NavigationBarItem(
                    selected = state.currentTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Filled.Info, contentDescription = "System") },
                    label = { Text("System") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SolarOrange,
                        selectedTextColor = SolarOrange,
                        unselectedIconColor = CoolGrey,
                        unselectedTextColor = CoolGrey,
                        indicatorColor = GlassNavy
                    )
                )
            }
        },
        containerColor = ObsidianBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(ObsidianBlack, SlateNavy)
                    )
                )
                .drawBehind {
                    // Futuristic grid lines
                    val gridSpacing = 60.dp.toPx()
                    val gridWidth = size.width
                    val gridHeight = size.height
                    
                    var x = 0f
                    while (x < gridWidth) {
                        drawLine(
                            color = GridLine,
                            start = Offset(x, 0f),
                            end = Offset(x, gridHeight),
                            strokeWidth = 1f
                        )
                        x += gridSpacing
                    }
                    
                    var y = 0f
                    while (y < gridHeight) {
                        drawLine(
                            color = GridLine,
                            start = Offset(0f, y),
                            end = Offset(gridWidth, y),
                            strokeWidth = 1f
                        )
                        y += gridSpacing
                    }
                }
        ) {
            when (state.currentTab) {
                0 -> DashboardTab(state = state, viewModel = viewModel)
                1 -> AppsTab(state = state, viewModel = viewModel)
                2 -> SystemTab(state = state)
            }

            // Optimization overlays
            if (state.isBoosting) {
                ProcessingOverlay(
                    title = "RAM RESTORATION IN PROGRESS",
                    progressText = state.boostProgressText,
                    accentColor = ElectricCyan
                )
            }

            if (state.isCooling) {
                ProcessingOverlay(
                    title = "CPU COOLING IN PROGRESS",
                    progressText = state.coolingProgressText,
                    accentColor = SolarOrange
                )
            }

            // Success Dialogs
            state.boostSuccessMessage?.let { msg ->
                SuccessDialog(
                    message = msg,
                    accentColor = ElectricCyan,
                    onDismiss = { viewModel.dismissBoostMessage() }
                )
            }

            state.coolingSuccessMessage?.let { msg ->
                SuccessDialog(
                    message = msg,
                    accentColor = SolarOrange,
                    onDismiss = { viewModel.dismissCoolingMessage() }
                )
            }
        }
    }
}

@Composable
fun DashboardTab(state: MainScreenUiState, viewModel: MainScreenViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title Header
        Text(
            text = "OPTIMIZER DASHBOARD",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Twin Gauges Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ResourceGauge(
                percentage = state.ramUsedPercent,
                title = "RAM USAGE",
                subtext = String.format("%.2f / %.2f GB", state.ramUsedGb, state.ramTotalGb),
                accentColor = ElectricCyan,
                glowColor = ElectricTeal
            )
            
            ResourceGauge(
                percentage = state.cpuUsagePercent,
                title = "CPU LOAD",
                subtext = "${state.cpuCores} Cores Active",
                accentColor = SolarOrange,
                glowColor = NeonGold
            )
        }

        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .border(1.dp, GridLine, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassNavy)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "SYSTEM HEALTH STATUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CoolGrey,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (state.ramUsedPercent < 75) HealthyGreen else SolarOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.ramUsedPercent < 75) "Optimal Performance" else "High Load Alert",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Text(
                        text = "TEMP: ${state.batteryTemp}°C",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.batteryTemp < 38.0f) HealthyGreen else SolarOrange
                    )
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GradientButton(
                text = "RESTORE RAM & BOOST",
                gradient = Brush.linearGradient(listOf(ElectricCyan, ElectricTeal)),
                onClick = { viewModel.boostRam() }
            )

            GradientButton(
                text = "COOL DOWN CPU PROCESSES",
                gradient = Brush.linearGradient(listOf(SolarOrange, NeonGold)),
                onClick = { viewModel.coolDownCpu() }
            )
        }
    }
}

@Composable
fun ResourceGauge(
    percentage: Int,
    title: String,
    subtext: String,
    accentColor: Color,
    glowColor: Color
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat() / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "GaugePercentage"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(150.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(130.dp)
        ) {
            // Background track
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = GridLine,
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
                )
            }
            
            // Progress arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = accentColor,
                    startAngle = 140f,
                    sweepAngle = 260f * animatedPercentage,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
                )
            }

            // Stats Center Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$percentage%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoolGrey,
                    letterSpacing = 0.5.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = subtext,
            fontSize = 11.sp,
            color = CoolGrey,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsTab(state: MainScreenUiState, viewModel: MainScreenViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(state.installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            state.installedApps
        } else {
            state.installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "APPLICATION MANAGER",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 2.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed apps...", color = CoolGrey) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = CoolGrey) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, GridLine, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SlateNavy,
                unfocusedContainerColor = GlassNavy,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No applications found.", color = CoolGrey, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredApps) { app ->
                    AppItemRow(app = app, onClick = { viewModel.openAppDetails(app.packageName) })
                }
            }
        }
    }
}

@Composable
fun AppItemRow(app: AppInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GridLine, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GlassNavy)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular app initial indicator instead of generic placeholder
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(ElectricCyan, SlateNavy)
                            )
                        )
                ) {
                    Text(
                        text = app.label.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = app.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = CoolGrey,
                        maxLines = 1
                    )
                }
            }
            
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlateNavy),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.border(1.dp, ElectricTeal.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "MANAGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
fun SystemTab(state: MainScreenUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SYSTEM SPECIFICATIONS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        val specs = listOf(
            SpecItem("Device Model", state.deviceModel, Icons.Filled.PhoneAndroid),
            SpecItem("Android OS", state.androidVersion, Icons.Filled.Info),
            SpecItem("CPU Core Count", "${state.cpuCores} physical cores", Icons.Filled.Cpu),
            SpecItem("Architecture", state.cpuArch, Icons.Filled.Build),
            SpecItem("Max VM Heap", "${Runtime.getRuntime().maxMemory() / (1024 * 1024)} MB", Icons.Filled.Warning),
            SpecItem("Optimization Level", "Deep hardware level check enabled", Icons.Filled.Lock)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            specs.forEach { spec ->
                SpecRow(spec = spec)
            }
        }
    }
}

data class SpecItem(val title: String, val value: String, val icon: ImageVector)

// Workaround fallback to avoid compile error for unresolved Cpu icon
val Icons.Filled.Cpu: ImageVector
    get() = Icons.Filled.Settings

@Composable
fun SpecRow(spec: SpecItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GridLine, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GlassNavy)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = spec.icon,
                contentDescription = null,
                tint = SolarOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = spec.title,
                    fontSize = 11.sp,
                    color = CoolGrey,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = spec.value,
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(gradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = ObsidianBlack,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun ProcessingOverlay(
    title: String,
    progressText: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator(
                color = accentColor,
                strokeWidth = 6.dp,
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = progressText,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SuccessDialog(
    message: String,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, accentColor, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateNavy)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    tint = HealthyGreen,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "OPTIMIZATION SUCCESSFUL",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    color = CoolGrey,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DISMISS",
                        color = ObsidianBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
