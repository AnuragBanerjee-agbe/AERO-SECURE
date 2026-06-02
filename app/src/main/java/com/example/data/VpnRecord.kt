package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vpn_records")
data class VpnRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serverName: String,
    val countryCode: String, // US, UK, IN
    val ipAddress: String,
    val protocol: String,
    val durationSeconds: Long,
    val peakSpeedMbps: Double,
    val timestamp: Long = System.currentTimeMillis()
)
