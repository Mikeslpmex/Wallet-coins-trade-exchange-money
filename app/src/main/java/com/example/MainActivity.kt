package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.NewTransferDialog
import com.example.ui.dialogs.TransactionDetailDialog
import com.example.ui.screens.AntifraudScreen
import com.example.ui.screens.GatewayScreen
import com.example.ui.screens.OrionTerminalScreen
import com.example.ui.screens.PartnerHubScreen
import com.example.ui.screens.VouchersStoreScreen
import com.example.ui.theme.Amber500
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.PayShieldViewModel

enum class AppTab(val title: String, val icon: ImageVector) {
    GATEWAY("Pasarela", Icons.Default.AccountBalanceWallet),
    VOUCHERS("Vouchers", Icons.Default.CardGiftcard),
    PARTNERS("Partners", Icons.Default.Handshake),
    ANTIFRAUD("Antifraude", Icons.Default.Shield),
    TERMINAL("Lucy Bot", Icons.Default.Terminal)
}

class MainActivity : ComponentActivity() {

    private val viewModel: PayShieldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme(darkTheme = true) {
                PayShieldApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayShieldApp(viewModel: PayShieldViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(AppTab.GATEWAY) }
    var showNewTransferDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.messageSnackbar) {
        uiState.messageSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(ElectricBlue, shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Slate950, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "PayShield",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Certified Partner • MercadoLibre • OXXO • Amazon",
                                color = ElectricBlue,
                                fontSize = 9.sp
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.requiere2FACount > 0) {
                        Badge(
                            containerColor = Amber500,
                            contentColor = Slate950,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("${uiState.requiere2FACount} 2FA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) ElectricBlue else Color(0xFF94A3B8)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) ElectricBlue else Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricBlue,
                            unselectedIconColor = Color(0xFF94A3B8),
                            indicatorColor = Slate800
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate950
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.GATEWAY -> GatewayScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onOpenNewTransfer = { showNewTransferDialog = true }
                )
                AppTab.VOUCHERS -> VouchersStoreScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                AppTab.PARTNERS -> PartnerHubScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                AppTab.ANTIFRAUD -> AntifraudScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                AppTab.TERMINAL -> OrionTerminalScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }

    // Detail Dialog
    uiState.selectedTransferencia?.let { selected ->
        TransactionDetailDialog(
            transferencia = selected,
            viewModel = viewModel,
            aiResult = uiState.aiAnalisisResult,
            isAiLoading = uiState.isAiLoading,
            onDismiss = { viewModel.selectTransferencia(null) }
        )
    }

    // New Transfer Dialog
    if (showNewTransferDialog) {
        NewTransferDialog(
            viewModel = viewModel,
            onDismiss = { showNewTransferDialog = false }
        )
    }
}
