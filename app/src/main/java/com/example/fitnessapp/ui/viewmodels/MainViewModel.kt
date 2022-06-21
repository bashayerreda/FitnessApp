package com.example.fitnessapp.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.RunData
import com.example.fitnessapp.others.SortType
import com.example.fitnessapp.repositries.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel(){
    val liveDataSortedByDate : MutableLiveData<MutableList<RunData>> = MutableLiveData(mutableListOf())
    val runDataSortedByDistance : MutableLiveData<MutableList<RunData>> = MutableLiveData(mutableListOf())
    val runDataSortedByAvgSpeed : MutableLiveData<MutableList<RunData>> = MutableLiveData(mutableListOf())
    val runDataSortedByCaloriesBurned : MutableLiveData<MutableList<RunData>> = MutableLiveData(mutableListOf())
    val runDataSortedByTimeMillis : MutableLiveData<MutableList<RunData>> = MutableLiveData(mutableListOf())
    fun insertDataIntoDataBase(runData: RunData){
        viewModelScope.launch(Dispatchers.IO)  {
            mainRepository.insertRun(runData)
        }

    }
    fun collectDataFromRepoSortedByDate(){
        viewModelScope.launch(Dispatchers.IO){
             mainRepository.getAllRunsSortedByDate()
                 .collect {
                     liveDataSortedByDate.postValue(it)
                     print(liveDataSortedByDate)
                 }
        }
    }
    fun collectDataFromRepoSortedByDistance(){
        viewModelScope.launch(Dispatchers.IO){
            mainRepository.getAllRunsSortedByDistance()

                .collect {
                    runDataSortedByDistance.postValue(it)
                }
        }
    }
    fun collectDataFromRepoSortedByAvgSpeed(){
        viewModelScope.launch(Dispatchers.IO){
            mainRepository.getAllRunsSortedByAvgSpeed()

                .collect {
                    runDataSortedByAvgSpeed.postValue(it)
                }
        }
    }
    fun collectDataFromRepoSortedByCaloriesBurned(){
        viewModelScope.launch(Dispatchers.IO){
            mainRepository.getAllRunsSortedByCaloriesBurned()

                .collect {
                    runDataSortedByCaloriesBurned.postValue(it)
                }
        }
    }
    fun collectDataFromRepoSortedByTimeMillis(){
        viewModelScope.launch(Dispatchers.IO){
            mainRepository.getAllRunsSortedByTimeInMillis()

                .collect {
                    runDataSortedByTimeMillis.postValue(it)
                }
        }
    }
    val runs = MediatorLiveData<MutableList<RunData>>()
    var sortType = SortType.DATE
    init {
        runs.addSource(liveDataSortedByDate) { result ->
            if(sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runDataSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runDataSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runDataSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runDataSortedByTimeMillis) { result ->
            if(sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> liveDataSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runDataSortedByTimeMillis.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runDataSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.DISTANCE -> runDataSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runDataSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }


}



