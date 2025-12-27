package com.mukesh.animeapp.ui.list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.mukesh.animeapp.data.db.entity.AnimeEntity
import com.mukesh.animeapp.data.respository.AnimeRepository
import com.mukesh.animeapp.util.NetworkMonitor
import com.mukesh.animeapp.util.NetworkUtil
import com.mukesh.animeapp.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnimeListViewModel(
    repo: AnimeRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val animePaging =
        repo.getAnimePager().cachedIn(viewModelScope)
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
}


