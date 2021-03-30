package com.aadi.kotlinRetrofitMvvm.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.koin.standalone.KoinComponent

class MainViewModel:  ViewModel(), KoinComponent {

    private var queryGroupState: MutableLiveData<Boolean>? = MutableLiveData()
    private var groupOneSearchStrings: MutableLiveData<HashMap<Int, String>>? = MutableLiveData()
    private var groupTwoSearchStrings: MutableLiveData<HashMap<Int, String>>? = MutableLiveData()

    companion object {
        const val QUERY_MAX_LENGTH = 20
    }

    init {
        queryGroupState?.value = false
        groupOneSearchStrings?.value = HashMap(3)
        groupTwoSearchStrings?.value = HashMap(3)
        Log.v(MainViewModel::class.qualifiedName, "Initializing Main View Model class")
    }

    fun setQueryGroupState(state: Boolean) {
        queryGroupState?.value = state
        Log.v(MainViewModel::class.qualifiedName, "Changing state to $state")
    }

    fun getQueryGroupState(): LiveData<Boolean> = queryGroupState as LiveData<Boolean>

    fun getGroupOneSearchStrings(): LiveData<HashMap<Int, String>> = groupOneSearchStrings as LiveData<HashMap<Int, String>>

    fun getGroupTwoSearchStrings(): LiveData<HashMap<Int, String>> = groupTwoSearchStrings as LiveData<HashMap<Int, String>>

    fun addSearchStringGroupOne(query: String, position: Int) {
        val restrictedQuery = query.take(QUERY_MAX_LENGTH)
        groupOneSearchStrings?.value?.put(position, restrictedQuery)
        Log.v(MainViewModel::class.qualifiedName, "Adding query to $position in one : $query")
    }

    fun emptySearchStringGroupOne() {
        groupOneSearchStrings?.value?.clear()
        Log.v(MainViewModel::class.qualifiedName, "Emptying Group one strings")
    }

    fun removeSearchStringKeyOne(key: Int) {
        if(groupOneSearchStrings?.value?.containsKey(key) == true)
            groupOneSearchStrings?.value?.remove(key)
        Log.v(MainViewModel::class.qualifiedName, "Removing key in one $key")
    }

    fun addSearchStringGroupTwo(query: String, position: Int) {
        val restrictedQuery = query.take(QUERY_MAX_LENGTH)
        groupTwoSearchStrings?.value?.put(position, restrictedQuery)
        Log.v(MainViewModel::class.qualifiedName, "Adding query to $position in two : $query")
    }

    fun emptySearchStringGroupTwo() {
        groupTwoSearchStrings?.value?.clear()
        Log.v(MainViewModel::class.qualifiedName, "Emptying Group two strings")
    }

    fun removeSearchStringKeyTwo(key: Int) {
        if(groupTwoSearchStrings?.value?.containsKey(key) == true)
            groupTwoSearchStrings?.value?.remove(key)
        Log.v(MainViewModel::class.qualifiedName, "Removing key in two $key")
    }

    fun getBothQueryString(): Pair<StringBuilder, StringBuilder> {
        val stringPair: Pair<StringBuilder, StringBuilder> = Pair(StringBuilder(""), StringBuilder(""))

        for((_: Int, value: String) in groupOneSearchStrings?.value!!)
            stringPair.first.append("+$value")
        for((_: Int, value: String) in groupTwoSearchStrings?.value!!)
            stringPair.second.append("+$value")
        Log.v(MainViewModel::class.qualifiedName, "Query strings ${stringPair.first} ${stringPair.second}")

        return stringPair
    }

}
