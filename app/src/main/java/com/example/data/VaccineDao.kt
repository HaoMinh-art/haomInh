package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineDao {
    // --- CHILD OPERATIONS ---
    @Query("SELECT * FROM children ORDER BY id DESC")
    fun getAllChildrenFlow(): Flow<List<ChildEntity>>

    @Query("SELECT * FROM children WHERE id = :id")
    suspend fun getChildById(id: Int): ChildEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: ChildEntity): Long

    @Query("DELETE FROM children WHERE id = :id")
    suspend fun deleteChildById(id: Int)

    @Delete
    suspend fun deleteChild(child: ChildEntity)

    // --- VACCINE RECORD OPERATIONS ---
    @Query("SELECT * FROM vaccine_records WHERE childId = :childId ORDER BY scheduledDate ASC")
    fun getVaccineRecordsForChildFlow(childId: Int): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE childId = :childId ORDER BY scheduledDate ASC")
    suspend fun getVaccineRecordsForChild(childId: Int): List<VaccineRecordEntity>

    @Query("SELECT * FROM vaccine_records WHERE isCompleted = 0 ORDER BY scheduledDate ASC")
    fun getAllUpcomingVaccinesFlow(): Flow<List<VaccineRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccineRecord(record: VaccineRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccineRecords(records: List<VaccineRecordEntity>)

    @Update
    suspend fun updateVaccineRecord(record: VaccineRecordEntity)

    @Delete
    suspend fun deleteVaccineRecord(record: VaccineRecordEntity)

    @Query("DELETE FROM vaccine_records WHERE childId = :childId")
    suspend fun clearVaccineRecordsForChild(childId: Int)
}
