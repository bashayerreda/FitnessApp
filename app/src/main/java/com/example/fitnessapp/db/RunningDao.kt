package com.example.fitnessapp.db

import androidx.room.*

@Dao
interface RunningDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(runData: RunData)

    @Delete
    suspend fun deleteRun(runData: RunData)
    @Query("select * from running_table ORDER BY timestamp DESC")
    suspend fun getAllRunsSortedByDate() : MutableList<RunData>
    @Query("select * from running_table ORDER BY timeInMillis DESC")
    suspend fun getAllRunsSortedByTimeInMillis() : MutableList<RunData>

    @Query("select * from running_table ORDER BY caloriesBurned DESC")
    suspend fun getAllRunsSortedByCaloriesBurned() : MutableList<RunData>

    @Query("select * from running_table ORDER BY avgSpeedInKMH DESC")
    suspend fun getAllRunsSortedByAvgSpeed() : MutableList<RunData>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance():  MutableList<RunData>


    //for statistic issues

    @Query("select AVG(avgSpeedInKMH) from running_table")
    suspend fun getTotalAvgSpeed() : Float

    @Query("select SUM(caloriesBurned) from running_table")
    suspend fun getTotalCaloriesBurned() : Int

    @Query("select SUM(timeInMillis) from running_table")
    suspend fun getTotalTimeMillie() : Long

    @Query("select SUM(distanceInMeters) from running_table")
    suspend fun getTotalDistanceInMeters() : Int



}