package com.example.aplicacionmovil

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var txtNombre: EditText
    private lateinit var btnEnviar: Button
    private lateinit var btnGuardarBD: Button
    private lateinit var lblTexto: TextView
    private val CHAT_PATH = "/chat"

    // URL de tu API (Cámbiala por la de Render cuando la tengas)
    private val API_URL = "https://tu-api-en-render.com/guardar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtNombre = findViewById(R.id.txtNombre)
        btnEnviar = findViewById(R.id.btnEnviar)
        btnGuardarBD = findViewById(R.id.btnGuardarBD)
        lblTexto = findViewById(R.id.lblTexto)

        // Botón para el Reloj
        btnEnviar.setOnClickListener {
            val texto = txtNombre.text.toString()
            if (texto.isNotEmpty()) {
                enviarMensajeAReloj(texto)
                txtNombre.text.clear()
            }
        }

        // Botón para Guardar en BD (Tarea API POST)
        btnGuardarBD.setOnClickListener {
            val texto = txtNombre.text.toString()
            if (texto.isNotEmpty()) {
                // Creamos un JSON para enviar
                val json = """
                    {
                        "usuario": "Celular",
                        "mensaje": "$texto",
                        "fecha": "${System.currentTimeMillis()}"
                    }
                """.trimIndent()
                
                post(API_URL, json)
                lblTexto.text = "Guardando en BD..."
                txtNombre.text.clear()
            }
        }
        
        // Probar GET al iniciar
        get("https://jsonplaceholder.typicode.com/todos/1")
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    // --- CÓDIGO DIAPOSITIVA 34 (GET) ---
    fun get(url: String) {
        // Crear un cliente de OkHttp
        val client = OkHttpClient()

        // Construir la petición
        val request = Request.Builder()
            .url(url)
            .build()

        // Ejecutar la petición en un hilo aparte
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejo de error
                Log.d("FETCH", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.d("FETCH", "Error en la respuesta: ${response.code}")
                    } else {
                        // Aquí se maneja la respuesta, por ejemplo, convertirla en String
                        val responseData = response.body?.string()
                        Log.d("FETCH", "Respuesta GET: $responseData")
                    }
                }
            }
        })
    }

    // --- CÓDIGO DIAPOSITIVA 35 (POST) ---
    fun post(url: String, jsonBody: String) {
        val client = OkHttpClient()
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonBody.toRequestBody(JSON)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("FETCH", "Error POST: ${e.message}")
                runOnUiThread {
                    lblTexto.text = "Error: No se pudo conectar"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseData = response.body?.string()
                    Log.d("FETCH", "Respuesta POST: $responseData")
                    runOnUiThread {
                        if (response.isSuccessful) {
                            lblTexto.text = "¡Guardado con éxito!"
                        } else {
                            lblTexto.text = "Error API: ${response.code}"
                        }
                    }
                }
            }
        })
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