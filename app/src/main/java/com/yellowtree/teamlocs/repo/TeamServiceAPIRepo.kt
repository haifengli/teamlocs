package com.yellowtree.teamlocs.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.yellowtree.teamlocs.api.TeamService
import com.yellowtree.teamlocs.model.Team
import com.yellowtree.teamlocs.util.Resource
import retrofit2.Call
import java.util.concurrent.atomic.AtomicBoolean
import retrofit2.Callback
import retrofit2.Response

class TeamServiceAPIRepo {

    companion object {
        @Volatile
        private var INSTANCE: TeamServiceAPIRepo? = null
        fun getInstance() = INSTANCE ?: synchronized(this) {
            INSTANCE ?: TeamServiceAPIRepo().apply {
                INSTANCE = this
            }
        }
    }

    fun fetchTeams(context: Context): LiveData<Resource<List<Team>>> {
        return object : LiveData<Resource<List<Team>>>() {

            private val started = AtomicBoolean(false)

            init {
                postValue(Resource.loading(null))
            }

            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    TeamService.getInstance(context).teams.enqueue(object :
                        Callback<List<Team>> {
                        override fun onResponse(
                            call: Call<List<Team>>,
                            response: Response<List<Team>>
                        ) {
                            if (response.isSuccessful) {
                                postValue(Resource.success(response.body()))
                                return
                            }

                            val errorMsg = response.errorBody().toString()
                            postValue(Resource.error(errorMsg ?: "Unknown Error"))
                        }

                        override fun onFailure(call: Call<List<Team>>, t: Throwable) {
                            postValue(Resource.error(t.message ?: "Unknown Error"))
                        }
                    }
                    )
                }
            }
        }
    }
}
