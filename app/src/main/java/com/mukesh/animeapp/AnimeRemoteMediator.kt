package com.mukesh.animeapp

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mukesh.animeapp.data.api.JikanApiService
import com.mukesh.animeapp.data.db.AnimeDatabase
import com.mukesh.animeapp.data.db.entity.AnimeEntity
import com.mukesh.animeapp.data.db.entity.RemoteKeys
import com.mukesh.animeapp.util.NetworkUtil
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class AnimeRemoteMediator(
    private val api: JikanApiService,
    private val db: AnimeDatabase,
    private val context: Context
) : RemoteMediator<Int, AnimeEntity>() {

    private val QUERY_KEY = "top_anime"

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AnimeEntity>
    ): MediatorResult {

        if (!NetworkUtil.isOnline(context)) {
            return MediatorResult.Success(endOfPaginationReached = false)
        }

        val page = when (loadType) {
            LoadType.REFRESH -> 1

            LoadType.PREPEND ->
                return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                val key = db.remoteKeysDao().remoteKeyByQuery(QUERY_KEY)
                    ?: return MediatorResult.Success(endOfPaginationReached = false)
                key.nextPage!!
            }
        }

        return try {
            val response = api.getTopAnime(
                page = page,
                limit = state.config.pageSize
            )

            val startIndex =
                (db.animeDao().maxPageIndex() ?: -1) + 1

            val entities = response.data.mapIndexed { index, it ->
                AnimeEntity(
                    id = it.mal_id,
                    title = it.title,
                    episodes = it.episodes,
                    rating = it.score,
                    images = it.images?.jpg?.imageUrl,
                    pageIndex = startIndex + index
                )
            }

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.remoteKeysDao().clearRemoteKeys(QUERY_KEY)
                    db.animeDao().clearAll()
                }

                db.animeDao().insertAll(entities)

                db.remoteKeysDao().insertOrReplace(
                    RemoteKeys(
                        label = QUERY_KEY,
                        nextPage = page + 1
                    )
                )
            }

            val endReached =
                entities.size < state.config.pageSize

            MediatorResult.Success(
                endOfPaginationReached = endReached
            )

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}
