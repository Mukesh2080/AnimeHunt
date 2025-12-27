package com.mukesh.animeapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mukesh.animeapp.data.db.entity.AnimeDetailEntity

@Dao
interface AnimeDetailDao {

    @Query("SELECT * FROM anime_details WHERE anime_id = :id LIMIT 1")
    suspend fun getAnimeDetail(id: Int): AnimeDetailEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAnimeDetail(detail: AnimeDetailEntity)
}