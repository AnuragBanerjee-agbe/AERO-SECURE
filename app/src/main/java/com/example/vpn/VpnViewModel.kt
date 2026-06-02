package com.example.vpn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.VpnDatabase
import com.example.data.VpnRecord
import com.example.data.VpnRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

enum class VpnProtocol(val displayName: String) {
    WIREGUARD("WireGuard (UDP)"),
    OPENVPN("OpenVPN (TCP)"),
    IPSEC("IPSec (IKEv2)")
}

data class VpnServer(
    val id: String,
    val countryName: String,
    val cityName: String,
    val countryCode: String, // US, UK, IN
    val flagEmoji: String,
    val basePingMs: Int,
    val loadPercentage: Int,
    val ipPrefix: String
)

class VpnViewModel(
    application: Application,
    private val repository: VpnRepository
) : AndroidViewModel(application) {

    // USA, UK, and INDIA servers
    val servers = listOf(
        VpnServer("in_mum", "India", "Mumbai Center", "IN", "🇮🇳", 12, 45, "139.59.32."),
        VpnServer("in_del", "India", "New Delhi Node", "IN", "🇮🇳", 22, 68, "45.115.24."),
        VpnServer("us_ny", "United States", "New York Peak", "US", "🇺🇸", 78, 62, "138.197.10."),
        VpnServer("us_ca", "United States", "Silicon Valley", "US", "🇺🇸", 92, 38, "104.248.80."),
        VpnServer("uk_ldn", "United Kingdom", "London Hub", "UK", "🇬🇧", 42, 51, "178.62.15."),
        VpnServer("uk_man", "United Kingdom", "Manchester Safe", "UK", "🇬🇧", 48, 29, "182.49.20.")
    )

    private val _selectedServer = MutableStateFlow(servers[0]) // Default to India Mumbai
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _activeServer = MutableStateFlow<VpnServer?>(null)
    val activeServer: StateFlow<VpnServer?> = _activeServer.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _selectedProtocol = MutableStateFlow(VpnProtocol.WIREGUARD)
    val selectedProtocol: StateFlow<VpnProtocol> = _selectedProtocol.asStateFlow()

    // Real-time speed metrics
    private val _downloadSpeed = MutableStateFlow(0.0)
    val downloadSpeed: StateFlow<Double> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0.0)
    val uploadSpeed: StateFlow<Double> = _uploadSpeed.asStateFlow()

    private val _ping = MutableStateFlow(0)
    val ping: StateFlow<Int> = _ping.asStateFlow()

    private val _activeIpAddress = MutableStateFlow("192.168.1.135") // Local IP
    val activeIpAddress: StateFlow<String> = _activeIpAddress.asStateFlow()

    // Timer tracking
    private val _connectionDuration = MutableStateFlow(0L) // seconds
    val connectionDuration: StateFlow<Long> = _connectionDuration.asStateFlow()

    // Speed test simulation status
    private val _isSpeedTesting = MutableStateFlow(false)
    val isSpeedTesting: StateFlow<Boolean> = _isSpeedTesting.asStateFlow()

    private val _speedTestValue = MutableStateFlow(0.0)
    val speedTestValue: StateFlow<Double> = _speedTestValue.asStateFlow()

    // History logs
    private val _connectionLogs = MutableStateFlow<List<VpnRecord>>(emptyList())
    val connectionLogs: StateFlow<List<VpnRecord>> = _connectionLogs.asStateFlow()

    private var activeTimerJob: Job? = null
    private var metricSimulationJob: Job? = null

    // Record stats
    private var peakSimulatedSpeed = 0.0

    init {
        // Collect history logs
        viewModelScope.launch {
            repository.allRecords.collectLatest {
                _connectionLogs.value = it
            }
        }
    }

    fun selectServer(server: VpnServer) {
        if (_connectionStatus.value == ConnectionStatus.DISCONNECTED) {
            _selectedServer.value = server
        }
    }

    fun selectProtocol(protocol: VpnProtocol) {
        if (_connectionStatus.value == ConnectionStatus.DISCONNECTED) {
            _selectedProtocol.value = protocol
        }
    }

    fun toggleConnection() {
        when (_connectionStatus.value) {
            ConnectionStatus.DISCONNECTED -> connect()
            ConnectionStatus.CONNECTED -> disconnect()
            else -> {} // Transitioning block
        }
    }

    private fun connect() {
        viewModelScope.launch {
            val server = _selectedServer.value
            _connectionStatus.value = ConnectionStatus.CONNECTING
            _activeServer.value = server

            // Connection delay handshake
            delay(1600)

            _connectionStatus.value = ConnectionStatus.CONNECTED
            _activeIpAddress.value = server.ipPrefix + Random.nextInt(2, 254)
            _connectionDuration.value = 0L
            peakSimulatedSpeed = 0.0

            startSessionTimer()
            startMetricFluctuation()
        }
    }

    private fun disconnect() {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.DISCONNECTING
            stopSessionTimer()
            stopMetricFluctuation()

            delay(1000)

            val connectedSrv = _activeServer.value
            if (connectedSrv != null && _connectionDuration.value > 0L) {
                val record = VpnRecord(
                    serverName = "${connectedSrv.cityName}, ${connectedSrv.countryName}",
                    countryCode = connectedSrv.countryCode,
                    ipAddress = _activeIpAddress.value,
                    protocol = _selectedProtocol.value.displayName,
                    durationSeconds = _connectionDuration.value,
                    peakSpeedMbps = if (peakSimulatedSpeed == 0.0) Random.nextDouble(35.0, 95.0) else peakSimulatedSpeed
                )
                repository.insertRecord(record)
            }

            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            _activeServer.value = null
            _activeIpAddress.value = "192.168.1.135"
        }
    }

    fun runSpeedTest() {
        if (_connectionStatus.value != ConnectionStatus.CONNECTED || _isSpeedTesting.value) return

        viewModelScope.launch {
            _isSpeedTesting.value = true
            _speedTestValue.value = 0.0

            val targetSpeed = when (_activeServer.value?.countryCode) {
                "IN" -> Random.nextDouble(140.0, 210.0)
                "US" -> Random.nextDouble(90.0, 160.0)
                "UK" -> Random.nextDouble(110.0, 185.0)
                else -> Random.nextDouble(60.0, 120.0)
            }

            val steps = 25
            for (i in 1..steps) {
                delay(120)
                val progression = i.toDouble() / steps.toDouble()
                _speedTestValue.value = (targetSpeed * progression) + Random.nextDouble(-8.0, 8.0)
            }

            delay(1200)
            _isSpeedTesting.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }

    private fun startSessionTimer() {
        activeTimerJob?.cancel()
        activeTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _connectionDuration.value += 1
            }
        }
    }

    private fun stopSessionTimer() {
        activeTimerJob?.cancel()
        activeTimerJob = null
    }

    private fun startMetricFluctuation() {
        metricSimulationJob?.cancel()
        metricSimulationJob = viewModelScope.launch {
            val server = _activeServer.value ?: return@launch
            while (true) {
                val baseDown = when (server.countryCode) {
                    "IN" -> 78.0
                    "US" -> 64.0
                    "UK" -> 72.0
                    else -> 45.0
                }

                val currentDown = baseDown + Random.nextDouble(-10.0, 15.0)
                val currentUp = (baseDown * 0.35) + Random.nextDouble(-3.0, 6.0)

                _downloadSpeed.value = String.format("%.2f", currentDown).toDouble()
                _uploadSpeed.value = String.format("%.2f", currentUp).toDouble()
                _ping.value = server.basePingMs + Random.nextInt(-2, 5)

                if (currentDown > peakSimulatedSpeed) {
                    peakSimulatedSpeed = currentDown
                }

                delay(1500)
            }
        }
    }

    private fun stopMetricFluctuation() {
        metricSimulationJob?.cancel()
        metricSimulationJob = null
        _downloadSpeed.value = 0.0
        _uploadSpeed.value = 0.0
        _ping.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        stopSessionTimer()
        stopMetricFluctuation()
    }
}

class VpnViewModelFactory(
    private val application: Application,
    private val repository: VpnRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VpnViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VpnViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
