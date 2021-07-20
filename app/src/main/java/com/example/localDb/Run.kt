package com.example.localDb

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "rundb")
data class Run(
    var icon: Bitmap? = null,
    var avgSpeedKHM: Float = 0f,
    var distance: Int = 6,
    var timeStamp: Long = 30L,
    var timeInMillis: Long = 20L,
    var caloriesBurn: Int = 40
) {
    @PrimaryKey(autoGenerate = true)
    var id:Int?=null
}
