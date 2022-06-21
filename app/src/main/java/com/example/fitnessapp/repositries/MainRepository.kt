package com.example.fitnessapp.repositries

import com.example.fitnessapp.db.RunData
import com.example.fitnessapp.db.RunningDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MainRepository @Inject constructor(private val runDao:RunningDao ) {
    suspend fun insertRun(runData: RunData) = runDao.insertRun(runData)

    suspend fun deleteRun(runData: RunData) = runDao.deleteRun(runData)

    suspend fun getAllRunsSortedByDate(): Flow<MutableList<RunData>> {
        return flow {
            emit(runDao.getAllRunsSortedByDate())
        }
    }
    suspend fun getAllRunsSortedByDistance(): Flow<MutableList<RunData>> {
        return flow {
            emit(runDao.getAllRunsSortedByDistance())
        }
    }
    suspend fun getAllRunsSortedByTimeInMillis(): Flow<MutableList<RunData>> {
        return flow {
            emit(runDao.getAllRunsSortedByTimeInMillis())
        }
    }
    suspend fun getAllRunsSortedByCaloriesBurned(): Flow<MutableList<RunData>> {
        return flow {
            emit(runDao.getAllRunsSortedByCaloriesBurned())
        }
    }

    suspend fun getAllRunsSortedByAvgSpeed(): Flow<MutableList<RunData>> {
        return flow {
            emit(runDao.getAllRunsSortedByAvgSpeed())
        }
    }

    suspend fun getTotalAvgSpeed(): Flow<Float> {
        return flow {
            emit(runDao.getTotalAvgSpeed())
        }
    }

    suspend fun getTotalDistance(): Flow<Int> {
        return flow {
            emit(runDao.getTotalDistanceInMeters())
        }
    }

    suspend fun getTotalCaloriesBurned(): Flow<Int> {
        return flow {
            emit(runDao.getTotalCaloriesBurned())
        }
    }

    suspend fun getTotalTimeInMillis(): Flow<Long> {
        return flow {
            emit(runDao.getTotalTimeMillie())
        }
    }
}