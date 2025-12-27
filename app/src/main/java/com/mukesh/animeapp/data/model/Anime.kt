package com.mukesh.animeapp.data.model

import com.google.gson.annotations.SerializedName

data class Anime(
    val mal_id: Int,
    val title: String,
    val episodes: Int?,
    val score: Double?,
    val images: Images?
)

data class Images (

    @SerializedName("jpg"  ) var jpg  : Jpg?  = Jpg(),
    @SerializedName("webp" ) var webp : Webp? = Webp()

)

data class Webp (

    @SerializedName("image_url"       ) var imageUrl      : String? = null,
    @SerializedName("small_image_url" ) var smallImageUrl : String? = null,
    @SerializedName("large_image_url" ) var largeImageUrl : String? = null

)
data class Jpg (

    @SerializedName("image_url"       ) var imageUrl      : String? = null,
    @SerializedName("small_image_url" ) var smallImageUrl : String? = null,
    @SerializedName("large_image_url" ) var largeImageUrl : String? = null

)
