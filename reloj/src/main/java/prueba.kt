package com.example.aplicacionmovil.presentation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.aplicacionmovil.R
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets

class Prueba : Activity(), SensorEventListener, MessageClient.OnMessageReceivedListener {

    // AUDIO
    private lateinit var mediaPlayer: MediaPlayer

    // TEXTO SENSOR
    private lateinit var textSensor: TextView

    // CHAT
    private lateinit var textChatRecibido: TextView
    private lateinit var editChat: EditText
    private lateinit var btnEnviarChat: Button
    private val CHAT_PATH = "/chat"

    // SENSOR
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    // SENSOR DE LUZ
    private var sensorType = Sensor.TYPE_LIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.prueba)

        // TEXTVIEW SENSOR
        textSensor = findViewById(R.id.textSensor)

        // CHAT UI
        textChatRecibido = findViewById(R.id.textChatRecibido)
        editChat = findViewById(R.id.editChat)
        btnEnviarChat = findViewById(R.id.btnEnviarChat)

        // AUDIO
        mediaPlayer = MediaPlayer.create(this, R.raw.sonido)

        // BOTONES
        val botonAudio: Button = findViewById(R.id.botonAudio)
        val volver: Button = findViewById(R.id.volver)

        // REPRODUCIR AUDIO
        botonAudio.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        }

        // ENVIAR CHAT
        btnEnviarChat.setOnClickListener {
            val mensaje = editChat.text.toString()
            if (mensaje.isNotEmpty()) {
                enviarMensajeACelular(mensaje)
                editChat.text.clear()
            }
        }

        // VOLVER
        volver.setOnClickListener {
            finish()
        }

        // SENSOR
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(sensorType)

        startSensor()
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
        sensor?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
        sensorManager.unregisterListener(this)
    }

    private fun enviarMensajeACelular(mensaje: String) {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(
                    node.id,
                    CHAT_PATH,
                    mensaje.toByteArray(StandardCharsets.UTF_8)
                ).addOnSuccessListener {
                    textChatRecibido.text = "Tú: $mensaje"
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == CHAT_PATH) {
            val mensaje = String(messageEvent.data, StandardCharsets.UTF_8)
            runOnUiThread {
                textChatRecibido.text = "Celular: $mensaje"
            }
        }
    }

    // INICIAR SENSOR
    private fun startSensor() {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BODY_SENSORS),
                1001
            )
            return
        }

        if (sensor != null) {
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    // CAMBIOS DEL SENSOR
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == sensorType) {
            val lectura = event.values[0]
            // MOSTRAR EN PANTALLA
            textSensor.text = "Luz: $lectura"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}