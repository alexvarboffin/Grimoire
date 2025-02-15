package data.local

import androidx.room.TypeConverter
import domain.model.TextReplacement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromReplacementList(value: List<TextReplacement>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toReplacementList(value: String): List<TextReplacement> {
        return json.decodeFromString(value)
    }
} 