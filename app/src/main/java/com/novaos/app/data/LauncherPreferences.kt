package com.novaos.app.data

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon

data class AppEntry(
    val packageName: String,
    val label: String,
    val activityName: String,
    val icon: Drawable,
    val launchIntent: Intent,
)

fun List<AppEntry>.toAppEntries(): List<AppEntry> = this
