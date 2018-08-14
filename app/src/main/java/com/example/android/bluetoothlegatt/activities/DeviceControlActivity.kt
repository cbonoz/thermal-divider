/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt.activities

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.example.android.bluetoothlegatt.BluetoothLeService
import com.example.android.bluetoothlegatt.LineChartView
import com.example.android.bluetoothlegatt.R
import com.example.android.bluetoothlegatt.SampleGattAttributes
import com.example.android.bluetoothlegatt.models.TempRecord
import timber.log.Timber

import java.util.ArrayList
import java.util.HashMap

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with `BluetoothLeService`, which in turn interacts with the
 * Bluetooth LE API.
 */
class DeviceControlActivity : AppCompatActivity() {


    private val coldValues = ArrayList<TempRecord>()
    private val hotValues = ArrayList<TempRecord>()

    private lateinit var mConnectionState: TextView
    private lateinit var mDataField: TextView
    private lateinit var mColdTemperature: TextView
    private lateinit var mHotTemperature: TextView
    private lateinit var mActivate: Button
    private lateinit var mOverlay: ImageView

    private var mDeviceAddress: String? = null
    private var mBluetoothLeService: BluetoothLeService? = null
    private var mGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()
    private var mConnected = false
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var mOnOffCharacteristic: BluetoothGattCharacteristic? = null
    private var mActive = false

    private var delay = 1000
    private var lastRead: Int = 0

