package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccine_records")
data class VaccineRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val childId: Int,                  // Links to ChildEntity.id
    val vaccineName: String,
    val diseasePrevented: String,
    val recommendedAgeInMonths: Int,
    val doseNumber: Int,
    val scheduledDate: Long,           // Expected date (millis UTC)
    val actualDate: Long? = null,      // Actual injection date (millis UTC)
    val isCompleted: Boolean = false,
    val location: String = "",
    val notes: String = ""
)
