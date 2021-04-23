package de.p72b.maps.location.heading

data class HeadingMarker(
    var center: Position,
    var rotation: Float? = null,
    var radius: Double
)

data class Position(
    var latitude: Double,
    val longitude: Double
)