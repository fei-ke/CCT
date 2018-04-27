package com.example.cct

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage


class XposedMod : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == Constants.MM_PACKAGE_NAME) {
            hook(lpparam)
        }
    }

    private fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {

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

                                val uri = Uri.parse(Constants.CCT_PROVIDER)
                                val result = activity.contentResolver.call(uri, Constants.METHOD_USE_CCT, url, null)
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

//        val classView = findClass("com.tencent.mm.plugin.sns.ui.a.d", lpparam.classLoader)
//        var method = classView.declaredMethods.findLast { it.name == "a" }
//        XposedBridge.hookMethod(method, object : XC_MethodHook() {
//            override fun afterHookedMethod(param: MethodHookParam) {
//                val any = param.args[3]
//                val vnh = getObjectField(any, "vnh")
//                val mdW = getObjectField(vnh, "mdW")
//                val mdt = getObjectField(vnh, "mdt")
//
//                Log.i("xxxxxxxxxmdW", mdW?.toString())
//                Log.i("xxxxxxxxxmdt", mdt?.toString())
//                val mayLaunchUrl = mCustomTabsSession?.mayLaunchUrl(Uri.parse(mdW.toString()), null, null)
//                Log.i("xxxxxxxxxmayLaunchUrl", "$mayLaunchUrl")
//            }
//        })
//
//
//
        findAndHookMethod(findClass("com.tencent.mm.pluginsdk.ui.chat.AppPanel", lpparam.classLoader), "cbe"
                , object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)
                val thisObject = param.thisObject
                val tQM = getObjectField(thisObject, "voT") as BooleanArray
                tQM[9] = true

                val count = getIntField(thisObject, "voI")
                setIntField(thisObject, "voI", count + 1)
            }
        })
    }
}