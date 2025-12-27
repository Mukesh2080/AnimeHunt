package com.mukesh.animeapp.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mukesh.animeapp.data.model.Genre
import com.mukesh.animeapp.data.model.Trailer

class Converters {

    private val gson = Gson()
    @TypeConverter
    fun fromGenreList(value: List<Genre>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGenreList(value: String): List<Genre> {
        val type = object : TypeToken<List<Genre>>() {}.type
        return gson.fromJson(value, type)
    }
    @TypeConverter
    fun fromTrailer(value: Trailer?): String {
        return gson.toJson(value)
    }
    @TypeConverter
    fun toTrailer(value: String): Trailer? {
        return gson.fromJson(value, Trailer::class.java)
    }
}
