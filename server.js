const express = require('express');
const cors = require('cors');
const { MsEdgeTTS, OUTPUT_FORMAT } = require('msedge-tts');
const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static('public'));
const VOICES = { nabila: 'bn-BD-NabanitaNeural', riya: 'bn-IN-TanishaaNeural', sara: 'en-US-JennyNeural' };
async function makeAudio(text, voice, speed, pitch) {
  const tts = new MsEdgeTTS();
  await tts.setMetadata(VOICES[voice] || VOICES.nabila, OUTPUT_FORMAT.AUDIO_24KHZ_48KBITRATE_MONO_MP3);
  const rate = (speed >= 1 ? '+' : '') + Math.round((speed - 1) * 100) + '%';
  const p = (pitch >= 0 ? '+' : '') + Math.round(pitch * 5) + 'Hz';
  const { audioStream } = await tts.toStream(text.slice(0, 1000), { rate, pitch: p });
  return new Promise((resolve, reject) => {
    const chunks = [];
    audioStream.on('data', d => chunks.push(d));
    audioStream.on('error', reject);
    audioStream.on('close', () => { const b = Buffer.concat(chunks); b.length ? resolve(b) : reject(new Error('empty audio')); });
  });
}
app.post('/tts', async (req, res) => {
  try {
    const { text, voice, speed, pitch } = req.body;
    if (!text || !text.trim()) return res.status(400).json({ error: 'text required' });
    const buf = await makeAudio(text, voice, Number(speed) || 1, Number(pitch) || 0);
    res.set('Content-Type', 'audio/mpeg');
    res.send(buf);
  } catch (e) { console.error(e); res.status(500).json({ error: String(e.message || e) }); }
});
app.listen(process.env.PORT || 3000, () => console.log('Server running'));
