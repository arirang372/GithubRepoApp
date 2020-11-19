package com.john.githubrepoapp.data

import com.john.githubrepoapp.data.remote.GithubService
import com.john.githubrepoapp.data.remote.GithubService.Companion.IN_QUALIFIER
import com.john.githubrepoapp.model.Repo
import com.john.githubrepoapp.model.RepoSearchResult
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import retrofit2.HttpException
import java.io.IOException


class GithubRepository(private val service: GithubService) {
    private val inMemoryCache = mutableListOf<Repo>()
    private val searchResults = ConflatedBroadcastChannel<RepoSearchResult>()

    // keep the last requested page. When the request is successful, increment the page number.
    private var lastRequestedPage = GITHUB_STARTING_PAGE_INDEX
    private var isRequestInProgress = false

    suspend fun getSearchResultStream(query: String): Flow<RepoSearchResult> {
        lastRequestedPage = 1
        inMemoryCache.clear()
        requestAndSaveData(query)
        return searchResults.asFlow()
    }

    suspend fun requestMore(query: String){
        if(isRequestInProgress)
            return
        val successful = requestAndSaveData(query)
        if(successful)
            lastRequestedPage++
    }

    private suspend fun requestAndSaveData(query: String): Boolean {
        isRequestInProgress = true
        var successful = false
        val apiQuery = query + IN_QUALIFIER
        try {
            val response = service.searchRepos(apiQuery, lastRequestedPage, NETWORK_PAGE_SIZE)
            val repos = response.items ?: emptyList()
            inMemoryCache.addAll(repos)
            val filtered = filterRepo(query)
            searchResults.offer(RepoSearchResult.Success(filtered))
            successful = true
        } catch (exception: IOException) {
            searchResults.offer(RepoSearchResult.Error(exception))
        } catch (exception: HttpException) {
            searchResults.offer(RepoSearchResult.Error(exception))
        }
        isRequestInProgress = false
        return successful
    }

    private fun filterRepo(query: String): List<Repo> {
        return inMemoryCache.filter {
            it.name.contains(query, true) ||
                    (it.description?.contains(query, true) ?: false)
        }.sortedWith(compareByDescending<Repo> { it.stars }.thenBy { it.name })
    }


    companion object {
        private const val GITHUB_STARTING_PAGE_INDEX = 1
        private const val NETWORK_PAGE_SIZE = 50
    }
}