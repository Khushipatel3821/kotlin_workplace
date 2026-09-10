package com.fitpulse.app.data.sensor.filter

/**
 * 3-Axis Single-pole IIR Low-Pass Filter for gravity isolation and noise smoothing.
 * y[n] = alpha * y[n-1] + (1 - alpha) * x[n]
 */
class LowPassFilter(private val alpha: Float = 0.8f) {

    private var outputX = 0f
    private var outputY = 0f
    private var outputZ = 0f
    private var isInitialized = false

    fun filter(x: Float, y: Float, z: Float): FloatArray {
        if (!isInitialized) {
            outputX = x
            outputY = y
            outputZ = z
            isInitialized = true
        } else {
            outputX = alpha * outputX + (1f - alpha) * x
            outputY = alpha * outputY + (1f - alpha) * y
            outputZ = alpha * outputZ + (1f - alpha) * z
        }
        return floatArrayOf(outputX, outputY, outputZ)
    }

    fun reset() {
        outputX = 0f
        outputY = 0f
        outputZ = 0f
        isInitialized = false
    }
}
