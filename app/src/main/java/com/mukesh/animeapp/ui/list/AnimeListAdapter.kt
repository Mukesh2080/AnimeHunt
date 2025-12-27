package com.mukesh.animeapp.ui.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mukesh.animeapp.R
import com.mukesh.animeapp.data.db.entity.AnimeEntity


class AnimeListAdapter(
    private val onClick: (AnimeEntity) -> Unit
) : PagingDataAdapter<AnimeEntity, AnimeListAdapter.AnimeViewHolder>(DiffCallback()) {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AnimeViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_anime, parent, false),
            onClick
        )

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class AnimeViewHolder(
        itemView: View,
        private val onClick: (AnimeEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val poster: ImageView = itemView.findViewById(R.id.poster)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val episodes: TextView = itemView.findViewById(R.id.episodes)
        private val rating: TextView = itemView.findViewById(R.id.rating)

        fun bind(item: AnimeEntity) {
            title.text = item.title
            episodes.text = "Episodes: ${item.episodes ?: "N/A"}"
            rating.text = "⭐ ${item.rating ?: "N/A"}"

            loadImageWithGlide(item.images, poster)

            itemView.setOnClickListener {
                onClick(item)
            }
        }

        private fun loadImageWithGlide(imageUrl: String?, imageView: ImageView) {
            Glide.with(imageView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .transform(
                    CenterCrop(),
                    RoundedCorners(16.dpToPx(imageView.context))
                )
                .error(R.drawable.ic_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
        fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AnimeEntity>() {
        override fun areItemsTheSame(old: AnimeEntity, new: AnimeEntity): Boolean =
            old.id == new.id

        override fun areContentsTheSame(old: AnimeEntity, new: AnimeEntity): Boolean =
            old == new
    }
}
