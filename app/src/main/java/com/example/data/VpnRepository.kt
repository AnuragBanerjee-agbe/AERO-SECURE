package com.example.data

import kotlinx.coroutines.flow.Flow

class VpnRepository(private val vpnRecordDao: VpnRecordDao) {
    val allRecords: Flow<List<VpnRecord>> = vpnRecordDao.getAllRecords()

    suspend fun insertRecord(record: VpnRecord) {
        vpnRecordDao.insertRecord(record)
    }

    suspend fun clearAllRecords() {
        vpnRecordDao.clearAllRecords()
    }
}
