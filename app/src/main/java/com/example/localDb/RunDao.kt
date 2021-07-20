package com.example.localDb

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao


@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: Run)

    @Delete
    suspend fun remove(run: Run)

    @Query("SELECT * FROM RUNDB ORDER BY timeStamp DESC")
      fun getAllData(): LiveData<List<Run>>

    @Query("SELECT * FROM RUNDB ORDER BY avgSpeedKHM DESC")
     fun getSortedBySpeed(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) FROM RUNDB")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(avgSpeedKHM) FROM RUNDB")
    fun getTotalAvgSpeed(): LiveData<Float>

    @Query("SELECT SUM(distance) FROM RUNDB")
    fun getTotalDistance(): LiveData<Int>

}