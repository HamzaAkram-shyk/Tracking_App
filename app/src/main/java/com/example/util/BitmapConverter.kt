package com.example.util

import android.graphics.Bitmap

interface BitmapConverter {

    fun fromBitmap(icon: Bitmap): ByteArray
    fun toBitmap(byteArray: ByteArray): Bitmap
}