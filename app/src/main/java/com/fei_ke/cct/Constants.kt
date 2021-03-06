package com.fei_ke.cct

object Constants {
    val PACKAGE_CONFIG = setOf(
        PackageConfig(
            packageName = "com.tencent.mm",
            webViewUiSet = setOf(
                "com.tencent.mm.plugin.webview.ui.tools.WebViewUI",
                "com.tencent.mm.plugin.webview.ui.tools.WebviewMpUI",
                "com.tencent.mm.plugin.brandservice.ui.timeline.preload.ui.TmplWebViewTooLMpUI"
            ),
            keyRawUrl = "rawUrl"
        ),
        PackageConfig(
            packageName = "com.tencent.wework",
            webViewUiSet = setOf(
                "com.tencent.wework.common.web.JsWebActivity",
                "com.tencent.wework.common.web.JsWebActivityWhithoutMoreOperation"
            ),
            keyRawUrl = "extra_web_url"
        )
    )

    const val CHROME_PACKAGE_NAME = "com.android.chrome"
    const val CHROME_CUSTOM_TAB_ACTIVITY = "org.chromium.chrome.browser.customtabs.CustomTabActivity"

    const val ACTION_ADD_RULE = BuildConfig.APPLICATION_ID + ".add_rule"
    const val CCT_PROVIDER = "content://${BuildConfig.APPLICATION_ID}.provider"
    const val METHOD_USE_CCT = "useCCT"
    const val KEY_USE_CCT = "use_cct"
    const val KEY_IGNORE_CCT = "ignore_cct"

    const val KEY_RULE_NAME = "rule_name"
    const val KEY_RAW_URL: String="raw_url"
    const val KEY_RULE_PATTERN = "rule_pattern"
    const val IGNORE_LIST_PREF_NAME = "ignore_list"

    const val KEY_DEFAULT_RULE_VERSION = "default_rule_version"

    class PackageConfig(val packageName: String, val webViewUiSet: Set<String>, val keyRawUrl: String)
}
