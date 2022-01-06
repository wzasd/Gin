package com.founicy.demo.dlgin.app.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.founicy.demo.dlgin.app.network.NetworkModule

@Suppress("UNCHECKED_CAST")
class MainCoroutinesViewModelFactory : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MainCoroutinesViewModel::class.java)) {
      return MainCoroutinesViewModel(NetworkModule.disneyCoroutinesService) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class.")
  }
}
