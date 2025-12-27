package com.mukesh.animeapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mukesh.animeapp.data.db.entity.RemoteKeys

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM remote_keys WHERE label = :label")
    suspend fun remoteKeyByQuery(label: String): RemoteKeys?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey: RemoteKeys)

    @Query("DELETE FROM remote_keys WHERE label = :label")
    suspend fun clearRemoteKeys(label: String)
}
