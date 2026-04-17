package com.dbarragan.comandafacil.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.dbarragan.comandafacil.R
import com.dbarragan.comandafacil.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivLogo      = findViewById<ImageView>(R.id.ivSplashLogo)
        val tvNombre    = findViewById<TextView>(R.id.tvSplashNombre)
        val tvSlogan    = findViewById<TextView>(R.id.tvSplashSlogan)

        ivLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_logo_in))
        tvNombre.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_text_in))
        tvSlogan.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_subtitle_in))

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3500)
    }
}
