package com.example.localDb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.util.Converter

@Database(
    entities = [Run::class],
    version = 2

)
@TypeConverters(Converter::class)
abstract class  RunDatabase : RoomDatabase(){
    abstract fun getRunDao():RunDao

    companion object{
        val DATABASE_NAME:String="runDB"
    }
}