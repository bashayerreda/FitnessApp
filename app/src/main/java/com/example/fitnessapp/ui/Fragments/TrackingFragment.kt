package  com.example.fitnessapp.ui.Fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fitnessapp.R
import com.example.fitnessapp.db.RunData
import com.example.fitnessapp.others.Constants.ACTION_PAUSE_SERVICE
import com.example.fitnessapp.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.fitnessapp.others.Constants.ACTION_STOP_SERVICE
import com.example.fitnessapp.others.Constants.MAP_ZOOM
import com.example.fitnessapp.others.Constants.POLYLINE_WIDTH
import com.example.fitnessapp.others.TrackingUtility
import com.example.fitnessapp.services.ServiceClass
import com.example.fitnessapp.services.polyLine
import com.example.fitnessapp.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.coroutines.launch
import java.lang.Math.round
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private var map : GoogleMap? = null
    private var isTracked :Boolean = false
    private var pathDraw = mutableListOf<polyLine>()
    private var currentTimeMillis = 0L
    @set:Inject
    var weight = 55f
    var menu : Menu? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            saveInformationForRunInDb()
        }
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync() {
            map = it
            reDrawAllPoints()
        }

        subscribeToObservers()

    }
    fun saveInformationForRunInDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathDraw) {
                distanceInMeters += TrackingUtility.calculateLengthForEachLine(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (currentTimeMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run =
                RunData(bmp, dateTimestamp, avgSpeed, distanceInMeters, currentTimeMillis, caloriesBurned)
            viewModel.insertDataIntoDataBase(run)


           deleteRun()
        }
    }


    private fun subscribeToObservers() {
        ServiceClass.isTracked.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            updateStatesForUi(it)
        })
        ServiceClass.pathDraw.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
               pathDraw = it
                 drawPoints()
                 moveCameraToUser()
        })
        lifecycleScope.launch{
            ServiceClass.timeInMillis.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                currentTimeMillis = it
                val formattedTime = TrackingUtility.getFormattedStopWatchTime(it,true)
                tvTimer.text = formattedTime
            })
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu_tracking,menu)
        this.menu = menu
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Material3_DayNight_Dialog_Alert)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                deleteRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun deleteRun(){
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun toggleRun() {
        if(isTracked) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }
    private fun sendCommandToService(action: String) =
            Intent(requireContext(), ServiceClass::class.java).also {
             it.action = action
            requireContext().startService(it)
        }

    private fun updateStatesForUi(isTracked : Boolean){
        this.isTracked = isTracked
        if (!isTracked && currentTimeMillis > 0L){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }
        else if (isTracked){
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if(pathDraw.isNotEmpty() && pathDraw.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathDraw.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }
    private fun reDrawAllPoints(){
        for (i in pathDraw){
            val polyLinesOptions = PolylineOptions()
                .color(Color.BLUE)
                .width(POLYLINE_WIDTH)
                .addAll(i)
            map?.addPolyline(polyLinesOptions)
        }
    }
    private fun drawPoints(){
        if (pathDraw.isNotEmpty()&&pathDraw.last().size > 1){
           val preLastIndex = pathDraw.last()[pathDraw.last().size - 2]
            val lastIndex = pathDraw.last().last()
            val polyLinesOptions = PolylineOptions()
                .color(Color.BLUE)
                .width(POLYLINE_WIDTH)
                .add(preLastIndex)
                .add(lastIndex)
           map?.addPolyline(polyLinesOptions)
        }

    }
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
    //for saving in database
    //zoom to take clear screenshot
    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for(polyline in pathDraw) {
            for(pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }


}