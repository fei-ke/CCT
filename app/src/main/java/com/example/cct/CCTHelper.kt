package com.example.cct

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsServiceConnection
import android.support.customtabs.CustomTabsSession
import android.util.Log
import de.robv.android.xposed.XposedHelpers

@SuppressLint("StaticFieldLeak")
object CCTHelper {
    private const val TAG = "CCTHelper"

    private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"

    private var isBindingCustomTabsService: Boolean = false

    private var mCustomTabsSession: CustomTabsSession? = null

    private lateinit var applicationContext: Context

    private val serviceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            mCustomTabsSession = client.newSession(null)
            isBindingCustomTabsService = false

            Log.i(TAG, "create CustomTabsSession success")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mCustomTabsSession = null

            Log.i(TAG, "onServiceDisconnected")
        }
    }

    fun init(context: Context) {
        applicationContext = context.applicationContext

        tryConnectCCTService()
    }

    @Synchronized
    private fun tryConnectCCTService() {
        if (mCustomTabsSession != null) return

        if (isBindingCustomTabsService) return

        isBindingCustomTabsService = true

        val result = CustomTabsClient.bindCustomTabsService(applicationContext, CUSTOM_TAB_PACKAGE_NAME, serviceConnection)

        Log.i(TAG, "bindCustomTabsService: $result")
    }

    fun mayLaunchUrl(url: String) {
        if (mCustomTabsSession != null) {
            mCustomTabsSession?.mayLaunchUrl(Uri.parse(url), null, null)
        } else {
            tryConnectCCTService()
        }
    }

    fun open(activity: Activity, intent: Intent) {
        if (mCustomTabsSession == null) {
            tryConnectCCTService()
        }

        val url = intent.getStringExtra(Constants.KEY_RAW_URL)

        intent.putExtra(Constants.KEY_IGNORE_CCT, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val openInWechatPendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val cctContext = activity.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)

        val statusBarColor = try {
            activity.window.statusBarColor
        } catch (t: Throwable) {
            Constants.MM_DEFAULT_STATUS_BAR_COLOR
        }

        val toolbarColor = try {
            val actionBar = XposedHelpers.callMethod(activity, "getSupportActionBar")
            val actionBarContainer = XposedHelpers.getObjectField(actionBar, "It")
            val colorDrawable = XposedHelpers.getObjectField(actionBarContainer, "KT") as ColorDrawable
            colorDrawable.color
        } catch (t: Throwable) {
            Constants.MM_DEFAULT_TOOL_BAR_COLOR
        }


        val ruleIntent = Intent(Constants.ACTION_ADD_RULE)
        ruleIntent.setClassName(BuildConfig.APPLICATION_ID, AddRuleActivity::class.java.name)
        ruleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        ruleIntent.putExtras(intent.extras)

        val rulePendingIntent = PendingIntent.getActivity(activity, 0, ruleIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = CustomTabsIntent.Builder(mCustomTabsSession)
                .setToolbarColor(toolbarColor)
                .setSecondaryToolbarColor(statusBarColor)
                .addMenuItem(cctContext.getString(R.string.open_in_wechat), openInWechatPendingIntent)
                .addMenuItem(cctContext.getString(R.string.add_rule), rulePendingIntent)
                .addDefaultShareMenuItem()
                .setStartAnimations(cctContext, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(cctContext, R.anim.slide_in_left, R.anim.slide_out_right)

        val customTabsIntent = builder.build()
        customTabsIntent.intent.setPackage(CUSTOM_TAB_PACKAGE_NAME)
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(activity, Uri.parse(url))
    }
}
