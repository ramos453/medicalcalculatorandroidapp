package com.example.medicalcalculatorapp.data.db.converters

import androidx.room.TypeConverter
import com.example.medicalcalculatorapp.domain.model.FieldType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

class FieldTypeConverter {
    @TypeConverter
    fun fromFieldType(fieldType: FieldType): String {
        return fieldType.name
    }

    @TypeConverter
    fun toFieldType(fieldTypeName: String): FieldType {
        return FieldType.valueOf(fieldTypeName)
    }
}

class StringListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }
}

class MapConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): Map<String, String>? {
        if (value == null) return null
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromMap(map: Map<String, String>?): String? {
        if (map == null) return null
        return gson.toJson(map)
    }
}