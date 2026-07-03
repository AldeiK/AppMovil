const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const app = express();

app.use(cors());
app.use(express.json());

const mongoURI = process.env.MONGO_URI;

mongoose.connect(mongoURI, { dbName: 'JoseRodolfoDB' })
    .then(() => console.log("✅ BD CONECTADA - MODO TRIPLE SENSOR"))
    .catch(err => console.error("❌ ERROR MONGO:", err));

// Esquema para los 3 sensores juntos
const TripleSensorSchema = new mongoose.Schema({
    ritmo: String,
    movimiento: String,
    luz: String,
    dispositivo: String,
    fecha: String
});

const Reporte = mongoose.model('Reporte', TripleSensorSchema);

app.post('/guardar', async (req, res) => {
    try {
        console.log("📥 REPORTE COMPLETO RECIBIDO:", req.body);
        const nuevoReporte = new Reporte(req.body);
        await nuevoReporte.save();
        res.status(201).send({ status: "success" });
    } catch (error) {
        res.status(500).send({ status: "error" });
    }
});

app.get('/', (req, res) => { res.send("Servidor de Reportes Triples Activo"); });

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`🚀 Puerto ${PORT}`));
