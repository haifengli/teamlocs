package com.yellowtree.teamlocs.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.yellowtree.teamlocs.api.GeoCodeService
import com.yellowtree.teamlocs.model.Coordinate
import com.yellowtree.teamlocs.model.GeoCodeResult
import com.yellowtree.teamlocs.util.Resource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

class GeocodeServiceAPIRepo {
    private val addressMap = hashMapOf<String, Coordinate>()

    companion object {
        @Volatile
        private var INSTANCE: GeocodeServiceAPIRepo? = null
        fun getInstance() = INSTANCE ?: synchronized(this) {
            INSTANCE ?: GeocodeServiceAPIRepo().apply {
                INSTANCE = this
            }
        }

    }

    fun geocode(context: Context, address: String) : LiveData<Resource<Coordinate>> {
        return object : LiveData<Resource<Coordinate>>() {
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
                    Callback<GeoCodeResult>{

                        override fun onResponse(
                            call: Call<GeoCodeResult>,
                            response: Response<GeoCodeResult>
                        ) {
                            if (response.isSuccessful) {
                                val geocodeResult = response.body()
                                val candidateSize = geocodeResult?.candidates?.size ?: 0
                                if (candidateSize > 0) {
                                    addressMap[address] = geocodeResult?.candidates!![0].location
                                    postValue(Resource.success(addressMap[address]))
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
        }
    }
}