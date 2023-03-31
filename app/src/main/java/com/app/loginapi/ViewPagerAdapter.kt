package com.app.loginapi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.imageview.ShapeableImageView

class ViewPagerAdapter(var context: Context, var imgList: IntArray) : PagerAdapter() {

    var layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return imgList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView: View? = layoutInflater?.inflate(R.layout.adapter_tutorial, container, false)
        val img = itemView?.findViewById<View>(R.id.img) as ShapeableImageView

        img.setImageResource(imgList[position])
        container.addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as RelativeLayout)
    }
}