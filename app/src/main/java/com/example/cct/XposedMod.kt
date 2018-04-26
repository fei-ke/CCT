package com.example.cct

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsServiceConnection
import android.support.customtabs.CustomTabsSession
import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage


class XposedMod : IXposedHookLoadPackage {
    companion object {
        val MM_PACKAGE_NAME = "com.tencent.mm"
        val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"  // Change when in stable
        var mCustomTabsSession: CustomTabsSession? = null
    }


    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == MM_PACKAGE_NAME) {
            hook(lpparam)
        }
    }

    private fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {

        findAndHookMethod(Application::class.java, "onCreate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val connection = object : CustomTabsServiceConnection() {
                    override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                        mCustomTabsSession = client.newSession(null)
                        Log.i("xxxxxxxxxonCustomTabsServiceConnected", "$mCustomTabsSession")
                    }

                    override fun onServiceDisconnected(name: ComponentName) {

                    }
                }
                val ok = CustomTabsClient.bindCustomTabsService(param.thisObject as Context, CUSTOM_TAB_PACKAGE_NAME, connection)
                Log.i("xxxxxxxxxok", "$ok")
            }
        })

        findAndHookMethod(Activity::class.java, "startActivityForResult", Intent::class.java, Int::class.java, Bundle::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val intent = param.args[0] as Intent
                        if (intent.component?.className == "com.tencent.mm.plugin.webview.ui.tools.WebViewUI"
                                && !intent.getBooleanExtra("ignoreCCT", false)) {
                            val activity = param.thisObject as Activity
                            CCTUtil.open(activity, intent)
                            //returm
                            param.result = null
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