package com.example.aplicacionmovil

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var txtNombre: EditText
    private lateinit var btnEnviar: Button
    private lateinit var lblTexto: TextView
    private val CHAT_PATH = "/chat"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Conectar elementos del XML
        txtNombre = findViewById(R.id.txtNombre)
        btnEnviar = findViewById(R.id.btnEnviar)
        lblTexto = findViewById(R.id.lblTexto)

        // Evento del botón
        btnEnviar.setOnClickListener {
            val texto = txtNombre.text.toString()
            if (texto.isNotEmpty()) {
                enviarMensajeAReloj(texto)
                txtNombre.text.clear()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    private fun enviarMensajeAReloj(mensaje: String) {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(
                    node.id,
                    CHAT_PATH,
                    mensaje.toByteArray(StandardCharsets.UTF_8)
                ).addOnSuccessListener {
                    lblTexto.text = "Tú: $mensaje"
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: com.google.android.gms.wearable.MessageEvent) {
        if (messageEvent.path == CHAT_PATH) {
            val mensaje = String(messageEvent.data, StandardCharsets.UTF_8)
            runOnUiThread {
                lblTexto.text = "Reloj: $mensaje"
            }
        }
    }
}