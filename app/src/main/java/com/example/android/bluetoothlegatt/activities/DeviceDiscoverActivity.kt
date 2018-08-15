package com.example.android.bluetoothlegatt.activities

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import com.example.android.bluetoothlegatt.R
import timber.log.Timber

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 * Once the thermal divider device is found continue to the control activity.
 */
class DeviceDiscoverActivity : AppCompatActivity(), BluetoothAdapter.LeScanCallback {

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private var mScanning: Boolean = false
    private var mHandler: Handler? = null

    private var loadingLayout: LinearLayout? = null

    override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
        if (device != null) {
            if (device.name != null && device.name == THERMAL_DIVIDER_DEVICE_NAME) {
                // We have found a valid device.
                val intent = Intent(applicationContext, DeviceControlActivity::class.java)
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.name)
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.address)
                if (mScanning) {
                    stopBluetoothScan()
                }
                Toast.makeText(this, "Connected: Started Monitoring", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            } else {
                Timber.d("Detected nonmatching BLE device: %s", device.name)
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_device_discover)
        if (actionBar != null) {
            actionBar!!.setTitle(R.string.title_devices)
        }

        loadingLayout = findViewById(R.id.loadingLayout)

        mHandler = Handler()

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            return
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "App requires bluetooth adapter on device", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Prompt for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_RESPONSE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_discover, menu)
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).isVisible = false
            menu.findItem(R.id.menu_scan).isVisible = true
            menu.findItem(R.id.menu_refresh).actionView = null
        } else {
            menu.findItem(R.id.menu_stop).isVisible = true
            menu.findItem(R.id.menu_scan).isVisible = false
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_scan -> scanLeDevice(true)
            R.id.menu_stop -> scanLeDevice(false)
        }
        return true
    }

    override fun onResume() {
        super.onResume()

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        scanLeDevice(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        if (data == null || (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED)) {
            Toast.makeText(this, getString(R.string.bluetooth_required), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        scanLeDevice(false)
    }

    private fun scanLeDevice(enable: Boolean) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed({
                stopBluetoothScan()
                invalidateOptionsMenu()
            }, SCAN_PERIOD)

            startBluetoothScan()
        } else {
            stopBluetoothScan()
        }
        invalidateOptionsMenu()
    }

    fun startBluetoothScan() {
        loadingLayout?.visibility = View.VISIBLE
        mScanning = true
        mBluetoothAdapter.startLeScan(this)
    }

    fun stopBluetoothScan() {
        loadingLayout?.visibility = View.INVISIBLE
        mScanning = false
        mBluetoothAdapter.stopLeScan(this)
        // TODO: remove
//        val intent = Intent(applicationContext, DeviceControlActivity::class.java)
//        startActivity(intent)
    }

    companion object {

        private const val REQUEST_ENABLE_BT = 1
        // Stops scanning after 10 seconds.
        private const val SCAN_PERIOD: Long = 10000

        private const val PERMISSION_RESPONSE = 42

        const val THERMAL_DIVIDER_DEVICE_NAME = "LUNCHBOX"
    }
}