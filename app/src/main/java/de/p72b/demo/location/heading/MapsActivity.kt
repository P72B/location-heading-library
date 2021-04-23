package de.p72b.demo.location.heading

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import de.p72b.locator.location.ILastLocationListener
import de.p72b.locator.location.LocationAwareAppCompatActivity
import de.p72b.maps.location.heading.HeadingMarker
import de.p72b.maps.location.heading.IndicatorListener
import de.p72b.maps.location.heading.LocationHeadingVisualizer

class MapsActivity : LocationAwareAppCompatActivity(), OnMapReadyCallback, IndicatorListener {

    private lateinit var mMap: GoogleMap
    private lateinit var locationHeadingVisualizer: LocationHeadingVisualizer

    private var locationMarker: Marker? = null
    private var rotationMarker: Marker? = null
    private var locationAccuracyCircle: Circle? = null
    private val default = LatLng(0.0, 0.0)

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        locationHeadingVisualizer = LocationHeadingVisualizer(this, this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        locationManager.getLastLocation(object : ILastLocationListener {
            override fun onError(code: Int, message: String?) {
                Toast.makeText(this@MapsActivity, message, Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(location: Location?) {
                initDefault()
                location?.let {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 17.0f))
                }
            }
        })
    }

    override fun onUpdate(headingRotationMarker: HeadingMarker?) {
        if (headingRotationMarker == null) return
        val latLng = LatLng(headingRotationMarker.center.latitude, headingRotationMarker.center.longitude)

        locationMarker?.apply {
            position = latLng
        }
        rotationMarker?.apply {
            position = latLng
            headingRotationMarker.rotation?.let {
                rotation = it
            }
        }
        locationAccuracyCircle?.apply {
            center = latLng
            radius = headingRotationMarker.radius
        }
    }

    override fun onRotationUpdate(headingRotationMarker: HeadingMarker?) {
        if (headingRotationMarker == null) return
        rotationMarker?.apply {
            headingRotationMarker.rotation?.let {
                rotation = it
            }
        }
    }

    private fun initDefault() {
        val indicatorColor: Int = ContextCompat.getColor(this, R.color.ws_blue)
        val locationIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_indicator)
        val locationHeadingIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_heading_indicator)
        locationMarker = mMap.addMarker(
            MarkerOptions()
                .position(default)
                .icon(locationIcon)
                .anchor(0.5f, 0.5f)
                .flat(true)
        ).apply {
            zIndex = Float.MAX_VALUE - 5
        }
        rotationMarker = mMap.addMarker(
            MarkerOptions()
                .position(default)
                .icon(locationHeadingIcon)
                .flat(true)
        ).apply {
            zIndex = 0.0f
        }
        locationAccuracyCircle = mMap.addCircle(CircleOptions().apply {
            strokeColor(ColorUtils.setAlphaComponent(indicatorColor, 80))
            strokeWidth(2f)
            fillColor(ColorUtils.setAlphaComponent(indicatorColor, 25))
            center(default)
            radius(0.0)
        })
        locationAccuracyCircle?.zIndex = 0f
    }
}