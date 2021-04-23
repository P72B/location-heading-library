package de.p72b.maps.location.heading

import android.app.Activity
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import de.p72b.locator.location.ILocationUpdatesListener
import de.p72b.locator.location.LocationManager
import de.p72b.locator.location.SettingsClientManager
import kotlin.math.abs

class LocationHeadingVisualizer(
    private val scopeActivity: Activity,
    private val listener: IndicatorListener
) : SensorEventListener, ILocationUpdatesListener, Application.ActivityLifecycleCallbacks {

    private var sphericalUtil = SphericalUtil()
    private var sensorManager: SensorManager = scopeActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val defaultSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var locationManager: LocationManager = LocationManager(scopeActivity, SettingsClientManager(scopeActivity))
    private var latestLocation: Location? = null
    private var headingMarker: HeadingMarker? = null
    private var currentBearing: Float? = null

    init {
        locationManager.subscribeToLocationChanges(this)
        scopeActivity.application.registerActivityLifecycleCallbacks(this)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        if (sensorEvent?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(16)
            val orientation = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val newBearing = (Math.toDegrees(orientation[0].toDouble())).toFloat()
            if (currentBearing == newBearing) {
                return
            }
            currentBearing = newBearing

            if (setMarkerRotation(newBearing)) {
                listener.onRotationUpdate(headingMarker)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing to do here
    }

    override fun onLocationChanged(location: Location) {
        latestLocation = location
        invalidateMarker()
    }

    override fun onLocationChangedError(code: Int, message: String?) {
        if (LocationManager.ERROR_PROVIDERS_DISABLED == code) {
            headingMarker = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // nothing to do here
    }

    override fun onActivityStarted(activity: Activity) {
        // nothing to do here
    }

    override fun onActivityResumed(activity: Activity) {
        if (scopeActivity == activity) {
            locationManager.subscribeToLocationChanges(this)
            sensorManager.registerListener(this, defaultSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (scopeActivity == activity) {
            locationManager.unSubscribeToLocationChanges(this)
            sensorManager.unregisterListener(this)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        // nothing to do here
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // nothing to do here
    }

    override fun onActivityDestroyed(activity: Activity) {
        // nothing to do here
    }

    private fun invalidateMarker() {
        val location = latestLocation ?: return

        val currentLatLng = Position(location.latitude, location.longitude)
        val accuracy = location.accuracy.toDouble()

        if (headingMarker == null) {
            headingMarker = HeadingMarker(center = currentLatLng, radius = accuracy)
        }

        headingMarker?.let { marker ->
            marker.center.let {
                if (sphericalUtil.computeHeading(currentLatLng, it) > ROTATION_MIN_OFFSET) {
                    setMarkerRotation(location.bearing)
                }
            }
        }

        headingMarker?.radius = accuracy
        headingMarker?.center = currentLatLng
        listener.onUpdate(headingMarker)
    }

    private fun setMarkerRotation(bearing: Float): Boolean {
        headingMarker?.apply {
            if (this.rotation == null) {
                rotation = bearing
                return true
            }
            this.rotation?.let {
                val delta = it.minus(bearing)
                if (abs(delta) <= ROTATION_MIN_OFFSET) {
                    return false
                }
                this.rotation = bearing
                return true
            }
        }
        return false
    }

    companion object {
        private const val ROTATION_MIN_OFFSET = 3.6F
    }
}