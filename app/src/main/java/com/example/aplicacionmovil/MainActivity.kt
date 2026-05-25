package com.example.aplicacionmovil

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Conectar elementos del XML
        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val btnEnviar = findViewById<Button>(R.id.btnEnviar)
        val lblTexto = findViewById<TextView>(R.id.lblTexto)

        // Evento del botón
        btnEnviar.setOnClickListener {

            // Obtener texto de la caja
            val texto = txtNombre.text.toString()

            // Mostrar texto en el label
            lblTexto.text = texto

            // Mostrar alerta bonita
            AlertDialog.Builder(this)
                .setTitle("Éxito")
                .setMessage("El texto fue enviado correctamente")
                .setPositiveButton("Aceptar", null)
                .show()
        }
    }
}