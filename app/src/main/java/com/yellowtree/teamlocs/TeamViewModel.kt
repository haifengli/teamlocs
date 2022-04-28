package com.yellowtree.teamlocs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.yellowtree.teamlocs.model.Coordinate
import com.yellowtree.teamlocs.model.GeoLocation
import com.yellowtree.teamlocs.model.Team
import com.yellowtree.teamlocs.repo.GeocodeServiceAPIRepo
import com.yellowtree.teamlocs.repo.TeamServiceAPIRepo
import com.yellowtree.teamlocs.util.Resource
import com.yellowtree.teamlocs.util.Status

class TeamViewModel(app: Application) : AndroidViewModel(app) {

    val teamsObservable = TeamServiceAPIRepo.getInstance().fetchTeams(app)

    private val _curTeamObservable = MutableLiveData<Team>()

    val teamLocationObservable: LiveData<List<LiveData<Resource<Coordinate>>>> =
        _curTeamObservable.switchMap { team ->
            val resultList = mutableListOf<LiveData<Resource<Coordinate>>>()
            team.teamMembers?.let { members ->
                for (member in members) {
                    resultList.add(GeocodeServiceAPIRepo.getInstance().geocode(app, member.address))
                }
            }
            MutableLiveData(resultList)

        }
    val testCandidateObservable = GeocodeServiceAPIRepo.getInstance().geocode(app, "Irvine, CA, US")

    init {

    }


    fun setTeam(team : Team) {
        _curTeamObservable.value?.let {
            if (team.teamName == team.teamName) {
                return
            }
        }
        _curTeamObservable.postValue(team)
    }


    override fun onCleared() {
        //teamsObservable.remov
    }
}