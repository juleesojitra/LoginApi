package com.app.loginapi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.viewpager.widget.ViewPager

import com.app.loginapi.databinding.ActivityQuickBinding
import com.app.loginapi.utils.Const
import com.google.gson.Gson

class QuickActivity : AppCompatActivity() {

    val b : ActivityQuickBinding by lazy { ActivityQuickBinding.inflate(layoutInflater) }
    lateinit var viewPagerAdapter: ViewPagerAdapter
    var activity: Activity = this
    var pos = 0
    var imgList = intArrayOf(R.drawable.vector_1, R.drawable.vector_1, R.drawable.vector_1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        initView()
    }

    private fun initView() {
        viewPagerAdapter = ViewPagerAdapter(activity, imgList)
        b.viewPager.adapter = viewPagerAdapter
        b.dotIndicator.setViewPager(b.viewPager)

        setAdapter()
        clickEvent()
    }
    private fun clickEvent() {
        b.nextImg.setOnClickListener {
            if (pos == 0 || pos == 1) {
                b.viewPager.currentItem = pos + 1
            } else {
                startActivity(Intent(activity, MainActivity::class.java))
                finish()
            }
        }
    }
    private fun setAdapter() {
        viewPagerAdapter = ViewPagerAdapter(activity, imgList)
        b.viewPager.adapter = viewPagerAdapter

        b.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}
            @RequiresApi(Build.VERSION_CODES.O)
            @SuppressLint("RestrictedApi", "ResourceAsColor")
            override fun onPageSelected(position: Int) {
                pos = position
                when (position) {
                    0 -> {
                        b.lblTitle.text = "Lorem ipsum dolor sit amet consectetur."
                        b.lblSubTitle.text = "Lorem ipsum dolor sit amet consectetur."
                        b.nextImg.setImageResource(R.drawable.btn_next_top)
                    }
                    1 -> {
                        b.lblTitle.text = "Lorem ipsum dolor sit amet consectetur."
                        b.lblSubTitle.text = "Lorem ipsum dolor sit amet consectetur."
                        b.nextImg.setImageResource(R.drawable.btn_next_top)
                    }
                    2 -> {
                        b.lblTitle.text = "Lorem ipsum dolor sit amet consectetur."
                        b.lblSubTitle.text = "Lorem ipsum dolor sit amet consectetur."
                        b.nextImg.setImageResource(R.drawable.btn_next_top)
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }
    override fun onBackPressed() {
        if (pos == 1 || pos == 2) {
            b.viewPager.currentItem = pos - 1
        } else if (pos == 0) {
            finishAffinity()
        }
    }




}