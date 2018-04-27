package com.example.cct

object Constants {
    const val MM_PACKAGE_NAME = "com.tencent.mm"
    const val MM_WEB_VIEW_UI = "com.tencent.mm.plugin.webview.ui.tools.WebViewUI"
    const val KEY_RAW_URL = "rawUrl"

    const val MM_DEFAULT_STATUS_BAR_COLOR = 0xFF303034.toInt()
    const val MM_DEFAULT_TOOL_BAR_COLOR = 0xFF393A3E.toInt()

    const val ACTION_ADD_RULE = BuildConfig.APPLICATION_ID + ".add_rule"
    const val CCT_PROVIDER = "content://" + BuildConfig.APPLICATION_ID + ".provider"
    const val METHOD_USE_CCT = "useCCT"
    const val KEY_USE_CCT = "use_cct"
    const val KEY_IGNORE_CCT = "ignore_cct"

    const val KEY_RULE_NAME = "rule_name"
    const val KEY_RULE_PATTERN = "rule_pattern"
}