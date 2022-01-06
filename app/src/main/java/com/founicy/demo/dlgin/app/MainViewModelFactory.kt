package com.founicy.demo.dlgin.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.founicy.demo.dlgin.app.network.NetworkModule

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(NetworkModule.disneyService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class.")
    }
}
