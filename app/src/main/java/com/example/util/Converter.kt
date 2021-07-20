package com.example.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converter : BitmapConverter {

    @TypeConverter
    override fun fromBitmap(icon: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        icon.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    override fun toBitmap(byteArray: ByteArray): Bitmap {

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


}