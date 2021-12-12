package com.fei_ke.cct

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import java.util.concurrent.Executors

@SuppressLint("StaticFieldLeak")
object CCTHelper {
    private const val TAG = "CCTHelper"

    private const val CUSTOM_TAB_PACKAGE_NAME = Constants.CHROME_PACKAGE_NAME

    private var isBindingCustomTabsService: Boolean = false

    private var mCustomTabsSession: CustomTabsSession? = null

    private lateinit var applicationContext: Context
    private lateinit var packageConfig: Constants.PackageConfig

    private val threadPool = Executors.newCachedThreadPool()

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

    fun init(context: Context, packageConfig: Constants.PackageConfig) {
        this.applicationContext = context.applicationContext
        this.packageConfig = packageConfig
        tryConnectCCTService()
    }

    @Synchronized
    private fun tryConnectCCTService() {
        if (mCustomTabsSession != null) return

        if (isBindingCustomTabsService) return

        isBindingCustomTabsService = true

        val result = CustomTabsClient.bindCustomTabsService(applicationContext, CUSTOM_TAB_PACKAGE_NAME, serviceConnection)
        if (!result) {
            isBindingCustomTabsService = false
        }
        Log.i(TAG, "bindCustomTabsService: $result")
    }

    fun mayLaunchUrl(url: String) {
        threadPool.execute {
            var success = false
            if (mCustomTabsSession != null) {

                val useCCT = try {
                    val result = applicationContext.contentResolver.call(Uri.parse(Constants.CCT_PROVIDER), Constants.METHOD_USE_CCT, url, null)
                    result.getBoolean(Constants.KEY_USE_CCT)
                } catch (t: Exception) {
                    false
                }

                if (useCCT) {
                    success = mCustomTabsSession?.mayLaunchUrl(Uri.parse(url), null, null) ?: false
                }
            } else {
                tryConnectCCTService()
            }
            Log.i(TAG, "mayLaunchUrl success: $success, url: $url")
        }
    }

    fun open(activity: Context, origin: Intent) {
        if (mCustomTabsSession == null) {
            tryConnectCCTService()
        }
        val url = origin.getStringExtra(packageConfig.keyRawUrl)

        //remove Parcelable type extra
        val intent = Intent(origin)
        val extras = intent.extras!!
        extras.keySet().forEach {
            if (extras.get(it) is Parcelable) {
                intent.removeExtra(it)
            }
        }

        intent.putExtra(Constants.KEY_IGNORE_CCT, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val openInOriginAppPendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val cctContext = activity.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)

        val ruleIntent = Intent(Constants.ACTION_ADD_RULE)
        ruleIntent.setClassName(BuildConfig.APPLICATION_ID, EditRuleActivity::class.java.name)
        ruleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        ruleIntent.putExtra(Constants.KEY_RAW_URL, url)

        val rulePendingIntent = PendingIntent.getActivity(activity, 0, ruleIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val packageManager = applicationContext.packageManager
        val originAppName = try {
            packageManager.getApplicationInfo(packageConfig.packageName, 0)
                .run(packageManager::getApplicationLabel)
        } catch (t: Throwable) {
            null
        }

        val builder = CustomTabsIntent.Builder(mCustomTabsSession)
                .setShowTitle(true)
                .addMenuItem(cctContext.getString(R.string.open_in_origin_app, originAppName), openInOriginAppPendingIntent)
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
