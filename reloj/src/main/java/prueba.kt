package com.example.aplicacionmovil.presentation

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import com.example.aplicacionmovil.R

class Prueba : Activity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.prueba)

        // AUDIO
        mediaPlayer = MediaPlayer.create(this, R.raw.sonido)

        // BOTÓN AUDIO
        val botonAudio: Button = findViewById(R.id.botonAudio)

        // BOTÓN VOLVER
        val volver: Button = findViewById(R.id.volver)

        botonAudio.setOnClickListener {

            if (!mediaPlayer.isPlaying) {

                mediaPlayer.start()

            }

        }

        volver.setOnClickListener {

            finish()

        }
    }
}