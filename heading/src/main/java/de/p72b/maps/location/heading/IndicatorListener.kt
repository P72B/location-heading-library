package de.p72b.maps.location.heading

interface IndicatorListener {
    fun onUpdate(
        headingRotationMarker: HeadingMarker?
    )

    fun onRotationUpdate(
        headingRotationMarker: HeadingMarker?
    )
}