const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Conexión a MongoDB Atlas
// NOTA: La variable MONGO_URI se configura en el panel de Render
const mongoURI = process.env.MONGO_URI;

if (!mongoURI) {
    console.error("ERROR: No se ha configurado la variable MONGO_URI en el servidor.");
}

mongoose.connect(mongoURI)
    .then(() => console.log("✅ Conectado exitosamente a MongoDB Atlas"))
    .catch(err => console.error("❌ Error de conexión a MongoDB:", err));

// Esquema de la Base de Datos
const MensajeSchema = new mongoose.Schema({
    usuario: String,
    mensaje: String,
    fecha: String
});

const Mensaje = mongoose.model('Mensaje', MensajeSchema);

// Ruta para recibir datos del Celular
app.post('/guardar', async (req, res) => {
    try {
        console.log("📥 Recibido del celular:", req.body);
        const nuevoMensaje = new Mensaje(req.body);
        await nuevoMensaje.save();
        res.status(201).send({ status: "success", message: "Mensaje guardado en la nube" });
    } catch (error) {
        console.error("❌ Error al guardar:", error);
        res.status(500).send({ status: "error", message: error.message });
    }
});

// Ruta de prueba (GET)
app.get('/', (req, res) => {
    res.send("Servidor de la Clase de Aplicaciones Móviles - Operativo");
});

// Iniciar servidor
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`🚀 Servidor corriendo en el puerto ${PORT}`);
});
