package com.aadi.kotlinRetrofitMvvm.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat.generateViewId
import androidx.core.widget.addTextChangedListener
import com.aadi.kotlinRetrofitMvvm.R
import com.aadi.kotlinRetrofitMvvm.viewmodel.MainViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var linearLayout: LinearLayout
    private lateinit var queryGroupSwitch : SwitchCompat
    private lateinit var queryGroupText: TextView
    private lateinit var editTextList: HashMap<Int, HashMap<Int, RelativeLayout>?>
    private val mainViewModel: MainViewModel = MainViewModel()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        queryGroupSwitch = findViewById(R.id.query_group_switch)
        queryGroupText = findViewById(R.id.query_group_text)
        val queryAddButton: Button = findViewById(R.id.query_add_button)
        linearLayout = findViewById(R.id.linear_layout_main)
        editTextList = HashMap(2)
        editTextList[0] = HashMap(3)
        editTextList[1] = HashMap(3)

        queryGroupSwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                Log.v(MainActivity::class.qualifiedName, "Switch state changed $isChecked")
                mainViewModel.setQueryGroupState(isChecked)
            }
        }

        findViewById<Button>(R.id.query_submit_button).setOnClickListener {
            run {
                val queryStrings: Pair<StringBuilder, StringBuilder> = mainViewModel.getBothQueryString()
                Log.v(MainActivity::class.qualifiedName, "Received query strings First ${queryStrings.first} Second ${queryStrings.second}")

                val intent = Intent(this@MainActivity,TabbedActivity::class.java);
                intent.putExtra(resources.getString(R.string.query_group1), queryStrings.first.toString())
                intent.putExtra(resources.getString(R.string.query_group2), queryStrings.second.toString())
                startActivity(intent)
            }
        }

        mainViewModel.getQueryGroupState().observe(this, { queryGroupState: Boolean ->
            run {
                Log.v(MainActivity::class.qualifiedName, "Query Group State Observer $queryGroupState")
                if(queryGroupState) queryGroupText.text = resources.getString(R.string.query_group1)
                else queryGroupText.text = resources.getString(R.string.query_group2)

                val currentHashMap: HashMap<Int, RelativeLayout> = when (queryGroupState) {
                    true -> editTextList[0] as HashMap<Int, RelativeLayout>
                    false -> editTextList[1] as HashMap<Int, RelativeLayout>
                }

                Log.v(MainActivity::class.qualifiedName, "Removing all views")
                linearLayout.removeAllViews()
                for((_: Int, value: RelativeLayout) in currentHashMap) linearLayout.addView(value)
            }
        })

        queryAddButton.setOnClickListener {
            run {
                Log.v(MainActivity::class.qualifiedName, "New Query Edit Text field add request ${queryGroupText.text}")
                val currentHashMap: HashMap<Int, RelativeLayout> = when (queryGroupSwitch.isChecked) {
                    true -> editTextList[0] as HashMap<Int, RelativeLayout>
                    false -> editTextList[1] as HashMap<Int, RelativeLayout>
                }
                if(currentHashMap.size < 3) {

                    val queryEntryView: RelativeLayout = layoutInflater.inflate(R.layout.query_entry, null, false) as RelativeLayout
                    linearLayout.addView(queryEntryView)
                    queryEntryView.id = generateViewId()
                    currentHashMap[queryEntryView.id] = queryEntryView
                    Log.v(MainActivity::class.qualifiedName, "New Query Edit Text field add request ${queryEntryView.id} ")

                    val queryField: EditText = queryEntryView.findViewById(R.id.query_search_entry_point)
                    queryField.addTextChangedListener(object: TextWatcher{
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                        override fun afterTextChanged(s: Editable?) {
                            Log.v(MainActivity::class.qualifiedName, "Text changed of Edit Text field ${queryEntryView.id}")
                            if (queryGroupSwitch.isChecked) s?.toString()?.let { it1 -> mainViewModel.addSearchStringGroupOne(it1, queryEntryView.id) }
                            else s?.toString()?.let { it1 -> mainViewModel.addSearchStringGroupTwo(it1, queryEntryView.id) }
                        }

                    })

                    val queryRemove: Button = queryEntryView.findViewById(R.id.query_search_remove_button)
                    queryRemove.setOnClickListener { view: View ->
                        run {
                            val parentView: View = view.parent as View
                            Log.v(MainActivity::class.qualifiedName, "Remove Query Edit Text field add request ${parentView.id} ")
                            if (queryGroupSwitch.isChecked)  mainViewModel.removeSearchStringKeyOne(parentView.id)
                            else mainViewModel.removeSearchStringKeyTwo(parentView.id)
                            currentHashMap.remove(parentView.id)
                            linearLayout.removeView(parentView)
                        }
                    }
                }
                else {
                    Log.v(MainActivity::class.qualifiedName, "Failed Query Edit Text field add request ${currentHashMap.size}")
                    Toast.makeText(this, "Cannot make more than 3 queries", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        Log.v(MainActivity::class.qualifiedName, "Activity resume ")
        super.onResume()

        mainViewModel.getGroupOneSearchStrings().observe(this, { queryEntryPoints: HashMap<Int, String> ->
            run {
                Log.v(MainActivity::class.qualifiedName, "Restoring edit text fields for one ")
                for((key: Int, value: String) in queryEntryPoints) {
                    val queryField: EditText? = editTextList[0]?.get(key)?.findViewById(R.id.query_search_entry_point)
                    queryField?.setText(value)
                }
            }
        })

        mainViewModel.getGroupTwoSearchStrings().observe(this, { queryEntryPoints: HashMap<Int, String> ->
            run {
                Log.v(MainActivity::class.qualifiedName, "Restoring edit text fields for one ")
                for((key: Int, value: String) in queryEntryPoints) {
                    val queryField: EditText? = editTextList[1]?.get(key)?.findViewById(R.id.query_search_entry_point)
                    queryField?.setText(value)
                }
            }
        })

    }

    override fun onRestart() {
        Log.v(MainActivity::class.qualifiedName, "Activity restart ")
        super.onRestart()

        Log.v(MainActivity::class.qualifiedName, "Resetting parameters ")
        mainViewModel.emptySearchStringGroupOne()
        mainViewModel.emptySearchStringGroupTwo()
        linearLayout.removeAllViews()

        Log.v(MainActivity::class.qualifiedName, "Size of Edit Text List zero ${editTextList[0]?.size}")
        editTextList[0]?.clear()

        Log.v(MainActivity::class.qualifiedName, "Size of Edit Text List one ${editTextList[1]?.size}")
        editTextList[1]?.clear()

        queryGroupSwitch.isChecked = false
    }
}