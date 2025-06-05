package com.example.medicalcalculatorapp.data.db.mapper

import com.example.medicalcalculatorapp.data.db.entity.UserProfileEntity
import com.example.medicalcalculatorapp.domain.model.UserProfile

class UserProfileMapper {

    fun mapEntityToDomain(entity: UserProfileEntity): UserProfile {
        return UserProfile(
            id = entity.id,
            email = entity.email,
            fullName = entity.fullName,
            profession = entity.profession,
            specialization = entity.specialization,
            institution = entity.institution,
            licenseNumber = entity.licenseNumber,
            country = entity.country,
            language = entity.language,
            profileImageUrl = entity.profileImageUrl,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun mapDomainToEntity(domain: UserProfile): UserProfileEntity {
        return UserProfileEntity(
            id = domain.id,
            email = domain.email,
            fullName = domain.fullName,
            profession = domain.profession,
            specialization = domain.specialization,
            institution = domain.institution,
            licenseNumber = domain.licenseNumber,
            country = domain.country,
            language = domain.language,
            profileImageUrl = domain.profileImageUrl,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}