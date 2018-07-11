package com.fei_ke.cct

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

            val arguments = arguments ?: throw RuntimeException("arguments should not be null!")

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

            val context = activity!!
            val dialogBuilder = AlertDialog.Builder(context, theme)
                    .setMessage(R.string.edit_rule_dialog_message)
                    .setView(contentView)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .setNegativeButton(R.string.dialog_cancel, null)

            if (rawUrl != null) {
                dialogBuilder.setNeutralButton(R.string.test, null)
            }

            val dialog = dialogBuilder.create()

            editTextName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    val text = s.toString()
                    if (rawName != text && Pref.hasKey(context, text)) {
                        editTextName.error = getString(R.string.duplicate_rule_name_hint)
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
                        editTextPattern.error = getString(R.string.rule_pattern_empty_hint)
                        return@setOnClickListener
                    }

                    if (rawName != null && rawName != name) {
                        Pref.remove(context, rawName!!)
                    }
                    Pref.add(context, name, pattern)
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
            activity?.finish()
        }

    }
}
