package com.yellowtree.teamlocs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yellowtree.teamlocs.repo.GeocodeServiceAPIRepo
import com.yellowtree.teamlocs.repo.TeamServiceAPIRepo

class TeamViewModel(app: Application): AndroidViewModel(app) {

    val teamsObservable = TeamServiceAPIRepo.getInstance().fetchTeams(app)

    val testCandidateObservable = GeocodeServiceAPIRepo.getInstance().geocode(app, "Irvine, CA, US")
}