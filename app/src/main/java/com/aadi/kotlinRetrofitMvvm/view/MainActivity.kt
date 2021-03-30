package com.aadi.kotlinRetrofitMvvm.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat.generateViewId
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
                    queryField.setOnFocusChangeListener { view: View, hasFocus: Boolean ->
                        run {
                            val parentView: View = view.parent as View
                            Log.v(MainActivity::class.qualifiedName, "Focus changed for ${queryGroupText.text} with $hasFocus of Edit Text field id ${parentView.id}")
                            if (!hasFocus && queryGroupSwitch.isChecked) mainViewModel.addSearchStringGroupOne(queryField.editableText.toString(), parentView.id)
                            else if (!hasFocus) mainViewModel.addSearchStringGroupTwo(queryField.editableText.toString(), parentView.id)
                        }
                    }

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