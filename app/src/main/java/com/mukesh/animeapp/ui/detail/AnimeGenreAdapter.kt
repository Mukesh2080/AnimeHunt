package com.mukesh.animeapp.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mukesh.animeapp.R
import com.mukesh.animeapp.data.model.Genre


class AnimeGenreAdapter : RecyclerView.Adapter<AnimeGenreAdapter.GenreViewHolder>() {

    private val items = mutableListOf<Genre>()

    fun submitList(data: List<Genre>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val genreName: TextView = itemView.findViewById(R.id.genreName)

        fun bind(genre: Genre) {
            genreName.text = genre.name
        }
    }
}