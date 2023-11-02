
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.speechbuddy.ui.SpeechBuddyTheme

@Composable
fun SettingsTextUi(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsTextUiPreview(){
    SpeechBuddyTheme{
        SettingsTextUi(text = "content")
    }
}