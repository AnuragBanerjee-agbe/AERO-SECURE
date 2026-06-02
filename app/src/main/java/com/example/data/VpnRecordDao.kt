package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnRecordDao {
    @Query("SELECT * FROM vpn_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<VpnRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: VpnRecord)

    @Query("DELETE FROM vpn_records")
    suspend fun clearAllRecords()
}
