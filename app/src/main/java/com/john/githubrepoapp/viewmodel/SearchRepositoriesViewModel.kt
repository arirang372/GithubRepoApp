package com.john.githubrepoapp.viewmodel

import androidx.lifecycle.*
import com.john.githubrepoapp.data.GithubRepository
import com.john.githubrepoapp.model.RepoSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchRepositoriesViewModel(private val repository: GithubRepository) : ViewModel() {
    private val queryLiveData = MutableLiveData<String>()
    val repoResult: LiveData<RepoSearchResult> = queryLiveData.switchMap { queryString ->
        liveData {
            val repos = repository.getSearchResultStream(queryString)
                .asLiveData(Dispatchers.Main)
            emitSource(repos)
        }
    }

    fun searchRepo(queryString: String) {
        queryLiveData.postValue(queryString)
    }

    fun listScrolled(visibleItemCount: Int, lastVisibleItemPosition: Int, totalItemCount: Int) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
            queryLiveData.value?.let {
                viewModelScope.launch {
                    repository.requestMore(it)
                }
            }
        }
    }

    companion object {
        private const val VISIBLE_THRESHOLD = 5
    }
}