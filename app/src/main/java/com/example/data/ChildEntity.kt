package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dateOfBirth: Long, // Epoch millisecond
    val gender: String,    // "Nam" or "Nữ" or "Khác"
    val notes: String = ""
)
