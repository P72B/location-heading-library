package de.p72b.maps.location.heading

class SphericalUtil {
    fun computeHeading(from: Position, to: Position): Double {
        val fromLat = Math.toRadians(from.latitude)
        val fromLng = Math.toRadians(from.longitude)
        val toLat = Math.toRadians(to.latitude)
        val toLng = Math.toRadians(to.longitude)
        val dLng = toLng - fromLng
        val heading = Math.atan2(
            Math.sin(dLng) * Math.cos(toLat),
            Math.cos(fromLat) * Math.sin(toLat) - Math.sin(fromLat) * Math.cos(toLat) * Math.cos(dLng)
        )
        return wrap(Math.toDegrees(heading), -180.0, 180.0)
    }

    private fun wrap(n: Double, min: Double, max: Double): Double {
        return if (n >= min && n < max) n else mod(n - min, max - min) + min
    }

    private fun mod(x: Double, m: Double): Double {
        return (x % m + m) % m
    }
}