package com.example.speechbuddy.compose.texttospeech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.speechbuddy.R
import com.example.speechbuddy.compose.utils.HomeTopAppBarUi
import com.example.speechbuddy.compose.utils.TitleUi
import com.example.speechbuddy.ui.SpeechBuddyTheme
import com.example.speechbuddy.ui.models.ButtonStatusType
import com.example.speechbuddy.viewmodel.TextToSpeechViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToSpeechScreen(
    modifier: Modifier = Modifier,
    bottomPaddingValues: PaddingValues,
    viewModel: TextToSpeechViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(topBar = {
            HomeTopAppBarUi(title = stringResource(id = R.string.talk_with_speech))
        }) { topPaddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = topPaddingValues.calculateTopPadding(),
                        bottom = bottomPaddingValues.calculateBottomPadding()
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TitleUi(
                    title = stringResource(id = R.string.tts_text),
                    description = stringResource(id = R.string.tts_explain)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = viewModel.textInput,
                    onValueChange = {
                        viewModel.setText(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .height(200.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row {
                    TextToSpeechButton(
                        buttonStatus = uiState.buttonStatus,
                        onPlay = { viewModel.ttsStart(context) },
                        onStop = { viewModel.ttsStop() },
                        viewModel = viewModel
                    )

                    TextClearButton(
                        buttonStatus = uiState.buttonStatus,
                        onPlay = { viewModel.clearText() },
                        onStop = {},
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun TextToSpeechButton(
    buttonStatus: ButtonStatusType,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    viewModel: TextToSpeechViewModel
) {
    val textToSpeechButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    )

    if (buttonStatus == ButtonStatusType.PLAY && !viewModel.isEmptyText()) {
        Button(
            onClick = onPlay,
            modifier = Modifier.size(
                width = 200.dp,
                height = 50.dp
            ),
            colors = textToSpeechButtonColors
        ) {
            Text(
                text = stringResource(id = R.string.play_text),
                style = MaterialTheme.typography.headlineMedium
            )
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = stringResource(id = R.string.play_text),
                modifier = Modifier.size(36.dp)
            )
        }
    } else if (buttonStatus == ButtonStatusType.PLAY && viewModel.isEmptyText()) {
        Button(
            onClick = {},
            modifier = Modifier.size(
                width = 200.dp,
                height = 50.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Text(
                text = stringResource(id = R.string.play_text),
                style = MaterialTheme.typography.headlineMedium
            )
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = stringResource(id = R.string.play_text),
                modifier = Modifier.size(36.dp)
            )
        }
    } else {
        Button(
            onClick = onStop,
            modifier = Modifier.size(
                width = 200.dp,
                height = 50.dp
            ),
            colors = textToSpeechButtonColors
        ) {
            Text(
                text = stringResource(id = R.string.stop_text),
                style = MaterialTheme.typography.headlineMedium
            )
            Icon(
                painterResource(R.drawable.stop_icon),
                contentDescription = stringResource(id = R.string.stop_text),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun TextClearButton(
    buttonStatus: ButtonStatusType,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    viewModel: TextToSpeechViewModel
) {
    if (buttonStatus == ButtonStatusType.PLAY && !viewModel.isEmptyText()) {
        Button(
            onClick = onPlay,
            modifier = Modifier.size(
                width = 200.dp,
                height = 50.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text(
                text = stringResource(id = R.string.clear_text),
                style = MaterialTheme.typography.headlineMedium
            )
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.clear_text),
                modifier = Modifier.size(36.dp)
            )
        }
    } else {
        Button(
            onClick = onStop,
            modifier = Modifier.size(
                width = 200.dp,
                height = 50.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Text(
                text = stringResource(id = R.string.clear_text),
                style = MaterialTheme.typography.headlineMedium
            )
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.clear_text),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Preview
@Composable
fun TextToSpeechScreenPreview() {
    SpeechBuddyTheme {
        TextToSpeechScreen(
            bottomPaddingValues = PaddingValues(16.dp)
        )
    }
}