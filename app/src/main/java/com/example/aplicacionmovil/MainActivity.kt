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
    private val client = OkHttpClient()

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
        if (messageEvent.path == "/sensores_triples") {
            val dato = String(messageEvent.data, StandardCharsets.UTF_8)
            val partes = dato.split(":")
            if (partes.size < 3) return
            
            val ritmo = partes[0]
            val mov = partes[1]
            val luz = partes[2]

            runOnUiThread {
                lblRitmo.text = "❤️ Ritmo: $ritmo BPM"
                lblMovimiento.text = "⌚ Mov: $mov m/s²"
                lblLuz.text = "💡 Luz: $luz lx"
                lblStatus.text = "Enviando Reporte Triple..."
            }

            val json = """
                {
                    "ritmo": "$ritmo",
                    "movimiento": "$mov",
                    "luz": "$luz",
                    "dispositivo": "Reloj_JoseRodolfo",
                    "fecha": "${System.currentTimeMillis()}"
                }
            """.trimIndent()
            
            post(API_URL, json)
        }
    }

    fun post(url: String, jsonBody: String) {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonBody.toRequestBody(JSON)
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", e.message ?: "Error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        lblStatus.text = "✅ Reporte guardado en MongoDB"
                        enviarConfirmacionAReloj()
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