    internal val handler = Handler()

    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Timber.e("Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService!!.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mConnected = true
                updateConnectionState(R.string.connected)
                invalidateOptionsMenu()
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mConnected = false
                updateConnectionState(R.string.disconnected)
                invalidateOptionsMenu()
                clearUI()
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService!!.supportedGattServices)
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                displayTemperatureData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
            }
        }
    }

    private fun clearUI() {}

    private lateinit var tempChart: LineChartView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_device_control)

        val intent = intent
        val mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME)
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS)

        // Sets up UI references.
        mConnectionState = findViewById<TextView>(R.id.connection_state)
        mColdTemperature = findViewById<TextView>(R.id.cold_temperature)
        mHotTemperature = findViewById<TextView>(R.id.hot_temperature)
        mDataField = findViewById<TextView>(R.id.data_value)
        mActivate = findViewById<Button>(R.id.activate)
        tempChart = findViewById<LineChartView>(R.id.lineChartView)
        mOverlay = findViewById<ImageView>(R.id.lunchbox_overlay)
        mOverlay.imageAlpha = 0
        mActivate.setOnClickListener {
            if (mBluetoothLeService != null) {
                if (mOnOffCharacteristic != null) {
                    val value = ByteArray(1)
                    if (mActive) {
                        value[0] = 0x00.toByte()
                    } else {
                        value[0] = 0x11.toByte()
                    }
                    mOnOffCharacteristic!!.value = value
                    if (mBluetoothLeService!!.writeCharacteristic()) {
                        if (mActive) {
                            mActivate.setText(R.string.activate)
                            mDataField.setText(R.string.inactive)
                            mActive = false
                        } else {
                            mActivate.setText(R.string.deactivate)
                            mDataField.setText(R.string.active)
                            mActive = true
                        }
                    }
                }
            }
        }

        if (actionBar != null) {
            actionBar.setTitle(R.string.reconnect)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Timber.d("Connect request result=$result")
        }
    }

    override fun onPause() {
        super.onPause()
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
        unregisterReceiver(mGattUpdateReceiver)
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_control, menu)
        if (mConnected) {
            menu.findItem(R.id.menu_connect).isVisible = false
            menu.findItem(R.id.menu_disconnect).isVisible = true
        } else {
            menu.findItem(R.id.menu_connect).isVisible = true
            menu.findItem(R.id.menu_disconnect).isVisible = false
        }
        menu.findItem(R.id.menu_alerts).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_connect -> {
                mBluetoothLeService!!.connect(mDeviceAddress)
                return true
            }
            R.id.menu_disconnect -> {
                mBluetoothLeService!!.disconnect()
                return true
            }
            R.id.menu_alerts -> {
                showAlertDialog()
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var alertDialog: MaterialDialog? = null

    private fun showAlertDialog() {
        alertDialog = MaterialDialog.Builder(this)
                .title("Configure Alerts")
                .items("Alert if temperature changes from target by 10%", "Alert if battery below 15%", "Alert if sensors disconnect")
                .itemsCallbackMultiChoice(null) { dialog, which, text ->
                    // TODO: replace with real notificaitons post demo
                    text.map { item -> Timber.d("Selected alert: $item") }
                    true
                }
                .positiveText(R.string.activate)
                .onPositive { dialog, which ->
                    Toast.makeText(this@DeviceControlActivity, "Updated Alerts", Toast.LENGTH_SHORT).show()
                }
                .show();
    }

    private fun updateConnectionState(resourceId: Int) {
        runOnUiThread { mConnectionState.setText(resourceId) }
    }

    private fun displayTemperatureData(data: String?) {
        if (data != null) {
            val currentRead = Integer.parseInt(data.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
            if (currentRead in 0..255) {
                val difference = Math.abs(currentRead - lastRead)
                val timestamp = System.currentTimeMillis()
                val tempString = String.format("%d", currentRead)
                if (lastRead > currentRead) {
                    mOverlay.imageAlpha = difference
                    mColdTemperature.text = tempString
                    coldValues.add(TempRecord(timestamp, currentRead))
                } else {
                    mHotTemperature.text = tempString
                    hotValues.add(TempRecord(timestamp, currentRead))
                }

                tempChart.setData(coldValues, hotValues)
                lastRead = currentRead
            }
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String
        val unknownServiceString = resources.getString(R.string.unknown_service)
        val unknownCharaString = resources.getString(R.string.unknown_characteristic)
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mGattCharacteristics = ArrayList()

        // Loops through available GATT Services.
        val LIST_NAME = "NAME"
        val LIST_UUID = "UUID"
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            if (uuid == SampleGattAttributes.LUNCHBOX_SERVICE) {
                currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownServiceString)
                currentServiceData[LIST_UUID] = uuid
                gattServiceData.add(currentServiceData)

                val gattCharacteristicGroupData = ArrayList<HashMap<String, String>>()
                val gattCharacteristics = gattService.characteristics
                val charas = ArrayList<BluetoothGattCharacteristic>()

                // Loops through available Characteristics.
                for (gattCharacteristic in gattCharacteristics) {
                    charas.add(gattCharacteristic)
                    val currentCharaData = HashMap<String, String>()
                    uuid = gattCharacteristic.uuid.toString()
                    if (uuid == SampleGattAttributes.TEMPERATURE_SENSOR_1) {
                        currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownCharaString)
                        currentCharaData[LIST_UUID] = uuid
                        gattCharacteristicGroupData.add(currentCharaData)
                        updateData(gattCharacteristic)
                    }
                    if (uuid == SampleGattAttributes.TEMPERATURE_SENSOR_2) {
                        delay = 2000
                        currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownCharaString)
                        currentCharaData[LIST_UUID] = uuid
                        gattCharacteristicGroupData.add(currentCharaData)
                        updateData(gattCharacteristic)
                    }
                    if (uuid == SampleGattAttributes.DEVICE_ON_OFF) {
                        mOnOffCharacteristic = gattCharacteristic
                    }
                }
                mGattCharacteristics.add(charas)
                gattCharacteristicData.add(gattCharacteristicGroupData)
            }
        }
    }

    private fun updateData(gattCharacteristic: BluetoothGattCharacteristic) {
        val charaProp = gattCharacteristic.properties
        if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService!!.setCharacteristicNotification(mNotifyCharacteristic!!, false)
                mNotifyCharacteristic = null
            }
            mBluetoothLeService!!.readCharacteristic(gattCharacteristic)
        }
        if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
            mNotifyCharacteristic = gattCharacteristic
            mBluetoothLeService!!.setCharacteristicNotification(gattCharacteristic, true)
        }
        handler.postDelayed({ updateData(gattCharacteristic) }, delay.toLong())
    }

    companion object {
        val EXTRAS_DEVICE_NAME = "THERMAL_DIVIDER_DEVICE_NAME"
        val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"

        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }
}
