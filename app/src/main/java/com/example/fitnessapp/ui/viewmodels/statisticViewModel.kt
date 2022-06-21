package com.example.fitnessapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.db.RunData
import com.example.fitnessapp.repositries.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {
    val liveDataTotalTimeInMillis : MutableLiveData<Long> = MutableLiveData()
    val liveDataTotalAvgSpeed : MutableLiveData<Float> = MutableLiveData()
    val liveDataTotalCaloriesBurned : MutableLiveData<Int> = MutableLiveData()
    val liveDataTotalDistance :MutableLiveData<Int> = MutableLiveData()
    val liveDataSortedByDate : MutableLiveData<MutableList<RunData>> = MutableLiveData(mutableListOf())

    fun getTotalTimeInMillies(){
        viewModelScope.launch {
            mainRepository.getTotalTimeInMillis().onStart {  }
                .catch {  }
                .collect {
                    liveDataTotalTimeInMillis.postValue(it)
                }
        }
    }
    fun getTotalAvgSpeed(){
        viewModelScope.launch {
            mainRepository.getTotalAvgSpeed().onStart {  }
                .catch {  }
                .collect {
                    liveDataTotalAvgSpeed.postValue(it)
                }
        }
    }
    fun getTotalCaloriesBurned(){
        viewModelScope.launch {
            mainRepository.getTotalCaloriesBurned().onStart {  }
                .catch {  }
                .collect {
                    liveDataTotalCaloriesBurned.postValue(it)
                }
        }
    }
    fun getTotalTotalDistance(){
        viewModelScope.launch {
            mainRepository.getTotalDistance().onStart {  }
                .catch {  }
                .collect {
                  liveDataTotalDistance.postValue(it)
                }
        }
    }
    fun collectDataFromRepoSortedByDate(){
        viewModelScope.launch(Dispatchers.IO){
            mainRepository.getAllRunsSortedByDate()
                .collect {
                    liveDataSortedByDate.postValue(it)

                }
        }
    }
}