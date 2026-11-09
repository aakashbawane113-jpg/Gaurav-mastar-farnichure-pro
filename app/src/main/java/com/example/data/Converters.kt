package com.example.data

import androidx.room.TypeConverter
import com.example.model.CuttingPart
import com.example.model.Estimation
import com.example.model.FurnitureConfig
import com.example.model.MaterialRates
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromConfig(config: FurnitureConfig?): String {
        return if (config == null) "" else moshi.adapter(FurnitureConfig::class.java).toJson(config)
    }

    @TypeConverter
    fun toConfig(json: String?): FurnitureConfig {
        if (json.isNullOrEmpty()) return FurnitureConfig()
        return try {
            moshi.adapter(FurnitureConfig::class.java).fromJson(json) ?: FurnitureConfig()
        } catch (e: Exception) {
            FurnitureConfig()
        }
    }

    @TypeConverter
    fun fromCuttingParts(parts: List<CuttingPart>?): String {
        if (parts == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, CuttingPart::class.java)
        return moshi.adapter<List<CuttingPart>>(type).toJson(parts)
    }

    @TypeConverter
    fun toCuttingParts(json: String?): List<CuttingPart> {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, CuttingPart::class.java)
        return try {
            moshi.adapter<List<CuttingPart>>(type).fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromRates(rates: MaterialRates?): String {
        return if (rates == null) "" else moshi.adapter(MaterialRates::class.java).toJson(rates)
    }

    @TypeConverter
    fun toRates(json: String?): MaterialRates {
        if (json.isNullOrEmpty()) return MaterialRates()
        return try {
            moshi.adapter(MaterialRates::class.java).fromJson(json) ?: MaterialRates()
        } catch (e: Exception) {
            MaterialRates()
        }
    }

    @TypeConverter
    fun fromEstimation(est: Estimation?): String {
        return if (est == null) "" else moshi.adapter(Estimation::class.java).toJson(est)
    }

    @TypeConverter
    fun toEstimation(json: String?): Estimation {
        if (json.isNullOrEmpty()) return Estimation()
        return try {
            moshi.adapter(Estimation::class.java).fromJson(json) ?: Estimation()
        } catch (e: Exception) {
            Estimation()
        }
    }
}
