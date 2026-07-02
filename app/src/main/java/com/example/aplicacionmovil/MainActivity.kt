package com.example.aplicacionmovil

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var lblTexto: TextView
    private val API_URL = "https://appmovil-2gf6.onrender.com/guardar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lblTexto = findViewById(R.id.lblTexto)
        lblTexto.text = "Esperando datos del Reloj..."
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/sensores") {
            val dato = String(messageEvent.data, StandardCharsets.UTF_8)
            val partes = dato.split(":")
            val tipo = partes[0]
            val valor = partes[1]

            runOnUiThread {
                lblTexto.text = "Recibido: $tipo -> $valor"
            }

            // GUARDAR AUTOMÁTICAMENTE EN LA BD
            val json = """
                {
                    "usuario": "Reloj_$tipo",
                    "mensaje": "$valor",
                    "fecha": "${System.currentTimeMillis()}"
                }
            """.trimIndent()
            
            post(API_URL, json)
        }
    }

    fun post(url: String, jsonBody: String) {
        val client = OkHttpClient()
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonBody.toRequestBody(JSON)
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BD", "Error al guardar: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.close()
                Log.d("BD", "Dato guardado en la nube")
            }
        })
    }
}