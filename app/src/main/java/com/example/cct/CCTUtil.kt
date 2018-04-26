package com.example.cct

import android.app.ActionBar
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import de.robv.android.xposed.XposedHelpers

object CCTUtil {
    fun open(activity: Activity, intent: Intent) {
        val url = intent.getStringExtra("rawUrl")

        intent.putExtra("ignoreCCT", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val openInWechat = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val selfContext = activity.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)

        var statusBarColor = Color.BLACK
        try {
            statusBarColor = activity.window.statusBarColor
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var toolbarColor = Color.BLACK
        try {
            val actionBar = XposedHelpers.callMethod(activity, "getSupportActionBar")
            val actionBarContainer = XposedHelpers.getObjectField(actionBar, "It")
            val colorDrawable = XposedHelpers.getObjectField(actionBarContainer, "KT") as ColorDrawable
            toolbarColor = colorDrawable.color
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val builder = CustomTabsIntent.Builder(XposedMod.mCustomTabsSession)
                .setToolbarColor(toolbarColor)
                .setSecondaryToolbarColor(statusBarColor)
                .addMenuItem("返回微信打开", openInWechat)
                .addDefaultShareMenuItem()
                .setStartAnimations(selfContext, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(selfContext, R.anim.slide_in_left, R.anim.slide_out_right)

        val customTabsIntent = builder.build()
        customTabsIntent.intent.setPackage(XposedMod.CUSTOM_TAB_PACKAGE_NAME)
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(activity, Uri.parse(url))
    }
}
