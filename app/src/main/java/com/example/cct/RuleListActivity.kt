package com.example.cct

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ListView
import android.widget.SimpleAdapter
import com.example.cct.Constants.KEY_RULE_NAME
import com.example.cct.Constants.KEY_RULE_PATTERN

class RuleListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
                .replace(Window.ID_ANDROID_CONTENT, RuleListFragment())
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.menu_add_rule)
                .setIcon(R.drawable.ic_menu_add)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener {
                    val intent = Intent(this, EditRuleActivity::class.java)
                    startActivity(intent)
                    true
                }
        menu.add(R.string.menu_restore_default_rules)
                .setOnMenuItemClickListener {
                    restoreDefaultRules()
                    true
                }
        return super.onCreateOptionsMenu(menu)
    }

    private fun restoreDefaultRules() {
        AlertDialog.Builder(this)
                .setMessage(R.string.dialog_restore_rules_message)
                .setNeutralButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_keep_custom_rules) { _, _ ->
                    Pref.restoreDefaultIgnoreList(this, true)
                    recreate()
                }
                .setNegativeButton(R.string.dialog_delete_custom_rule) { _, _ ->
                    Pref.restoreDefaultIgnoreList(this, false)
                    recreate()
                }
                .show()


    }

    class RuleListFragment : ListFragment() {
        private lateinit var data: MutableList<Map<String, *>>
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            listView.setOnItemLongClickListener { _, _, position, _ ->
                val ruleName = data[position][KEY_RULE_NAME].toString()

                AlertDialog.Builder(activity!!)
                        .setTitle(getString(R.string.confirm_delete))
                        .setPositiveButton(R.string.dialog_ok) { _, _ ->
                            Pref.remove(activity!!, ruleName)
                            data.removeAt(position)
                            (listAdapter as SimpleAdapter)
                                    .notifyDataSetChanged()
                        }
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                return@setOnItemLongClickListener true
            }
        }

        override fun onResume() {
            super.onResume()
            data = ArrayList(Pref.getAll(activity!!)
                    .toList()
                    .map { mapOf(KEY_RULE_NAME to it.first, KEY_RULE_PATTERN to it.second) }
            )

            listAdapter = SimpleAdapter(activity, data, android.R.layout.simple_list_item_2,
                    arrayOf(KEY_RULE_NAME, KEY_RULE_PATTERN),
                    intArrayOf(android.R.id.text1, android.R.id.text2))
        }

        override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
            val map = data[position]
            val name = map[KEY_RULE_NAME]
            val pattern = map[KEY_RULE_PATTERN]
            val intent = Intent(activity, EditRuleActivity::class.java).apply {
                putExtra(KEY_RULE_NAME, name.toString())
                putExtra(KEY_RULE_PATTERN, pattern.toString())
            }
            startActivity(intent)
        }
    }
}
