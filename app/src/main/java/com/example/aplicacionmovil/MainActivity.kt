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

    private lateinit var lblRitmo: TextView
    private lateinit var lblMovimiento: TextView
    private lateinit var lblLuz: TextView
    private lateinit var lblStatus: TextView
    
    private val API_URL = "https://appmovil-2gf6.onrender.com/guardar"
    
    // Instancia única de OkHttp para no saturar el sistema
    private val client = OkHttpClient()
    private var ultimoEnvio: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        lblRitmo = findViewById(R.id.lblRitmo)
        lblMovimiento = findViewById(R.id.lblMovimiento)
        lblLuz = findViewById(R.id.lblLuz)
        lblStatus = findViewById(R.id.lblStatus)
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
                when (tipo) {
                    "Corazon" -> lblRitmo.text = "❤️ Ritmo: ${valor.toFloat().toInt()} BPM"
                    "Movimiento" -> lblMovimiento.text = "⌚ Mov: ${String.format("%.2f", valor.toFloat())}"
                    "Luz" -> lblLuz.text = "💡 Luz: ${valor.toFloat().toInt()} lx"
                }
                // Solo mostrar "Enviando" si no hay un error previo bloqueando la pantalla
                if (!lblStatus.text.startsWith("❌")) {
                    lblStatus.text = "Enviando $tipo a MongoDB..."
                }
            }

            // FILTRO: Solo enviar a MongoDB cada 3 segundos para no saturar
            val ahora = System.currentTimeMillis()
            if (ahora - ultimoEnvio > 3000) {
                ultimoEnvio = ahora
                
                // Determinamos la unidad de medida según el sensor
                val unidad = when(tipo) {
                    "Corazon" -> "BPM"
                    "Movimiento" -> "m/s²"
                    "Luz" -> "lx"
                    else -> ""
                }

                val json = """
                    {
                        "sensor": "$tipo",
                        "valor": "$valor",
                        "unidad": "$unidad",
                        "dispositivo": "Reloj_JoseRodolfo",
                        "fecha": "$ahora"
                    }
                """.trimIndent()

                post(API_URL, json)
            }
        }
    }

    fun post(url: String, jsonBody: String) {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonBody.toRequestBody(JSON)
        
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR_BD", "Fallo total: ${e.message}")
                runOnUiThread {
                    lblStatus.text = "❌ Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("EXITO_BD", "Guardado en la nube")
                    runOnUiThread {
                        lblStatus.text = "✅ Sincronizado con MongoDB"
                        enviarConfirmacionAReloj()
                    }
                } else {
                    Log.e("ERROR_BD", "Error servidor: ${response.code}")
                    runOnUiThread {
                        lblStatus.text = "⚠️ Error Servidor: ${response.code}"
                    }
                }
                response.close()
            }
        })
    }

    private fun enviarConfirmacionAReloj() {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(node.id, "/status", "OK".toByteArray())
            }
        }
    }
}