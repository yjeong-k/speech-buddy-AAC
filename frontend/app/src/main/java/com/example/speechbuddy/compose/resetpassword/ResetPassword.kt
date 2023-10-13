package com.example.speechbuddy.compose.resetpassword

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.speechbuddy.R
import com.example.speechbuddy.compose.utils.ButtonLevel
import com.example.speechbuddy.compose.utils.ButtonUi
import com.example.speechbuddy.compose.utils.TextFieldUi
import com.example.speechbuddy.compose.utils.TitleUi
import com.example.speechbuddy.compose.utils.TopAppBarUi
import com.example.speechbuddy.ui.SpeechBuddyTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPassword(
    onResetClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TopAppBarUi(
                    onBackClick = { onResetClick() }
                )
            }
        ) {
            var password = remember { mutableStateOf("") }
            var passwordCheck = remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .padding(horizontal = 25.dp, vertical = 35.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TitleUi(
                    title = stringResource(id = R.string.reset_passoword_title),
                    description = stringResource(id = R.string.reset_password_subtitle2)
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Password Text Field
                TextFieldUi(
                    label = { Text(stringResource(id = R.string.new_password_field)) },
                    value = password.value,
                    onValueChange = { password.value = it },
                    supportingText = { Text(stringResource(id = R.string.false_new_password)) },
                    isError = false,
                    isValid = false,
                    isHidden = false,
                )

                // Password Check Text Field
                TextFieldUi(
                    label = { Text(stringResource(id = R.string.new_password_check_field)) },
                    value = passwordCheck.value,
                    onValueChange = { passwordCheck.value = it },
                    supportingText = { Text(stringResource(id = R.string.false_new_password_check)) },
                    isError = false,
                    isValid = false,
                    isHidden = false,
                )

                // Set password Button
                ButtonUi(
                    text = stringResource(id = R.string.reset_password_next),
                    onClick = { /* perform login */ },
                    isError = false,
                    isEnabled = true,
                    level = ButtonLevel.PRIMARY
                )

            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    SpeechBuddyTheme {
        ResetPassword({})
    }
}

