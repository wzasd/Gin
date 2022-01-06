package com.founicy.demo.dlgin.app.adapter

import android.view.View
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.founicy.demo.dlgin.app.databinding.ItemPosterBinding
import com.skydoves.baserecyclerviewadapter.BaseViewHolder
import com.founicy.demo.dlgin.app.model.Poster

class PosterViewHolder(view: View) : BaseViewHolder(view) {

  private lateinit var data: Poster
  private val binding: ItemPosterBinding by bindings(view)

  override fun bindData(data: Any) {
    if (data is Poster) {
      this.data = data
      drawItemUI()
    }
  }

  private fun drawItemUI() {
    binding.apply {
      ViewCompat.setTransitionName(binding.itemContainer, data.name)
      poster = data
      executePendingBindings()
    }
  }

  override fun onClick(p0: View?) = Unit

  override fun onLongClick(p0: View?) = false
}

inline fun <reified T : ViewDataBinding> bindings(view: View): Lazy<T> =
  lazy {
    requireNotNull(DataBindingUtil.bind<T>(view)) { "cannot find the matched view to layout." }
  }
