package com.yellowtree.teamlocs.repo

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.yellowtree.teamlocs.api.GeoCodeService
import com.yellowtree.teamlocs.api.TimeZoneService
import com.yellowtree.teamlocs.model.Coordinate
import com.yellowtree.teamlocs.model.GeoCodeResult
import com.yellowtree.teamlocs.model.GeoTimeInfo
import com.yellowtree.teamlocs.model.TimeZoneInfo
import com.yellowtree.teamlocs.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Time
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean



class GeocodeServiceAPIRepo {
    private val addressMap = hashMapOf<String, GeoTimeInfo>()
    private val scope = MainScope()

    companion object {
        @Volatile
        private var INSTANCE: GeocodeServiceAPIRepo? = null
        fun getInstance() = INSTANCE ?: synchronized(this) {
            INSTANCE ?: GeocodeServiceAPIRepo().apply {
                INSTANCE = this
            }
        }

    }




    private val queue = LinkedList<Runnable>()
    private var job : Job? = null
    private fun submitToSerialExecutor(runnable: Runnable) {
        queue.offer(runnable)
        if (job?.isActive != true) {
            job = scope.launch {
                while (!queue.isEmpty()) {
                    val nextTask = queue.poll()
                    nextTask?.run()
                    delay(1000)
                }
            }
        }
    }

    fun geocode(context: Context, address: String): LiveData<Resource<GeoTimeInfo>> {
        return object : LiveData<Resource<GeoTimeInfo>>() {
            private var started = AtomicBoolean(false)

            init {
                postValue(Resource.loading(null))
            }

            override fun onActive() {
                super.onActive()
                if (addressMap.containsKey(address)) {
                    postValue(Resource.success(addressMap[address]))
                    return
                }
                if (started.compareAndSet(false, true)) {
                    GeoCodeService.getInstance().getLocation(address).enqueue(object :
                        Callback<GeoCodeResult> {

                        override fun onResponse(
                            call: Call<GeoCodeResult>,
                            response: Response<GeoCodeResult>
                        ) {
                            if (response.isSuccessful) {
                                val geocodeResult = response.body()
                                val candidateSize = geocodeResult?.candidates?.size ?: 0
                                if (candidateSize > 0) {

                                    val loc = geocodeResult?.candidates!![0].location
                                    Log.d("Test", "send time zone request for ${loc.y} and ${loc.x}")
                                    submitToSerialExecutor {
                                        requestTimeZone(address, loc)
                                    }
                                } else {
                                    postValue(Resource.error("Unknown error"))
                                }
                                return
                            }
                            val errorMsg = response.errorBody().toString()
                            postValue(Resource.error(errorMsg))
                        }


                        override fun onFailure(call: Call<GeoCodeResult>, t: Throwable) {
                            postValue(Resource.error(t.message ?: "Unknown Error"))
                        }
                    })

                }
            }

            private fun requestTimeZone(address: String, coordinate: Coordinate) {
                TimeZoneService.getInstance().getTimeZone(coordinate.y, coordinate.x).enqueue(object :
                    Callback<TimeZoneInfo> {
                    override fun onResponse(call: Call<TimeZoneInfo>, response: Response<TimeZoneInfo>) {
                        if (response.isSuccessful) {
                            Log.d("Test", "Timezone request comes back successfully")
                            val timeZoneInfo = response.body()
                            val geoTimeInfo = GeoTimeInfo(address, coordinate, timeZoneInfo!!.abbreviation, timeZoneInfo.gmtOffset)
                            addressMap[address] = geoTimeInfo
                            postValue(Resource.success(geoTimeInfo))
                        }else {
                            val errorMsg = response.errorBody().toString()
                            postValue(Resource.error(errorMsg))
                        }
                    }

                    override fun onFailure(call: Call<TimeZoneInfo>, t: Throwable) {
                        Log.d("Test", "Timezone request failed")
                        postValue(Resource.error(t.message ?: "Unknown Error"))
                    }
                }
                )
            }



        }
    }



}