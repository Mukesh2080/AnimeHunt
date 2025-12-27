package com.mukesh.animeapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mukesh.animeapp.data.db.entity.AnimeEntity

@Dao

interface AnimeDao {

    @Query("""
    SELECT * FROM anime
    ORDER BY pageIndex ASC
""")
    fun pagingSource(): PagingSource<Int, AnimeEntity>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(anime: List<AnimeEntity>)

    @Query("DELETE FROM anime")
    suspend fun clearAll()

    @Query("SELECT MAX(pageIndex) FROM anime")
    suspend fun maxPageIndex(): Int?
}
