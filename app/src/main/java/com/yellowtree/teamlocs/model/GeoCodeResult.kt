package com.yellowtree.teamlocs.model

data class GeoCodeResult(val spatialReference: SpatialReference, val candidates: List<GeoLocation>)