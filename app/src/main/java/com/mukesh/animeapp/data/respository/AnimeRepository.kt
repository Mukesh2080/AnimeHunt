package com.mukesh.animeapp.data.respository

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mukesh.animeapp.AnimeRemoteMediator
import com.mukesh.animeapp.data.api.JikanApiService
import com.mukesh.animeapp.data.db.dao.AnimeDao
import com.mukesh.animeapp.data.db.AnimeDatabase
import com.mukesh.animeapp.data.db.dao.AnimeDetailDao
import com.mukesh.animeapp.data.db.entity.AnimeDetailEntity
import com.mukesh.animeapp.data.db.entity.AnimeEntity
import com.mukesh.animeapp.data.model.AnimeDetail
import com.mukesh.animeapp.util.NetworkUtil
import com.mukesh.animeapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AnimeRepository(
    private val api: JikanApiService,
    private val db: AnimeDatabase,
    private val dao: AnimeDao,
    private val daoDetail: AnimeDetailDao,
    private val context: Context
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getAnimePager(): Flow<PagingData<AnimeEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 20,
                enablePlaceholders = true,

            ),
            remoteMediator = AnimeRemoteMediator(
                api = api,
                db = db,
                context = context
            ),
            pagingSourceFactory = {
                dao.pagingSource()
            }
        ).flow
    }

    suspend fun fetchAnimeDetails(id: Int): Resource<AnimeDetail> {

        val cached = daoDetail.getAnimeDetail(id)
        if (cached != null) {
            return Resource.Success(
                AnimeDetail(
                    mal_id = cached.animeId,
                    title = cached.title,
                    synopsis = cached.synopsis,
                    episodes = cached.episodes,
                    score = cached.score,
                    genres = cached.genres,
                    trailer = cached.trailer
                )
            )
        }

        // Offline fallback
        if (!NetworkUtil.isOnline(context)) {
            return Resource.Error("No internet and no cached data")
        }

        //Network fetch + persist
        return try {
            val remote = api.getAnimeDetails(id).data

            daoDetail.insertAnimeDetail(
                AnimeDetailEntity(
                    animeId = remote.mal_id,
                    title = remote.title,
                    synopsis = remote.synopsis,
                    episodes = remote.episodes,
                    score = remote.score,
                    genres = remote.genres,
                    trailer = remote.trailer
                )
            )

            Resource.Success(remote)

        } catch (e: Exception) {
            Resource.Error("Unable to load anime details")
        }
    }
}
