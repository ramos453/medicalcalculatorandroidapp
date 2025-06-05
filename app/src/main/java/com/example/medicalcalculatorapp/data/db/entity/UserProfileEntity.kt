package com.example.medicalcalculatorapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val fullName: String? = null,
    val profession: String? = null,
    val specialization: String? = null,
    val institution: String? = null,
    val licenseNumber: String? = null,
    val country: String? = null,
    val language: String = "es",
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)