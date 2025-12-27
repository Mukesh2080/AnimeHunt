package com.mukesh.animeapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukesh.animeapp.data.db.entity.AnimeEntity
import com.mukesh.animeapp.data.model.AnimeDetail
import com.mukesh.animeapp.data.respository.AnimeRepository
import com.mukesh.animeapp.util.NetworkMonitor
import com.mukesh.animeapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnimeDetailViewModel(
    private val repo: AnimeRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state =
        MutableStateFlow<Resource<AnimeDetail>>(Resource.Loading())
    val state: StateFlow<Resource<AnimeDetail>> = _state

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline


    fun load(id: Int) {
        viewModelScope.launch {
            _state.value = Resource.Loading()

            val result = repo.fetchAnimeDetails(id)
            _state.value = when (result) {
                is Resource.Error -> {
                    if (!networkMonitor.isOnline.value) {
                        Resource.Error(
                            "You're offline. Showing saved details if available."
                        )
                    } else {
                        result
                    }
                }
                else -> result
            }
        }
    }
}


