package com.john.githubrepoapp

import androidx.lifecycle.ViewModelProvider
import com.john.githubrepoapp.data.GithubRepository
import com.john.githubrepoapp.data.remote.GithubService
import com.john.githubrepoapp.viewmodel.ViewModelFactory


object Injection {

    private fun provideGithubRepository(): GithubRepository {
        return GithubRepository(GithubService.create())
    }

    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return ViewModelFactory(provideGithubRepository())
    }
}