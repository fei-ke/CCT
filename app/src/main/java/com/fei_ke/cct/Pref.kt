package com.fei_ke.cct

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

object Pref {
    private val DEFAULT_IGNORE_LIST: Map<String, String>

    init {
        DEFAULT_IGNORE_LIST = hashMapOf(
                "饿了么红包" to "https://h5.ele.me/hongbao/.*",
                "美团红包" to "https://activity.waimai.meituan.com/coupon/sharechannel/.*",
                "payapp" to "https://payapp.weixin.qq.com/.*",
                "微信支付" to "https://pay.weixin.qq.com/.*",
                "腾讯公益" to "https://ssl.gongyi.qq.com/.*",
                "腾讯支付" to "https://pay.qq.com/.*",
                "tenpay" to "https://[^\\.]*.tenpay.com/.*",
                "城市服务" to "https://mp.weixin.qq.com/cityservice/\\?.*",
                "定位" to "http://mp.weixin.qq.com/mp/lifedetail\\?.*"
        )

        val initVersion = DEFAULT_IGNORE_LIST.toString().hashCode()

        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.context)
        val curVersion = defaultSharedPreferences.getInt(Constants.KEY_DEFAULT_RULE_VERSION, 0)
        if (initVersion != curVersion) {
            restoreDefaultIgnoreList(App.context, true)
            defaultSharedPreferences.edit()
                    .putInt(Constants.KEY_DEFAULT_RULE_VERSION, initVersion)
                    .apply()
        }
    }

    private var preferences: SharedPreferences? = null

    private fun getPreference(context: Context) = preferences
            ?: context.getSharedPreferences(Constants.IGNORE_LIST_PREF_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): Map<String, *> =
            getPreference(context).all

    fun hasKey(context: Context, key: String): Boolean =
            getPreference(context).contains(key)

    fun add(context: Context, key: String, pattern: String) =
            getPreference(context).edit().putString(key, pattern).apply()

    fun remove(context: Context, key: String) =
            getPreference(context).edit().remove(key).apply()


    fun isInIgnoreList(context: Context, url: String): Boolean =
            getPreference(context).all.values.find { Regex(it.toString()).matches(url) } != null

    fun restoreDefaultIgnoreList(context: Context, keepCustom: Boolean) {
        var defaultList: Map<String, String> = HashMap<String, String>(DEFAULT_IGNORE_LIST)
        val editor = getPreference(context).edit()
        if (keepCustom) {
            val all = getAll(context)
            defaultList = defaultList.filterKeys { !all.containsKey(it) }
        } else {
            editor.clear()
        }

        defaultList.forEach { t, u -> editor.putString(t, u) }
        editor.apply()
    }
}
