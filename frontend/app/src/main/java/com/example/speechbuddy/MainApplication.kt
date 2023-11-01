package com.example.speechbuddy

import android.app.Application
import com.example.speechbuddy.domain.utils.TokenSharedPreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application(){
    companion object{
        lateinit var token_prefs : TokenSharedPreferences
    }
}