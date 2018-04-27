package com.example.cct

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast

class EditRuleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ruleName = intent.getStringExtra(Constants.KEY_RULE_NAME)
        val rawUrl = intent.getStringExtra(Constants.KEY_RAW_URL)
        val rulePattern = intent.getStringExtra(Constants.KEY_RULE_PATTERN) ?: rawUrl

        val args = Bundle().apply {
            putString(Constants.KEY_RULE_NAME, ruleName)
            putString(Constants.KEY_RAW_URL, rawUrl)
            putString(Constants.KEY_RULE_PATTERN, rulePattern)
        }

        val dialogFragment = EditRuleFragment().apply {
            arguments = args
        }
        dialogFragment.show(supportFragmentManager, "")

    }

    class EditRuleFragment : DialogFragment() {
        private var rawUrl: String? = null
        private var rawName: String? = null
        private var rulePattern: String? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setStyle(DialogFragment.STYLE_NORMAL, 0)

            rawUrl = arguments.getString(Constants.KEY_RAW_URL)
            rawName = arguments.getString(Constants.KEY_RULE_NAME)
            rulePattern = arguments.getString(Constants.KEY_RULE_PATTERN)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val contentView = View.inflate(activity, R.layout.layout_edit_rule, null)

            val editTextName = contentView.findViewById<EditText>(R.id.editTextName)
            val editTextPattern = contentView.findViewById<EditText>(R.id.editTextPattern)

            editTextName.setText(rawName)
            editTextPattern.setText(rulePattern)

            val dialog = AlertDialog.Builder(context, theme)
                    .setView(contentView)
                    .setPositiveButton("确定", null)
                    .setNegativeButton("取消", null)
                    .setNeutralButton("测试", null)
                    .create()

            editTextName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    val text = s.toString()
                    if (rawName != text && Pref.hasKey(activity, text)) {
                        editTextName.error = "已有同名规则存在，保存将覆盖"
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })

            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val pattern = editTextPattern.text.toString()
                    var name = editTextName.text.toString()
                    if (TextUtils.isEmpty(name)) {
                        name = pattern
                    }

                    if (TextUtils.isEmpty(pattern)) {
                        editTextPattern.error = "不能为空"
                        return@setOnClickListener
                    }

                    if (rawName != null && rawName != name) {
                        Pref.remove(activity, rawName!!)
                    }
                    Pref.add(activity, name, pattern)
                    dismiss()
                }

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    val test = rawUrl != null && Regex(editTextPattern.text.toString()).matches(rawUrl!!)

                    Toast.makeText(activity, test.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            return dialog
        }

        override fun onDismiss(dialog: DialogInterface?) {
            super.onDismiss(dialog)
            activity.finish()
        }

    }
}