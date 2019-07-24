package com.fei_ke.cct

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
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
                && lpparam.processName == Constants.CHROME_PACKAGE_NAME) {
            hookChrome(lpparam)
        }
    }

    private fun hookPackage(lpparam: XC_LoadPackage.LoadPackageParam, config: Constants.PackageConfig) {

        findAndHookMethod(Application::class.java, "onCreate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                CCTHelper.init(param.thisObject as Context, config)
            }
        })

        findAndHookMethod(Activity::class.java, "startActivityForResult", Intent::class.java, Int::class.java, Bundle::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val intent = param.args[0] as Intent
                            if (config.webViewUiSet.contains(intent.component?.className)
                                    && !intent.getBooleanExtra(Constants.KEY_IGNORE_CCT, false)) {

                                val activity = param.thisObject as Activity

                                val url = intent.getStringExtra(config.keyRawUrl)

                                val result = activity.contentResolver.call(Uri.parse(Constants.CCT_PROVIDER), Constants.METHOD_USE_CCT, url, null)
                                    ?:return

                                if (result.getBoolean(Constants.KEY_USE_CCT)) {
                                    CCTHelper.open(activity, intent)

                                    //return
                                    param.result = null
                                }
                            }
                        } catch (t: Throwable) {
                            XposedBridge.log(t)
                        }

                    }
                })
    }

    private fun hookChrome(lpparam: XC_LoadPackage.LoadPackageParam) {
        findAndHookMethod(Constants.CHROME_CUSTOM_TAB_ACTIVITY, lpparam.classLoader,
                "onOptionsItemSelected", MenuItem::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                //fixme 需要一个通用的判断
                //close chrome custom tab when select menu item 'Open in Wechat / 返回微信打开'
                val menuItem = param.args[0] as MenuItem
                val thisActivity = param.thisObject as Activity
                val thatContext = thisActivity.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)
                val text = thatContext.getString(R.string.open_in_origin_app)
                if (menuItem.title == text) {
                    thisActivity.finish()
                }
            }
        })
    }
}
