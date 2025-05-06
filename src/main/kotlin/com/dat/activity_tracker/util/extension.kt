package com.dat.activity_tracker.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asDesktopBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.skia.Bitmap

fun ImageVector.toBitmap(): ImageBitmap {
    return this.toBitmap()
}