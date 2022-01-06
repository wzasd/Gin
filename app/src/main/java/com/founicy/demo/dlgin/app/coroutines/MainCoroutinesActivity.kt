package com.founicy.demo.dlgin.app.coroutines

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.founicy.demo.dlgin.app.R
import com.founicy.demo.dlgin.app.adapter.PosterAdapter
import com.founicy.demo.dlgin.app.databinding.ActivityMainCoroutinesBinding


class MainCoroutinesActivity : AppCompatActivity() {

  private val viewModelFactory: MainCoroutinesViewModelFactory = MainCoroutinesViewModelFactory()
  private val viewModel: MainCoroutinesViewModel by lazy {
    ViewModelProvider(this, viewModelFactory).get(MainCoroutinesViewModel::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    DataBindingUtil.setContentView<ActivityMainCoroutinesBinding>(this, R.layout.activity_main_coroutines).apply {
      lifecycleOwner = this@MainCoroutinesActivity
      viewModel = this@MainCoroutinesActivity.viewModel
      adapter = PosterAdapter()
    }

    viewModel.toastLiveData.observe(
      this,
      {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
      }
    )
  }
}
