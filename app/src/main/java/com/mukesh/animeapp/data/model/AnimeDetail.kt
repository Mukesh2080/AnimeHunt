package com.mukesh.animeapp.data.model

import com.google.gson.annotations.SerializedName

data class AnimeDetail(
    val mal_id: Int,
    val title: String,
    val synopsis: String?,
    val episodes: Int?,
    val score: Double?,
    val genres: List<Genre>,
    val trailer: Trailer?
)

data class Genre(val name: String)

data class Trailer (

    @SerializedName("youtube_id" ) var youtubeId : String? = null,
    @SerializedName("url"        ) var url       : String? = null,
    @SerializedName("embed_url"  ) var embedUrl  : String? = null,
    @SerializedName("images"     ) var images    : Images? = Images()

)
