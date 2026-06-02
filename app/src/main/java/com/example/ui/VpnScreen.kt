package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VpnRecord
import com.example.vpn.ConnectionStatus
import com.example.vpn.VpnProtocol
import com.example.vpn.VpnServer
import com.example.vpn.VpnViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class VpnTab {
    TUNNEL,
    SPEEDTEST,
    HISTORY
}

// Sleek Design Theme Colors (Peach / Clay / Terracotta)
private object SleekPalette {
    val background = Color(0xFFFDF8F6)
    val containerBg = Color(0xFFF5E0D8)
    val textPrimary = Color(0xFF221A18)
    val textSecondary = Color(0xFF8F4C38)
    val subtleText = Color(0x998F4C38)
    val borderLight = Color(0xFFE5CDC2)
    val orangeClay = Color(0xFF8F4C38)
    val softPeach = Color(0xFFFFB4A2)
    val emeraldShield = Color(0xFF34A853)
    val crimsonOff = Color(0xFFD32F2F)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VpnScreen(viewModel: VpnViewModel) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val activeServer by viewModel.activeServer.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    val ping by viewModel.ping.collectAsState()
    val activeIpAddress by viewModel.activeIpAddress.collectAsState()
    val connectionDuration by viewModel.connectionDuration.collectAsState()
    val isSpeedTesting by viewModel.isSpeedTesting.collectAsState()
    val speedTestValue by viewModel.speedTestValue.collectAsState()
    val connectionLogs by viewModel.connectionLogs.collectAsState()

