package com.fitpulse.app.data.sensor.motion

class PeakValleyDetector(
    private val minPeakDistanceMs: Long = 600L,
    private val peakThreshold: Float = 2.0f,
    private val valleyThreshold: Float = -1.5f
) {
    private var lastPeakTimestamp = 0L
    private var lastValleyTimestamp = 0L
    private var searchingForPeak = true

    fun process(value: Float, timestampMs: Long): DetectionType {
        if (searchingForPeak) {
            if (value > peakThreshold && (timestampMs - lastPeakTimestamp) > minPeakDistanceMs) {
                lastPeakTimestamp = timestampMs
                searchingForPeak = false
                return DetectionType.PEAK
            }
        } else {
            if (value < valleyThreshold && (timestampMs - lastValleyTimestamp) > (minPeakDistanceMs / 2)) {
                lastValleyTimestamp = timestampMs
                searchingForPeak = true
                return DetectionType.VALLEY
            }
        }
        return DetectionType.NONE
    }

    fun reset() {
        lastPeakTimestamp = 0L
        lastValleyTimestamp = 0L
        searchingForPeak = true
    }

    enum class DetectionType {
        NONE,
        PEAK,
        VALLEY
    }
}
