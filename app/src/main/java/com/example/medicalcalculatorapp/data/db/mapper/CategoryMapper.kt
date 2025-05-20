package com.example.medicalcalculatorapp.data.db.mapper

import com.example.medicalcalculatorapp.data.db.entity.CategoryEntity
import com.example.medicalcalculatorapp.domain.model.Category

class CategoryMapper {

    fun mapEntityToDomain(entity: CategoryEntity): Category {
        return Category(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            iconResId = entity.iconResId
        )
    }

    fun mapDomainToEntity(domain: Category): CategoryEntity {
        return CategoryEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            iconResId = domain.iconResId
        )
    }
}