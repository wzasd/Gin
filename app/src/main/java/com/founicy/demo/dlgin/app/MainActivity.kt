package com.founicy.demo.dlgin.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.founicy.demo.dlgin.app.adapter.PosterAdapter
import com.founicy.demo.dlgin.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModelFactory: MainViewModelFactory = MainViewModelFactory()
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            lifecycleOwner = this@MainActivity
            viewModel = this@MainActivity.viewModel
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
