package com.cropwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cropwise.app.ui.navigation.CropWiseApp
import com.cropwise.app.ui.theme.CropWiseTheme

class MainActivity : ComponentActivity() {

    private val container by lazy { AppContainer(this) }
    private val vm: MainViewModel by viewModels { MainViewModel.Factory(container.repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CropWiseTheme {
                val ready by vm.ready.collectAsStateWithLifecycle()
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (!ready) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        CropWiseApp(vm)
                    }
                }
            }
        }
    }
}
