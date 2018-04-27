package com.example.cct

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CCTHelper.init(applicationContext)
        button.setOnClickListener {
            CCTHelper.open(this, Intent().also { it.putExtra(Constants.KEY_RAW_URL, "http://www.baidu.com") })
        }
    }
}
