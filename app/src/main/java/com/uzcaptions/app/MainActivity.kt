package com.uzcaptions.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.uzcaptions.app.ui.navigation.UzCaptionsNavGraph
import com.uzcaptions.app.ui.theme.UzCaptionsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as UzCaptionsApplication

        setContent {
            UzCaptionsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UzCaptionsNavGraph(app = app)
                }
            }
        }
    }
}
