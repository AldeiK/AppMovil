package com.example.aplicacionmovil.presentation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.aplicacionmovil.R
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets

class Prueba : Activity(), SensorEventListener, MessageClient.OnMessageReceivedListener {

    private lateinit var sensorManager: SensorManager
    private var sCorazon: Sensor? = null
    private var sMovimiento: Sensor? = null
    private var sLuz: Sensor? = null

    private lateinit var txtRitmo: TextView
    private lateinit var txtMov: TextView
    private lateinit var txtLuz: TextView
    private lateinit var txtStatus: TextView

    // Valores temporales
    private var vRitmo = "0"
    private var vMov = "0"
    private var vLuz = "0"
    private var ultimoEnvio: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prueba)

        txtRitmo = findViewById(R.id.txtRitmo)
        txtMov = findViewById(R.id.txtMov)
        txtLuz = findViewById(R.id.txtLuz)
        txtStatus = findViewById(R.id.txtStatus)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sCorazon = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        sMovimiento = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sLuz = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        pedirPermisos()
    }

    private fun pedirPermisos() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
        sCorazon?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        sMovimiento?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        sLuz?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val valor = event.values[0]

        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                vRitmo = valor.toInt().toString()
                txtRitmo.text = "❤️ Ritmo: $vRitmo"
            }
            Sensor.TYPE_ACCELEROMETER -> {
                vMov = String.format("%.1f", valor)
                txtMov.text = "⌚ Mov: $vMov"
            }
            Sensor.TYPE_LIGHT -> {
                vLuz = valor.toInt().toString()
                txtLuz.text = "💡 Luz: $vLuz"
            }
        }

        // ENVIAR LOS 3 JUNTOS cada 5 segundos
        val ahora = System.currentTimeMillis()
        if (ahora - ultimoEnvio > 5000) {
            ultimoEnvio = ahora
            enviarACelular("$vRitmo:$vMov:$vLuz")
        }
    }

    private fun enviarACelular(dato: String) {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(node.id, "/sensores_triples", dato.toByteArray(StandardCharsets.UTF_8))
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/status") {
            runOnUiThread {
                txtStatus.text = "☁️ Sincronizado"
                txtStatus.postDelayed({ txtStatus.text = "Sincronizando..." }, 2000)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}