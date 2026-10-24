package com.novaos.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novaos.app.data.AppEntry
import com.novaos.app.data.LauncherPreferences
import com.novaos.app.data.toAppEntries
import com.novaos.app.ui.theme.NovaOsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val viewModel: LauncherViewModel = viewModel(factory = LauncherViewModelFactory(context))
            NovaOsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LauncherScreen(viewModel = viewModel)
                }
            }
        }
    }
}

class LauncherViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LauncherViewModel(context.applicationContext) as T
    }
}

class LauncherViewModel(private val appContext: Context) : ViewModel() {
    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private val _themeMode = MutableStateFlow(LauncherPreferences.ThemeMode.MIDNIGHT)
    val themeMode: StateFlow<LauncherPreferences.ThemeMode> = _themeMode.asStateFlow()

    fun loadApps() {
        _apps.value = appContext.packageManager
            .queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .mapNotNull { info ->
                val label = info.loadLabel(appContext.packageManager).toString().ifBlank { info.activityInfo.packageName }
                val launchIntent = appContext.packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (launchIntent == null) null else AppEntry(
                    packageName = info.activityInfo.packageName,
                    label = label,
                    activityName = info.activityInfo.name,
                    icon = info.loadIcon(appContext.packageManager),
                    launchIntent = launchIntent,
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun toggleTheme() {
        _themeMode.value = when (_themeMode.value) {
            LauncherPreferences.ThemeMode.MIDNIGHT -> LauncherPreferences.ThemeMode.GRAPHITE
            LauncherPreferences.ThemeMode.GRAPHITE -> LauncherPreferences.ThemeMode.OLED
            LauncherPreferences.ThemeMode.OLED -> LauncherPreferences.ThemeMode.MIDNIGHT
        }
    }
}

@Composable
fun LauncherScreen(viewModel: LauncherViewModel) {
    val apps by viewModel.apps.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F17))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "NOVA OS",
                    color = Color(0xFFF5F7FF),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "premium Android TV launcher",
                    color = Color(0xFFB8C4E0),
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = { viewModel.toggleTheme() },
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = "Theme: ${themeMode.name}")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeMetricCard(label = "Apps", value = apps.size.toString())
            HomeMetricCard(label = "Profile", value = "Default")
            HomeMetricCard(label = "Status", value = "Offline")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(apps) { app ->
                AppRowCard(app = app, context = context)
            }
        }
    }
}

@Composable
fun HomeMetricCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .height(80.dp)
            .weight(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, color = Color(0xFFB8C4E0), fontSize = 12.sp)
            Text(text = value, color = Color(0xFFF5F7FF), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AppRowCard(app: AppEntry, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2A354A), RoundedCornerShape(20.dp))
            .clickable {
                try {
                    context.startActivity(app.launchIntent)
                } catch (_: Exception) {
                    // Graceful failure for invalid launchables.
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF1A2B45), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = app.icon.toBitmap()
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    color = Color(0xFFF5F7FF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
                Text(
                    text = app.packageName,
                    color = Color(0xFFB8C4E0),
                    fontSize = 12.sp
                )
            }

            Text(text = "Launch", color = Color(0xFF5EA5FF), fontWeight = FontWeight.Bold)
        }
    }
}
