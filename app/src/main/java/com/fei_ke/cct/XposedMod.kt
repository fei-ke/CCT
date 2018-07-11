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
        if (lpparam.packageName == Constants.MM_PACKAGE_NAME && lpparam.isFirstApplication
                && lpparam.processName == Constants.MM_PACKAGE_NAME) {
            hookMM(lpparam)
        } else if (lpparam.packageName == Constants.CHROME_PACKAGE_NAME && lpparam.isFirstApplication
                && lpparam.processName == Constants.CHROME_PACKAGE_NAME) {
            hookChrome(lpparam)
        }
    }

    private fun hookMM(lpparam: XC_LoadPackage.LoadPackageParam) {

        findAndHookMethod(Application::class.java, "onCreate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                CCTHelper.init(param.thisObject as Context)
            }
        })

        findAndHookMethod(Activity::class.java, "startActivityForResult", Intent::class.java, Int::class.java, Bundle::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val intent = param.args[0] as Intent
                            if (intent.component?.className == Constants.MM_WEB_VIEW_UI
                                    && !intent.getBooleanExtra(Constants.KEY_IGNORE_CCT, false)) {

                                val activity = param.thisObject as Activity

                                val url = intent.getStringExtra(Constants.KEY_RAW_URL)

                                val result = activity.contentResolver.call(Uri.parse(Constants.CCT_PROVIDER), Constants.METHOD_USE_CCT, url, null)
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

//        findAndHookConstructor(findClass("com.tencent.mm.plugin.sns.ui.r", lpparam.classLoader),
//                findClass("com.tencent.mm.protocal.c.bnp", lpparam.classLoader), String::class.java,
//                object : XC_MethodHook() {
//                    override fun afterHookedMethod(param: MethodHookParam) {
//                        val any = param.args[0]
//                        val vnh = getObjectField(any, "wQo")
//                        val url = getObjectField(vnh, "nfX").toString()
//                        CCTHelper.mayLaunchUrl(url)
//                    }
//                })
//
//        findAndHookConstructor(findClass("com.tencent.mm.pluginsdk.ui.applet.k", lpparam.classLoader),
//                String::class.java, Int::class.java, Any::class.java,
//                object : XC_MethodHook() {
//                    override fun afterHookedMethod(param: MethodHookParam) {
//                        val url = param.args[0].toString()
//                        CCTHelper.mayLaunchUrl(url)
//                    }
//                })
//
    }

    private fun hookChrome(lpparam: XC_LoadPackage.LoadPackageParam) {
        findAndHookMethod(Constants.CHROME_CUSTOM_TAB_ACTIVITY, lpparam.classLoader,
                "onOptionsItemSelected", MenuItem::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                //close chrome custom tab when select menu item 'Open in Wechat / 返回微信打开'
                val menuItem = param.args[0] as MenuItem
                val thisActivity = param.thisObject as Activity
                val thatContext = thisActivity.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)
                val text = thatContext.getString(R.string.open_in_wechat)
                if (menuItem.title == text) {
                    thisActivity.finish()
                }
            }
        })
    }
}
