package com.example.aplicacionmovil.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.aplicacionmovil.R

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val boton: Button = findViewById(R.id.boton)

        boton.setOnClickListener {

            // ALERTA
            Toast.makeText(
                this,
                "👋 Bienvenido",
                Toast.LENGTH_SHORT
            ).show()

            // CAMBIAR PANTALLA
            val intent = Intent(this, Prueba::class.java)
            startActivity(intent)

        }
    }
}