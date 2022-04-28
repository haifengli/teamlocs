package com.yellowtree.teamlocs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yellowtree.teamlocs.databinding.ActivityMapsBinding
import com.yellowtree.teamlocs.model.Coordinate
import com.yellowtree.teamlocs.util.Resource
import com.yellowtree.teamlocs.util.Status

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: TeamViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var outdatedLocations : List<LiveData<Resource<Coordinate>>>? = null


    private val coordinateObserver = Observer<Resource<Coordinate>> {
        if (it.status == Status.SUCCESS) {
            val coord = it.data
            coord?.apply {
                Log.d("Test", "candidate coordidate x: $coord.x; y: $coord.y ")
                updateMarker(this)
            }


        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel.teamsObservable.observe(this) { resource ->
            Log.d("Test", "resource size ${resource.data?.size ?: 0}")
            if (resource.status == Status.SUCCESS) {
                val teams = resource.data
                teams?.let {
                    viewModel.setTeam(it[0])
                }
            }
        }


        viewModel.teamLocationObservable.observe(this) { currentLocations ->
            outdatedLocations?.let {
                for(outdatedLocation in it) {
                    outdatedLocation.removeObservers(this)
                }
            }
            for(location in currentLocations) {
                location.observe(this, coordinateObserver)
            }
            outdatedLocations = currentLocations
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun updateMarker(cord: Coordinate) {
        val location = LatLng(cord.y, cord.x)
        mMap.addMarker(MarkerOptions().position(location).title("New Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }
}