package com.example.cct

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        lateinit var context: App
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        context = this
    }
}