    var activeTab by remember { mutableStateOf(VpnTab.TUNNEL) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SleekPalette.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SleekPalette.orangeClay),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Aero Secure VPN icon",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AERO SECURE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = SleekPalette.textPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "VIRTUAL NETWORK COGNITION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = SleekPalette.textSecondary,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SleekPalette.containerBg)
                            .clickable { showHelpDialog = true }
                            .testTag("help_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Help diagnostics details",
                            tint = SleekPalette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Premium Segmented Tab Selector (Tunnel, Speedtest, Database Logs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SleekPalette.containerBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        Triple(VpnTab.TUNNEL, Icons.Outlined.Security, "TUNNEL"),
                        Triple(VpnTab.SPEEDTEST, Icons.Outlined.Speed, "DIAGNOSTICS"),
                        Triple(VpnTab.HISTORY, Icons.Outlined.History, "LOGS")
                    ).forEach { (tab, icon, label) ->
                        val isSelected = activeTab == tab
                        val bgCol by animateColorAsState(if (isSelected) SleekPalette.orangeClay else Color.Transparent)
                        val contentCol by animateColorAsState(if (isSelected) Color.White else SleekPalette.textSecondary)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgCol)
                                .clickable { activeTab = tab }
                                .testTag("tab_selector_${tab.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = contentCol,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = contentCol
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Safearea paddings for navigation keys
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(150)) with fadeOut(animationSpec = tween(150))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { currentScreen ->
            when (currentScreen) {
                VpnTab.TUNNEL -> TunnelTabContent(
                    viewModel = viewModel,
                    connectionStatus = connectionStatus,
                    activeServer = activeServer,
                    selectedServer = selectedServer,
                    selectedProtocol = selectedProtocol,
                    downloadSpeed = downloadSpeed,
                    uploadSpeed = uploadSpeed,
                    ping = ping,
                    activeIpAddress = activeIpAddress,
                    connectionDuration = connectionDuration
                )
                VpnTab.SPEEDTEST -> SpeedTestTabContent(
                    viewModel = viewModel,
                    connectionStatus = connectionStatus,
                    activeServer = activeServer,
                    isSpeedTesting = isSpeedTesting,
                    speedTestValue = speedTestValue
                )
                VpnTab.HISTORY -> HistoryTabContent(
                    viewModel = viewModel,
                    connectionLogs = connectionLogs
                )
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SleekPalette.orangeClay
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AERO DIAGNOSTICS",
                        color = SleekPalette.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "This simulates a secure physical VPN proxy mapping traffic across strategic gateway servers in India, USA, and UK.",
                        color = SleekPalette.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Connection establishes handshakes on selected protocols.\n" +
                                "• Speed metrics fluctuate on connected loads in real-time.\n" +
                                "• Completed sessions record elapsed seconds & peak telemetry to SQL (Room) databases for dynamic audit analytics.\n" +
                                "• Run full speed diagnostic dial test metrics inside diagnostics.",
                        color = SleekPalette.textSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showHelpDialog = false },
                    modifier = Modifier.testTag("dismiss_help_dialog")
                ) {
                    Text(
                        "DISMISS",
                        fontWeight = FontWeight.Bold,
                        color = SleekPalette.orangeClay,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            containerColor = SleekPalette.background,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ==========================================
// TUNNEL CONNECTION TAB
// ==========================================
@Composable
private fun TunnelTabContent(
    viewModel: VpnViewModel,
    connectionStatus: ConnectionStatus,
    activeServer: VpnServer?,
    selectedServer: VpnServer,
    selectedProtocol: VpnProtocol,
    downloadSpeed: Double,
    uploadSpeed: Double,
    ping: Int,
    activeIpAddress: String,
    connectionDuration: Long
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Shield Banner
        item {
            val statusTitle: String
            val statusSub: String
            val bannerColor: Color
            val iconVec: androidx.compose.ui.graphics.vector.ImageVector

            when (connectionStatus) {
                ConnectionStatus.CONNECTED -> {
                    statusTitle = "SECURED GATEWAY CONNECTION ACTIVE"
                    statusSub = "YOUR PHYSICAL DATA PACKETS ARE DEEP ENCRYPTED"
                    bannerColor = SleekPalette.emeraldShield.copy(alpha = 0.12f)
                    iconVec = Icons.Default.Lock
                }
                ConnectionStatus.CONNECTING -> {
                    statusTitle = "INITIALIZING LINE HANDSHAKE..."
                    statusSub = "ESTABLISHING CERTIFICATES ON SECURE PROTOCOLS"
                    bannerColor = SleekPalette.softPeach.copy(alpha = 0.3f)
                    iconVec = Icons.Default.CellTower
                }
                ConnectionStatus.DISCONNECTING -> {
                    statusTitle = "TEARING DOWN ENCRYPTED SOCK..."
                    statusSub = "PURGING TEMPORARY SESSION LOG CACHES"
                    bannerColor = SleekPalette.crimsonOff.copy(alpha = 0.1f)
                    iconVec = Icons.Default.RemoveCircleOutline
                }
                ConnectionStatus.DISCONNECTED -> {
                    statusTitle = "UNSECURED (VPN OFF)"
                    statusSub = "YOUR WEB PRESENCE MAY BE PUBLICLY ACCESSED"
                    bannerColor = SleekPalette.crimsonOff.copy(alpha = 0.08f)
                    iconVec = Icons.Default.HeartBroken
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bannerColor)
                    .border(1.dp, SleekPalette.borderLight.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (connectionStatus == ConnectionStatus.CONNECTED) SleekPalette.emeraldShield.copy(alpha = 0.2f)
                            else SleekPalette.orangeClay.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVec,
                        contentDescription = null,
                        tint = if (connectionStatus == ConnectionStatus.CONNECTED) SleekPalette.emeraldShield else SleekPalette.orangeClay,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = SleekPalette.textPrimary
                    )
                    Text(
                        text = statusSub,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekPalette.textSecondary
                    )
                }
            }
        }

        // Beautiful Radial Connection Handshake Switch Button
        item {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(210.dp),
                contentAlignment = Alignment.Center
            ) {
                // Infinite Pulsating Rings during handshake
                val infiniteTransition = rememberInfiniteTransition()
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                val isProcessing = connectionStatus == ConnectionStatus.CONNECTING || connectionStatus == ConnectionStatus.DISCONNECTING

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            SleekPalette.softPeach.copy(alpha = pulseAlpha),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = (70.dp.toPx() * pulseScale)
                                )
                            }
                    )
                }

                // Main Solid Dial Circular Button Component
                val buttonBgColor by animateColorAsState(
                    targetValue = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> SleekPalette.emeraldShield
                        ConnectionStatus.CONNECTING -> SleekPalette.softPeach
                        ConnectionStatus.DISCONNECTING -> SleekPalette.borderLight
                        ConnectionStatus.DISCONNECTED -> SleekPalette.orangeClay
                    },
                    animationSpec = tween(400)
                )

                Card(
                    modifier = Modifier
                        .size(144.dp)
                        .clip(CircleShape)
                        .border(
                            BorderStroke(4.dp, SleekPalette.containerBg),
                            CircleShape
                        )
                        .clickable { viewModel.toggleConnection() }
                        .testTag("connect_toggle_button"),
                    colors = CardDefaults.cardColors(containerColor = buttonBgColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (connectionStatus == ConnectionStatus.CONNECTED) Icons.Default.PowerSettingsNew else Icons.Outlined.PowerSettingsNew,
                                contentDescription = "Connect Toggle",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> "SECURED"
                                    ConnectionStatus.CONNECTING -> "TUNNELING"
                                    ConnectionStatus.DISCONNECTING -> "ENDING"
                                    ConnectionStatus.DISCONNECTED -> "CONNECT"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Active Connection Metrics Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekPalette.containerBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SleekPalette.borderLight, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // PING
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PING",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = SleekPalette.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (connectionStatus == ConnectionStatus.CONNECTED) "${ping}ms" else "--",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPalette.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(SleekPalette.borderLight)
                    )

                    // DOWNLOAD
                    Column(
                        modifier = Modifier.weight(1.2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = SleekPalette.textSecondary,
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "DOWN SPEED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = SleekPalette.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (connectionStatus == ConnectionStatus.CONNECTED) "${downloadSpeed} Mbps" else "0.00",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPalette.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(SleekPalette.borderLight)
                    )

                    // UPLOAD
                    Column(
                        modifier = Modifier.weight(1.2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = SleekPalette.textSecondary,
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "UP SPEED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = SleekPalette.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (connectionStatus == ConnectionStatus.CONNECTED) "${uploadSpeed} Mbps" else "0.00",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPalette.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Active Connection IP Address and Secure Session Timer Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SleekPalette.borderLight.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SECURED IPS VIRTUAL ROUTING",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = SleekPalette.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeIpAddress,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPalette.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("ip_address_display")
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SESSION TIMER",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = SleekPalette.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedTime = remember(connectionDuration) {
                            val mins = connectionDuration / 60
                            val secs = connectionDuration % 60
                            String.format("%02d:%02d", mins, secs)
                        }
                        Text(
                            text = if (connectionStatus == ConnectionStatus.CONNECTED) formattedTime else "00:00",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (connectionStatus == ConnectionStatus.CONNECTED) SleekPalette.orangeClay else SleekPalette.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("session_timer_display")
                        )
                    }
                }
            }
        }

        // Protocol Selector Card
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "SECURE TUNNEL PROTOCOLS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = SleekPalette.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VpnProtocol.values().forEach { protocol ->
                        val isSelected = selectedProtocol == protocol
                        val isDisconnected = connectionStatus == ConnectionStatus.DISCONNECTED

                        val borderCol = if (isSelected) SleekPalette.orangeClay else SleekPalette.borderLight
                        val bgCol = if (isSelected) SleekPalette.containerBg else Color.White
                        val textCol = if (isSelected) SleekPalette.textPrimary else SleekPalette.textSecondary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgCol)
                                .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                .clickable(enabled = isDisconnected) { viewModel.selectProtocol(protocol) }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("protocol_${protocol.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = protocol.displayName.substringBefore(" ("),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isDisconnected) textCol else textCol.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Server Selection Panel
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "AVAILABLE SECURE GATEWAYS(USA / UK / INDIA)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = SleekPalette.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Render all servers (Mumbai, Delhi, New York, Silicon Valley, London, Manchester)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SleekPalette.borderLight)
                ) {
                    Column {
                        viewModel.servers.forEachIndexed { i, server ->
                            val isSelected = selectedServer.id == server.id
                            val isActive = activeServer?.id == server.id
                            val isDisconnected = connectionStatus == ConnectionStatus.DISCONNECTED

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isDisconnected) { viewModel.selectServer(server) }
                                    .background(
                                        if (isSelected) SleekPalette.containerBg.copy(alpha = 0.4f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("server_item_${server.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Flag bubble
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SleekPalette.background),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = server.flagEmoji, fontSize = 20.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = server.cityName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekPalette.textPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SleekPalette.orangeClay.copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = server.countryCode,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SleekPalette.orangeClay,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${server.countryName} • Gateway server proxy socket",
                                        fontSize = 9.sp,
                                        color = SleekPalette.textSecondary
                                    )
                                }

                                // Load and Latency Metrics indicator
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = if (server.loadPercentage < 50) SleekPalette.emeraldShield else SleekPalette.orangeClay,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${server.basePingMs}ms",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SleekPalette.textPrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Text(
                                        text = "LOAD ${server.loadPercentage}%",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekPalette.textSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Radio indicator selection check
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { if (isDisconnected) viewModel.selectServer(server) },
                                    enabled = isDisconnected,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = SleekPalette.orangeClay,
                                        unselectedColor = SleekPalette.borderLight
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (i < viewModel.servers.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color = SleekPalette.borderLight.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SPEED TEST DIAGNOSTICS TAB
// ==========================================
@Composable
private fun SpeedTestTabContent(
    viewModel: VpnViewModel,
    connectionStatus: ConnectionStatus,
    activeServer: VpnServer?,
    isSpeedTesting: Boolean,
    speedTestValue: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (connectionStatus != ConnectionStatus.CONNECTED) {
            // Unconnected State Prompt
            Spacer(modifier = Modifier.weight(0.2f))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SleekPalette.containerBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = SleekPalette.orangeClay,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "SECURED CONNECTION ENFORCED",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = SleekPalette.textPrimary
            )
            Text(
                text = "Speed diagnostics require an established secure connection. First, connect to an Indian, US, or UK tunnel gateway to test network latency & speed dial indices.",
                fontSize = 11.sp,
                color = SleekPalette.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.weight(0.8f))
        } else {
            // Speedmeter UI block when Connected
            activeServer?.let { server ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NETWORK DIAGNOISTICS PORT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = SleekPalette.textSecondary
                    )
                    Text(
                        text = "${server.cityName} Gateway • ${selectedProtocolText(viewModel)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPalette.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful interactive Canvas Speedometer Dial
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw speedometer dial backings
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 10.dp.toPx()
                        val diameter = size.minDimension - strokeW
                        val radius = diameter / 2f
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)

                        // 1. Draw dial static background arc
                        drawArc(
                            color = SleekPalette.containerBg,
                            startAngle = 140f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        // 2. Draw colored sweep indicators
                        val maxDialValue = 250.0
                        val rawPct = (speedTestValue / maxDialValue).coerceIn(0.0, 1.0)
                        val angleSweep = 260f * rawPct.toFloat()

                        if (angleSweep > 0f) {
                            drawArc(
                                color = SleekPalette.orangeClay,
                                startAngle = 140f,
                                sweepAngle = angleSweep,
                                useCenter = false,
                                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }

                        // 3. Draw notch marks along dial path
                        for (i in 0..10) {
                            val angleRad = ((140f + (260f * (i / 10f))) * PI / 180f).toFloat()
                            val innerLen = radius * 0.9f
                            val outerLen = radius * 0.96f

                            val startPt = Offset(
                                centerOffset.x + cos(angleRad) * innerLen,
                                centerOffset.y + sin(angleRad) * innerLen
                            )
                            val endPt = Offset(
                                centerOffset.x + cos(angleRad) * outerLen,
                                centerOffset.y + sin(angleRad) * outerLen
                            )

                            drawLine(
                                color = SleekPalette.textSecondary.copy(alpha = 0.5f),
                                start = startPt,
                                end = endPt,
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // 4. Draw pointer needle pin indicator
                        val pointerAngleRad = ((140f + angleSweep) * PI / 180f).toFloat()
                        val needleLength = radius * 0.85f
                        val endNeedleOffset = Offset(
                            centerOffset.x + cos(pointerAngleRad) * needleLength,
                            centerOffset.y + sin(pointerAngleRad) * needleLength
                        )

                        drawLine(
                            color = SleekPalette.orangeClay,
                            start = centerOffset,
                            end = endNeedleOffset,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // 5. Dial base cap point
                        drawCircle(
                            color = SleekPalette.textPrimary,
                            radius = 6.dp.toPx()
                        )
                        drawCircle(
                            color = SleekPalette.softPeach,
                            radius = 3.dp.toPx()
                        )
                    }

                    // Numeric stats container in center of speedometer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 80.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", speedTestValue),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = SleekPalette.textPrimary
                        )
                        Text(
                            text = "Mbps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPalette.textSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Trigger Button to Start Speedometer sweep runs
                Button(
                    onClick = { viewModel.runSpeedTest() },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPalette.orangeClay),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSpeedTesting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("speed_test_button")
                ) {
                    val label = if (isSpeedTesting) "RUNNING TELEMETRY EVAL..." else "LAUNCH LINE SPEED TEST"
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                // Extra summary card showing peak server benchmark info
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekPalette.containerBg.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = SleekPalette.orangeClay)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BENCHMARK PERFORMANCE RATINGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = SleekPalette.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This analyzes current node capacity & measures structural ping offsets. Average network performance is typically higher on Indian local endpoints while UK/US proxy tunnels trade mild ping increments for global routing keys.",
                            fontSize = 10.sp,
                            color = SleekPalette.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun selectedProtocolText(viewModel: VpnViewModel): String {
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    return selectedProtocol.displayName.substringBefore(" (")
}

// ==========================================
// HISTORY / PERSISTENCE LOGS TAB (Room DB)
// ==========================================
@Composable
private fun HistoryTabContent(
    viewModel: VpnViewModel,
    connectionLogs: List<VpnRecord>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekPalette.orangeClay),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LIFETIME SECURED ANALYTICS",
                    color = SleekPalette.softPeach,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        val totalSessions = connectionLogs.size
                        Text(
                            text = "$totalSessions Connections",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Successfully logged in Room database",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { viewModel.clearHistory() }
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                            .testTag("clear_logs_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Clear logs history database",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "PURGE ALL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                // Aggregate numeric highlights (peak speed, average time)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "PEAK SECURE SPEED",
                            fontSize = 8.sp,
                            color = SleekPalette.softPeach,
                            fontFamily = FontFamily.Monospace
                        )
                        val maxSpeed = connectionLogs.maxOfOrNull { it.peakSpeedMbps } ?: 0.0
                        Text(
                            text = String.format("%.1f Mbps", maxSpeed),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "TOTAL HOURS SECURED",
                            fontSize = 8.sp,
                            color = SleekPalette.softPeach,
                            fontFamily = FontFamily.Monospace
                        )
                        val totalSecs = connectionLogs.sumOf { it.durationSeconds }
                        val formattedHours = String.format("%.2f Hrs", totalSecs / 3600.0)
                        Text(
                            text = formattedHours,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Text(
            text = "PERSISTENT SESSION LOGS LEDGER",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = SleekPalette.textSecondary
        )

        // Session Ledger List Flow
        if (connectionLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SleekPalette.borderLight)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty Database Logs",
                        tint = SleekPalette.subtleText,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "HISTORICAL LEDGER EMPTY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPalette.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Connect lines and disconnect to log database records",
                        fontSize = 10.sp,
                        color = SleekPalette.textSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SleekPalette.borderLight)
                    .background(Color.White)
            ) {
                items(connectionLogs) { record ->
                    val serverFlag = when {
                        record.countryCode == "IN" -> "🇮🇳"
                        record.countryCode == "US" -> "🇺🇸"
                        record.countryCode == "UK" -> "🇬🇧"
                        else -> "🌐"
                    }

                    val dateFormatted = remember(record.timestamp) {
                        try {
                            val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            formatter.format(Date(record.timestamp))
                        } catch (e: Exception) {
                            "--"
                        }
                    }

                    val durationFormatted = remember(record.durationSeconds) {
                        val mins = record.durationSeconds / 60
                        val secs = record.durationSeconds % 60
                        String.format("%02d:%02d", mins, secs)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Country flag bubble
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SleekPalette.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = serverFlag, fontSize = 21.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = record.serverName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekPalette.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = record.protocol.substringBefore(" ("),
                                    fontSize = 8.sp,
                                    color = SleekPalette.orangeClay,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(SleekPalette.containerBg)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "IP: ${record.ipAddress} • $dateFormatted",
                                fontSize = 10.sp,
                                color = SleekPalette.textSecondary
                            )
                        }

                        // Statistics duration
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = durationFormatted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPalette.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format("%.1f Mbps", record.peakSpeedMbps),
                                fontSize = 9.sp,
                                color = SleekPalette.textSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Divider(color = SleekPalette.background)
                }
            }
        }
    }
}
