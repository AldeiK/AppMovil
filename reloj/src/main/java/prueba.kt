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
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets

class Prueba : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sCorazon: Sensor? = null
    private var sMovimiento: Sensor? = null
    private var sLuz: Sensor? = null

    private lateinit var txtRitmo: TextView
    private lateinit var txtMov: TextView
    private lateinit var txtLuz: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prueba)

        txtRitmo = findViewById(R.id.txtRitmo)
        txtMov = findViewById(R.id.txtMov)
        txtLuz = findViewById(R.id.txtLuz)

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
        sCorazon?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        sMovimiento?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        sLuz?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val valor = event.values[0]
        var tipoSensor = ""

        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                tipoSensor = "Corazon"
                txtRitmo.text = "❤️ Ritmo: ${valor.toInt()}"
            }
            Sensor.TYPE_ACCELEROMETER -> {
                tipoSensor = "Movimiento"
                txtMov.text = "⌚ Mov: ${String.format("%.1f", valor)}"
            }
            Sensor.TYPE_LIGHT -> {
                tipoSensor = "Luz"
                txtLuz.text = "💡 Luz: ${valor.toInt()}"
            }
        }

        // ENVIAR AL CELULAR
        enviarACelular("$tipoSensor:$valor")
    }

    private fun enviarACelular(dato: String) {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(node.id, "/sensores", dato.toByteArray(StandardCharsets.UTF_8))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}