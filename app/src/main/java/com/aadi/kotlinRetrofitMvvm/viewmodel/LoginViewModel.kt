package com.aadi.kotlinRetrofitMvvm.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private var auth: FirebaseAuth = Firebase.auth
    private var currentUserData: MutableLiveData<FirebaseUser?> = MutableLiveData()

    init {
        currentUserData.postValue(auth.currentUser)
    }

    fun getUidLiveData(): LiveData<FirebaseUser?> = currentUserData

    fun signInCredentials(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Log.v(LoginViewModel::class.qualifiedName, "Authentication successful")
                    currentUserData.value = task.result?.user
                } else
                    Log.e(LoginViewModel::class.qualifiedName, "Authentication failed", task.exception)

            }
    }
}