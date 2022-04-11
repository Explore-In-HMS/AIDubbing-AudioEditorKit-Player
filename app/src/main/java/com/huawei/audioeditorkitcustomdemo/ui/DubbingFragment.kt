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
package com.huawei.audioeditorkitcustomdemo.ui

import android.media.AudioFormat
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Pair
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.huawei.audioeditorkitcustomdemo.R
import com.huawei.audioeditorkitcustomdemo.utils.FileUtils
import com.huawei.audioeditorkitcustomdemo.utils.PCMToWav
import com.huawei.hms.audioeditor.sdk.engine.dubbing.*
import java.io.File


class DubbingFragment : Fragment() {

    private lateinit var convertButton: Button
    private lateinit var tts_loader: ProgressBar
    private lateinit var tts_loader_background: View
    private lateinit var textInput : EditText
    private lateinit var callback: HAEAiDubbingCallback
    private lateinit var speakerRadioGroup: RadioGroup
    private var aiMode = 0
    private var runOnce: Boolean = false
    private var AUDIO_PATH: String? = null
    private  var mEngine: HAEAiDubbingEngine? = null
    private val AI_DUBBING_PATH = "aiDubbingOutput"
    private val fileName = "aidubbing"
    private val PCM_EXT = ".pcm"
    private val WAV_EXT = ".wav"
    private val language = "english"
    private var speakerSex = "Male"
    private var audioPath = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_dubbing, container, false)
        convertButton = v.findViewById(R.id.btn_convert)
        tts_loader = v.findViewById(R.id.tts_loader)
        tts_loader_background = v.findViewById(R.id.tts_loader_background)
        textInput = v.findViewById(R.id.txt_input)
        speakerRadioGroup = v.findViewById(R.id.speaker_group)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AUDIO_PATH = FileUtils.initFile(requireContext())
        mEngine = HAEAiDubbingEngine(generateConfig())
        initDubbingCallback()

        speakerRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radio_male -> speakerSex = "Male"
                R.id.radio_female -> speakerSex = "Female"
            }
        }

        convertButton.setOnClickListener {
            tts_loader_background.visibility = View.VISIBLE
            tts_loader.visibility = View.VISIBLE
            tts_loader_background.bringToFront()
            tts_loader.bringToFront()
            runOnce = true
            val s: String = textInput.text.toString()
            if (TextUtils.isEmpty(s)) {
                Toast.makeText(
                    activity,
                    "Enter some text.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                val config = generateConfig()
                initAiDubbing(config)
                val taskId: String? = mEngine?.speak(s, aiMode)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopAiDubbing()
    }

    private fun stopAiDubbing() {
        if (mEngine != null) {
            mEngine!!.stop()
            mEngine = null
        }
    }

    private fun initAiDubbing(mConfig: HAEAiDubbingConfig?) {
        var mConfig = mConfig
        if (mConfig == null) {
            mConfig = generateConfig()
        }
        if (mEngine == null) {
            mEngine = HAEAiDubbingEngine(mConfig)
        } else {
            mEngine?.updateConfig(mConfig)
        }
        // Set playback callback
        mEngine?.setAiDubbingCallback(callback)
    }

    private fun generateConfig(): HAEAiDubbingConfig {
        aiMode = HAEAiDubbingEngine.QUEUE_FLUSH or HAEAiDubbingEngine.OPEN_STREAM or HAEAiDubbingEngine.EXTERNAL_PLAYBACK
        var speakerType = 18
        val speakerList= mEngine?.getSpeakerNoRequest(language)
        if (speakerList != null) {
            for (speaker: HAEAiDubbingSpeaker in speakerList){
                if(speaker.speakerDesc == speakerSex){
                    speakerType = Integer.parseInt(speaker.name)
                }
            }
        }
        when(speakerSex){
            "Male" -> speakerType = 18
            "Female" -> speakerType = 19
        }
        val mlConfigs =
            HAEAiDubbingConfig().setVolume(120).setSpeed(100).setType(speakerType)
                .setLanguage(language)
        return mlConfigs
    }

    private fun setAudioFileName(fileName: String, fileType: String): String {
        var filePath = ""
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(fileType)) {
            return filePath
        }
        val cachePath = Environment.getExternalStorageDirectory().absolutePath
        val mFolder =
            cachePath + File.separator + AI_DUBBING_PATH
        val file = File(mFolder)
        if (!file.exists()) {
            file.mkdirs()
        }
        filePath = mFolder + File.separator + fileName + fileType
        return filePath
    }

    private fun initDubbingCallback(){
        callback = object : HAEAiDubbingCallback {
            override fun onError(taskId: String, err: HAEAiDubbingError) {
                stopAiDubbing()
            }

            override fun onWarn(taskId: String, warn: HAEAiDubbingWarn) {}
            override fun onRangeStart(taskId: String, start: Int, end: Int) {}
            override fun onAudioAvailable(
                taskId: String,
                HAEAiDubbingAudioInfo: HAEAiDubbingAudioInfo,
                i: Int,
                pair: Pair<Int, Int>,
                bundle: Bundle
            ) {

                val pcmFile = setAudioFileName(
                    fileName,
                    PCM_EXT
                )
                FileUtils.writeBufferToFile(HAEAiDubbingAudioInfo.audioData, pcmFile, true)
            }

            override fun onEvent(taskId: String, eventID: Int, bundle: Bundle?) {

                if(runOnce){
                    if (eventID == HAEAiDubbingConstants.EVENT_SYNTHESIS_COMPLETE) {
                        runOnce = false
                        val pcmFile = setAudioFileName(
                            fileName,
                            PCM_EXT
                        )
                        val waveFile = setAudioFileName(
                            fileName,
                            WAV_EXT
                        )
                        audioPath = waveFile
                        val convertWaveFile: String? = PCMToWav.convertWaveFile(
                            pcmFile,
                            waveFile,
                            16000,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT
                        )

                        activity?.runOnUiThread(Runnable {
                            tts_loader_background.visibility = View.GONE
                            tts_loader.visibility = View.GONE
                            val bundle = Bundle()
                            bundle.putString("audioPath", audioPath)
                            bundle.putString("audioFileName", "$fileName.wav")
                            val destinationFragment = PlayerFragment()
                            destinationFragment.arguments = bundle
                            fragmentManager?.beginTransaction()?.replace(R.id.fragment_container,destinationFragment)?.commit()
                        })
                    }
                }
            }

            override fun onSpeakerUpdate(
                speakerList: List<HAEAiDubbingSpeaker>,
                lanList: List<String>,
                lanDescList: List<String>
            ) {
            }
        }
    }
}