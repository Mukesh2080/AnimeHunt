package com.mukesh.animeapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mukesh.animeapp.data.db.dao.AnimeDao
import com.mukesh.animeapp.data.db.dao.AnimeDetailDao
import com.mukesh.animeapp.data.db.dao.RemoteKeysDao
import com.mukesh.animeapp.data.db.entity.AnimeDetailEntity
import com.mukesh.animeapp.data.db.entity.AnimeEntity
import com.mukesh.animeapp.data.db.entity.RemoteKeys

@Database(
    entities = [
        AnimeEntity::class,
        AnimeDetailEntity::class,
        RemoteKeys::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AnimeDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao
    abstract fun animeDetailDao(): AnimeDetailDao
    abstract fun remoteKeysDao(): RemoteKeysDao



    companion object {
        fun create(context: Context): AnimeDatabase =
            Room.databaseBuilder(
                context,
                AnimeDatabase::class.java,
                "anime_db"
            ).build()
    }
}
