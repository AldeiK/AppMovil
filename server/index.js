const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const app = express();

app.use(cors());
app.use(express.json());

// Forzamos la conexión a una base de datos específica llamada 'JoseRodolfoDB'
const mongoURI = process.env.MONGO_URI;

mongoose.connect(mongoURI, { dbName: 'JoseRodolfoDB' })
    .then(() => console.log("✅ CONECTADO A MONGODB - BD: JoseRodolfoDB"))
    .catch(err => console.error("❌ ERROR MONGO:", err));

const Mensaje = mongoose.model('Mensaje', {
    usuario: String,
    mensaje: String,
    fecha: String
});

app.post('/guardar', async (req, res) => {
    try {
        console.log("📥 DATO RECIBIDO:", req.body);
        const nuevo = new Mensaje(req.body);
        const guardado = await nuevo.save();
        console.log("💾 GUARDADO CON ID:", guardado._id);
        res.status(201).send({ status: "success", id: guardado._id });
    } catch (error) {
        console.error("❌ ERROR AL GUARDAR:", error);
        res.status(500).send({ status: "error" });
    }
});

app.get('/', (req, res) => { res.send("Servidor Activo"); });

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`🚀 Puerto ${PORT}`));
