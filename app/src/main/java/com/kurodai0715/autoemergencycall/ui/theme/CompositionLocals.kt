/*
 * Copyright (c) 2025 Okuda Tomohiro
 * Licensed under the MIT License.
 */

package com.kurodai0715.directdebitmanager.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import coil.ImageLoader

val LocalImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("ImageLoader not provided")
}

val LocalDensityMode = compositionLocalOf { DensityMode.Normal }
