package com.example.cct

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("rawUrl", "https://www.baidu.com")

            CCTUtil.open(this, intent)
        }
    }
}
