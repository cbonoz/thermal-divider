package com.example.android.bluetoothlegatt

import java.util.HashMap

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
object SampleGattAttributes {
    private val attributes = HashMap<String, String>()

    const val LUNCHBOX_SERVICE = "19b10000-e8f2-537e-4f6c-d104768a1214"
    const val DEVICE_ON_OFF = "19b10001-e8f2-537e-4f6c-d104768a1214"
    const val TEMPERATURE_SENSOR_1 = "19b10000-e8f2-537e-4f6c-d104768a1217"
    const val TEMPERATURE_SENSOR_2 = "19b10000-e8f2-537e-4f6c-d104768a1218"

    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes.get(uuid)
        return if (name == null) defaultName else name
    }
}
