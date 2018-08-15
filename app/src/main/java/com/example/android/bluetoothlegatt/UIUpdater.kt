package com.example.android.bluetoothlegatt

import android.os.Handler
import android.os.Looper


class UIUpdater {
    // Create a Handler that uses the Main Looper to run in
    private val mHandler = Handler(Looper.getMainLooper())

    private var mStatusChecker: Runnable? = null
    private var UPDATE_INTERVAL = 2000L

    /**
     * Creates an UIUpdater object, that can be used to
     * perform UIUpdates on a specified time interval.
     *
     * @param uiUpdater A runnable containing the update routine.
     */
    constructor(uiUpdater: Runnable) {
        mStatusChecker = object : Runnable {
            override fun run() {
                // Run the passed runnable
                uiUpdater.run()
                // Re-run it after the update interval
                mHandler.postDelayed(this, UPDATE_INTERVAL)
            }
        }
    }

    /**
     * The same as the default constructor, but specifying the
     * intended update interval.
     *
     * @param uiUpdater A runnable containing the update routine.
     * @param interval  The interval over which the routine
     * should run (milliseconds).
     */
    constructor(uiUpdater: Runnable, interval: Long) {
        UPDATE_INTERVAL = interval
    }

    /**
     * Starts the periodical update routine (mStatusChecker
     * adds the callback to the handler).
     */
    @Synchronized
    fun startUpdates() {
        mStatusChecker?.run()
    }

    /**
     * Stops the periodical update routine from running,
     * by removing the callback.
     */
    @Synchronized
    fun stopUpdates() {
        mHandler.removeCallbacks(mStatusChecker)
    }
}
