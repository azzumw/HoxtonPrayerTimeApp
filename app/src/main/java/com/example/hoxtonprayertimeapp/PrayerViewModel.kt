package com.example.hoxtonprayertimeapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.IllegalArgumentException

class PrayerViewModel :ViewModel() {

    private lateinit var firestore: FirebaseFirestore

    init {
        initialiseFireStoreEmulator()
    }

    private fun initialiseFireStoreEmulator(){
        firestore = Firebase.firestore
        firestore.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
    }

    override fun onCleared() {
        super.onCleared()
        firestore.clearPersistence()
    }

    companion object {
        private const val EMULATOR_HOST = "10.0.2.2"
        private const val EMULATOR_PORT = 8080
    }
}

class PrayerViewModelFactory: ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PrayerViewModel::class.java)){
            return PrayerViewModel() as T
        }else throw IllegalArgumentException("ViewModel not recognised")
    }
}