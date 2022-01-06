/*
 * Designed and developed by 2020 founicy(Jeffrey wang)
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

package com.founicy.demo.dlgin.app.adapter

import android.view.View
import com.founicy.demo.dlgin.app.R
import com.skydoves.baserecyclerviewadapter.BaseAdapter
import com.skydoves.baserecyclerviewadapter.SectionRow
import com.founicy.demo.dlgin.app.model.Poster

class PosterAdapter : BaseAdapter() {

  init {
    addSection(arrayListOf<Poster>())
  }

  fun addPosterList(posters: List<Poster>) {
    sections().first().run {
      clear()
      addAll(posters)
      notifyDataSetChanged()
    }
  }

  override fun layout(sectionRow: SectionRow) = R.layout.item_poster

  override fun viewHolder(layout: Int, view: View) = PosterViewHolder(view)
}