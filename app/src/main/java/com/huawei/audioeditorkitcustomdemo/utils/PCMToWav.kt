/*
  Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  You may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.huawei.audioeditorkitcustomdemo.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import com.huawei.hms.audioeditor.sdk.util.SmartLog
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object PCMToWav {
    private val TAG = PCMToWav::class.java.simpleName

    // Transformation function.
    fun convertWaveFile(
        fileNamePcm: String?,
        fileNameWav: String?,
        mediaRate: Int,
        mediaChannel: Int,
        mediaFormat: Int
    ): String? {
        // Cached data size.
        val bufferSize = AudioRecord.getMinBufferSize(mediaRate, mediaChannel, mediaFormat) * 2
        FileUtils.deleteFile(fileNameWav)
        var pcm: FileInputStream? = null
        var wav: FileOutputStream? = null
        try {
            val data = ByteArray(bufferSize)
            pcm = FileInputStream(fileNamePcm)
            wav = FileOutputStream(fileNameWav)
            val totalAudioLen = pcm.channel.size()
            val totalDataLen = totalAudioLen + 36
            val channels = 1
            val byteRate: Long
            byteRate = if (mediaFormat == AudioFormat.ENCODING_PCM_16BIT) {
                (16 * mediaRate * channels / 8).toLong()
            } else if (mediaFormat == AudioFormat.ENCODING_PCM_8BIT) {
                (8 * mediaRate * channels / 8).toLong()
            } else {
                Log.e(
                    TAG,
                    "mediaFormat is neither AudioFormat.ENCODING_PCM_16BIT nor AudioFormat.ENCODING_PCM_8BIT, convert failed."
                )
                return ""
            }
            addWaveHeader(
                wav,
                totalAudioLen,
                totalDataLen,
                mediaRate.toLong(),
                channels,
                byteRate,
                mediaChannel,
                mediaFormat
            )
            while (pcm.read(data) != -1) {
                wav.write(data)
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, e.message.toString())
        } catch (e: IOException) {
            Log.e(TAG, e.message.toString())
        } finally {
            try {
                pcm?.close()
                wav?.close()
            } catch (e: IOException) {
                SmartLog.e(TAG, e.message)
            }
        }
        FileUtils.deleteFile(fileNamePcm)
        return fileNameWav
    }

    // Adding the WAV header information.
    @Throws(IOException::class)
    private fun addWaveHeader(
        out: FileOutputStream, totalAudioLen: Long, totalDataLen: Long, longSampleRate: Long,
        channels: Int, byteRate: Long, mediaChannel: Int, mediaFormat: Int
    ) {
        val header = ByteArray(44)
        // Add RIFF header.
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        // Data Size.
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte()
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        // Fmt data size.
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // Encoding mode.
        header[20] = 1
        header[21] = 0
        // Channel.
        header[22] = channels.toByte()
        header[23] = 0
        // Sampling rate.
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        // Audio data transmission rate, which is calculated as follows: Sampling rate x channels x Sampling depth/8
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        var tongdaowei = 1
        tongdaowei = if (mediaChannel == AudioFormat.CHANNEL_IN_MONO) {
            1
        } else {
            2
        }
        if (mediaFormat == AudioFormat.ENCODING_PCM_16BIT) {
            header[32] = (tongdaowei * 16 / 8).toByte()
        } else if (mediaFormat == AudioFormat.ENCODING_PCM_8BIT) {
            header[32] = (tongdaowei * 8 / 8).toByte()
        }
        header[33] = 0
        if (mediaFormat == AudioFormat.ENCODING_PCM_16BIT) {
            header[34] = 16
        } else if (mediaFormat == AudioFormat.ENCODING_PCM_8BIT) {
            header[34] = 8
        }
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }
}