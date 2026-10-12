package com.example.tts

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Joins PCM WAV files written by the speech engine into one file, with a short pause between parts. */
object WavJoiner {

    private class Pcm(val format: ByteArray, val sampleRate: Int, val channels: Int, val bitsPerSample: Int, val data: ByteArray)

    /** Returns false when a file is unreadable or the parts use different audio formats. */
    fun join(parts: List<File>, output: File, pauseMs: Int = 120): Boolean {
        val pcms = parts.map { read(it) ?: return false }
        val first = pcms.first()
        if (pcms.any { it.sampleRate != first.sampleRate || it.channels != first.channels || it.bitsPerSample != first.bitsPerSample }) {
            return false
        }

        val frameBytes = first.channels * first.bitsPerSample / 8
        val pauseBytes = (first.sampleRate.toLong() * pauseMs / 1000 * frameBytes).toInt()
        val dataSize = pcms.sumOf { it.data.size } + pauseBytes * (pcms.size - 1)

        output.outputStream().buffered().use { out ->
            val header = ByteBuffer.allocate(12 + 8 + first.format.size + 8).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray()).putInt(4 + 8 + first.format.size + 8 + dataSize).put("WAVE".toByteArray())
            header.put("fmt ".toByteArray()).putInt(first.format.size).put(first.format)
            header.put("data".toByteArray()).putInt(dataSize)
            out.write(header.array())
            val pause = ByteArray(pauseBytes)
            pcms.forEachIndexed { index, pcm ->
                if (index > 0) out.write(pause)
                out.write(pcm.data)
            }
        }
        return true
    }

    private fun read(file: File): Pcm? {
        val bytes = try { file.readBytes() } catch (_: Exception) { return null }
        if (bytes.size < 12 || String(bytes, 0, 4) != "RIFF" || String(bytes, 8, 4) != "WAVE") return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        var format: ByteArray? = null
        var data: ByteArray? = null
        var offset = 12
        while (offset + 8 <= bytes.size) {
            val id = String(bytes, offset, 4)
            // Streaming writers may leave the size unset; never read past the end of the file
            val size = buffer.getInt(offset + 4).let { if (it < 0 || offset + 8 + it > bytes.size) bytes.size - offset - 8 else it }
            when (id) {
                "fmt " -> format = bytes.copyOfRange(offset + 8, offset + 8 + size)
                "data" -> data = bytes.copyOfRange(offset + 8, offset + 8 + size)
            }
            offset += 8 + size + (size and 1)
        }
        val fmt = format ?: return null
        if (fmt.size < 16 || data == null) return null
        val fmtBuffer = ByteBuffer.wrap(fmt).order(ByteOrder.LITTLE_ENDIAN)
        if (fmtBuffer.getShort(0).toInt() != 1) return null
        return Pcm(
            format = fmt,
            channels = fmtBuffer.getShort(2).toInt(),
            sampleRate = fmtBuffer.getInt(4),
            bitsPerSample = fmtBuffer.getShort(14).toInt(),
            data = data
        )
    }
}
