const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const app = express();

app.use(cors());
app.use(express.json());

const mongoURI = process.env.MONGO_URI;

mongoose.connect(mongoURI, { dbName: 'JoseRodolfoDB' })
    .then(() => console.log("✅ CONECTADO A MONGODB - BD: JoseRodolfoDB"))
    .catch(err => console.error("❌ ERROR MONGO:", err));

// Esquema específico para SENSORES
const SensorSchema = new mongoose.Schema({
    sensor: String,
    valor: String,
    unidad: String,
    dispositivo: String,
    fecha: String
});

const Lectura = mongoose.model('Lectura', SensorSchema);

app.post('/guardar', async (req, res) => {
    try {
        console.log("📥 DATO DE SENSOR RECIBIDO:", req.body);
        const nuevaLectura = new Lectura(req.body);
        const guardado = await nuevaLectura.save();
        res.status(201).send({ status: "success", id: guardado._id });
    } catch (error) {
        console.error("❌ ERROR AL GUARDAR SENSOR:", error);
        res.status(500).send({ status: "error" });
    }
});

app.get('/', (req, res) => { res.send("Servidor de Sensores Activo"); });

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`🚀 Puerto ${PORT}`));
