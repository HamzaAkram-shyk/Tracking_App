package com.example.Repository

import android.content.Context
import com.example.di.MainActivityContext
import com.example.localDb.Run
import com.example.localDb.RunDao
import com.example.stepcounterapp.MainActivity
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runRunDao: RunDao,

) {

    suspend fun insertRun(run: Run) {
        runRunDao.insert(run)
    }

    fun getAllRun()= runRunDao.getAllData()

}