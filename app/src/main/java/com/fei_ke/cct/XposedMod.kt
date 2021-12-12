package com.fei_ke.cct

import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookConstructor
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage


class XposedMod : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        Constants.PACKAGE_CONFIG.forEach { config ->
            if (lpparam.packageName == config.packageName && lpparam.isFirstApplication
                && lpparam.processName == config.packageName
            ) {
                hookPackage(lpparam, config)
            }
        }

        if (lpparam.packageName == Constants.CHROME_PACKAGE_NAME && lpparam.isFirstApplication
            && lpparam.processName == Constants.CHROME_PACKAGE_NAME
        ) {
            hookChrome(lpparam)
        }
    }

    private fun hookPackage(lpparam: XC_LoadPackage.LoadPackageParam, config: Constants.PackageConfig) {

        findAndHookMethod(Application::class.java, "onCreate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                CCTHelper.init(param.thisObject as Context, config)
            }
        })
        findAndHookConstructor(Instrumentation::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                hookStartActivity(param.thisObject as Instrumentation, config)
            }
        })
    }

    private fun hookStartActivity(instrumentation: Instrumentation, config: Constants.PackageConfig) {
        instrumentation.addMonitor(object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
                return interceptIntent(intent, config)
            }
        })
    }

    private fun interceptIntent(intent: Intent, config: Constants.PackageConfig): Instrumentation.ActivityResult? {
        try {
            if (config.webViewUiSet.contains(intent.component?.className)
                && !intent.getBooleanExtra(Constants.KEY_IGNORE_CCT, false)
            ) {

                val context = AndroidAppHelper.currentApplication()

                val url = intent.getStringExtra(config.keyRawUrl)

                val result =
                    context.contentResolver.call(Uri.parse(Constants.CCT_PROVIDER), Constants.METHOD_USE_CCT, url, null)
                        ?: return null

                if (result.getBoolean(Constants.KEY_USE_CCT)) {
                    CCTHelper.open(context, intent)
                    return Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
                }
            }
        } catch (t: Throwable) {
            XposedBridge.log(t)
        }
        return null
    }

    private fun hookChrome(lpparam: XC_LoadPackage.LoadPackageParam) {
        findAndHookMethod(Constants.CHROME_CUSTOM_TAB_ACTIVITY, lpparam.classLoader,
            "onOptionsItemSelected", MenuItem::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    //fixme 需要一个通用的判断
                    //close chrome custom tab when select menu item 'Open in Wechat / 返回微信打开'
                    val menuItem = param.args[0] as MenuItem
                    val thisActivity = param.thisObject as Activity
                    val thatContext =
                        thisActivity.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)
                    val text = thatContext.getString(R.string.open_in_origin_app)
                    if (menuItem.title == text) {
                        thisActivity.finish()
                    }
                }
            })
    }
}
