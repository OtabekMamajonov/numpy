package com.dokonhisob.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dokonhisob.app.ui.navigation.DokonNavGraph
import com.dokonhisob.app.ui.theme.DokonHisobKitobTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DokonApplication

        setContent {
            DokonHisobKitobTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DokonNavGraph(app = app)
                }
            }
        }
    }
}
