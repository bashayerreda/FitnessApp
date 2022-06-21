package com.example.fitnessapp.ui.Fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.fitnessapp.R
import com.example.fitnessapp.others.CustomMarkerView
import com.example.fitnessapp.others.TrackingUtility
import com.example.fitnessapp.ui.StatisticViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import java.lang.Math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
        applyChangesOnBarChart()
    }
    fun applyChangesOnBarChart(){
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }
    }


    fun observeLiveData(){
        viewModel.liveDataTotalTimeInMillis.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.getTotalTimeInMillies()
        viewModel.liveDataTotalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.getTotalTotalDistance()
        viewModel.liveDataTotalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
            }
        })
        viewModel.getTotalAvgSpeed()
        viewModel.liveDataTotalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                tvTotalCalories.text = totalCalories
            }
        })
        viewModel.getTotalCaloriesBurned()
        viewModel.liveDataSortedByDate.observe(viewLifecycleOwner, Observer {
            //indices using for take range from list here using x as index of list y is avg speed
            val avgSpeed = it.indices.map { i-> BarEntry(i.toFloat(),it[i].avgSpeedInKMH)}
                val bardataSet = BarDataSet(avgSpeed, "Avg Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.white)
                }
              barChart.data = BarData(bardataSet)
            barChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
            //to see changes and update it
            barChart.invalidate()


        })
        viewModel.collectDataFromRepoSortedByDate()
    }
}