package com.example.proyectomovil

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val lblTexto = findViewById<TextView>(R.id.lblTexto)

        btnEnviar.setOnClickListener {

            // Obtener texto de la caja
            val texto = txtNombre.text.toString()

            // Pasar texto al label
            lblTexto.text = texto

            // Mostrar alerta popup
            Toast.makeText(
                this,
                "Texto enviado con éxito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}