/*
 * Designed and developed by 2020 wzasd (Jeffrey wang)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
