package com.example.speechbuddy.utils

import android.util.Patterns
import com.example.speechbuddy.utils.Constants.Companion.MINIMUM_PASSWORD_LENGTH

fun isValidEmail(input: String): Boolean {
    return input.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(input).matches()
}

fun isValidPassword(input: String): Boolean {
    return input.length >= MINIMUM_PASSWORD_LENGTH
}

fun isValidVerifyNumber(input: String): Boolean{
    return input.length == 6
}