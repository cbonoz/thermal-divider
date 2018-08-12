package com.example.android.bluetoothlegatt.activities

import android.content.Intent

import com.daimajia.androidanimations.library.Techniques
import com.example.android.bluetoothlegatt.R
import com.viksaa.sssplash.lib.activity.AwesomeSplash
import com.viksaa.sssplash.lib.cnst.Flags
import com.viksaa.sssplash.lib.model.ConfigSplash
import timber.log.Timber


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AwesomeSplash() {

    //DO NOT OVERRIDE onCreate()!
    //if you need to start some services do it in initSplash()!
    override fun initSplash(configSplash: ConfigSplash) {

        val duration: Int = 1000
        Timber.d("Splash duration: %s", duration)

        //Customize Circular Reveal
        configSplash.backgroundColor = R.color.md_brown_100
        configSplash.animCircularRevealDuration = duration //int ms
        configSplash.revealFlagX = Flags.REVEAL_LEFT  //or Flags.REVEAL_RIGHT
        configSplash.revealFlagY = Flags.REVEAL_TOP //or Flags.REVEAL_BOTTOM

        //Choose LOGO OR PATH; if you don't provide String value for path it's logo by default

        //Customize Logo
        configSplash.logoSplash = R.drawable.thermal_divider
        configSplash.animLogoSplashDuration = duration //int ms
        configSplash.animLogoSplashTechnique = Techniques.FadeIn //choose one form Techniques (ref: https://github.com/daimajia/AndroidViewAnimations)

        //Customize Title
        configSplash.titleSplash = getString(R.string.slogan)
        configSplash.titleTextColor = R.color.md_brown_500
        configSplash.titleTextSize = 18f //float value
        configSplash.animTitleDuration = duration
        configSplash.animTitleTechnique = Techniques.FadeInDown
        // configSplash.setTitleFont("fonts/myfont.ttf"); //provide string to your font located in assets/fonts/
    }

    override fun animationsFinished() {
        proceed()
    }

    private fun proceed() {
        val intent = Intent(this, DeviceDiscoverActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
