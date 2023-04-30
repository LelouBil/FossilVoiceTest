package net.leloubil.fossilvoicetest

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.leloubil.fossilvoicetest.ui.theme.FossilVoiceTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FossilVoiceTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainComponent()
                }
            }
        }
    }
}

@Composable
fun MainComponent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp).fillMaxWidth())
        Text("Fossil Voice Test", style = MaterialTheme.typography.headlineLarge)
        Spacer(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
        )
        ModelChoice()
    }
}

@Composable
fun ModelChoice() {
    val value = LocalContext.current.getSharedPreferences("prefs", MODE_PRIVATE)
        .getString("language", "English")
    val languages = listOf("French","English")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(value) }
    val context = LocalContext.current
    fun onSelect(key: String) {
        context.getSharedPreferences("prefs", MODE_PRIVATE).edit()
            .putString("language", key).apply()
        onOptionSelected(key)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Language", style = MaterialTheme.typography.titleLarge)
        //large spacer
        Spacer(modifier = Modifier
            .height(16.dp)
            .fillMaxWidth())
        languages.forEach { language ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedOption == language,
                    onClick = {onSelect(language)}
                )
                Text(language)
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FossilVoiceTestTheme {
        MainComponent()
    }
}
