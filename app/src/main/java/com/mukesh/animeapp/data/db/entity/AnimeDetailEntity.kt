package com.mukesh.animeapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mukesh.animeapp.data.model.Genre
import com.mukesh.animeapp.data.model.Trailer

@Entity(tableName = "anime_details")
data class AnimeDetailEntity(

    @PrimaryKey
    @ColumnInfo(name = "anime_id")
    val animeId: Int,

    val title: String,
    val synopsis: String?,
    val episodes: Int?,
    val score: Double?,
    val genres: List<Genre>,
    val trailer: Trailer?
)