package com.example.data

import android.graphics.drawable.Drawable

data class AppModel(
    val label: String,
    val packageName: String,
    val activityName: String,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val iconDrawable: Drawable? = null
)
