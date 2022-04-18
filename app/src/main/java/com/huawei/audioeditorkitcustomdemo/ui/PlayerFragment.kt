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

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.huawei.audioeditorkitcustomdemo.App
import com.huawei.audioeditorkitcustomdemo.R
import com.huawei.audioeditorkitcustomdemo.model.TranscriptedWord
import com.huawei.hms.audioeditor.sdk.HuaweiAudioEditor
import com.huawei.hms.audioeditor.sdk.asset.HAEAudioAsset
import com.huawei.hms.audioeditor.sdk.lane.HAEAudioLane


class PlayerFragment : Fragment() {

    private val TAG = "PlayerFragment"
    private lateinit var playPauseButton: Button
    private lateinit var audioProgressBar: SeekBar
    private lateinit var audioDuration: TextView
    private lateinit var textInput: TextView
    private lateinit var audioSpeedMenu: Button
    private lateinit var mEditor: HuaweiAudioEditor
    private var speed: Float = 1f
    private var audioAsset: HAEAudioAsset? = null
    private var audioLane: HAEAudioLane? = null
    private var progressAudio = 0
    private var audioLength: Long = 0
    private var isAudioFinished = false
    private var onPause = false

    private var highlightedWord: TranscriptedWord? = null
    private var textToHighlight: String? = ""

    private var wordIndex = 0
    private var currentWord = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_player, container, false)
        initUI(v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        val bundle: Bundle? = arguments
        val audioPath = bundle?.getString("audioPath")
        val text = bundle?.getString("convertedText")
        textInput.text = text
        textToHighlight = text

        mEditor = HuaweiAudioEditor.create(requireContext())
        mEditor.initEnvironment()
        val mTimeLine = mEditor.timeLine
        audioLane = mTimeLine?.appendAudioLane()
        audioAsset = audioLane?.appendAudioAsset(audioPath, mTimeLine.currentTime)
        audioLength = mTimeLine.endTime

        mEditor.setPlayCallback(object : HuaweiAudioEditor.PlayCallback {
            override fun onPlayProgress(p0: Long) {
                val current = mTimeLine.currentTime
                val endTime = mTimeLine.endTime
                val leftTime = millToTime()
                progressAudio = (100 * current / endTime).toInt()
                activity?.runOnUiThread(Runnable {
                    audioDuration.text = leftTime
                    audioProgressBar.progress = progressAudio
                })
                highlightWord(current)
            }

            override fun onPlayStopped() {
                Log.i(TAG, "Audio stopped.")
            }

            override fun onPlayFinished() {
                activity?.runOnUiThread(Runnable {
                    playPauseButton.setBackgroundResource(R.drawable.play_button)
                    onPause = true
                    isAudioFinished = true
                })
            }

            override fun onPlayFailed() {
                Log.i(TAG, "Error occurred during audio play.")
            }
        })
    }

    private fun highlightWord(current: Long) {
        val transcribedFile = App.transcribedFile
        for (transcribedWord in transcribedFile!!) {
            if (transcribedWord.getEndTime() > current && current > transcribedWord.getStartTime()) {
                if (currentWord != (transcribedWord.getWord())) {
                    currentWord = transcribedWord.getWord()
                    currentWord.let {
                        val startIndex =
                            textToHighlight?.lowercase()
                                ?.indexOf(it.lowercase(), wordIndex)
                        if (startIndex != -1) {
                            val stopIndex = startIndex?.plus(it.length)
                            activity?.runOnUiThread {
                                val highlightedSpannableWord =
                                    SpannableString(textInput.text)
                                highlightedSpannableWord.setSpan(
                                    ForegroundColorSpan(Color.BLUE),
                                    startIndex!!,
                                    stopIndex!!,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                textInput.text = highlightedSpannableWord
                            }
                            wordIndex = stopIndex!!
                        }
                    }
                    break
                }
            }
        }
    }

    private fun millToTime(): String {
        val current = audioLength - (progressAudio * audioLength / 100)
        val secDuration = current / 1000
        val sec = (secDuration % 60).toInt()
        val min = secDuration / 60
        var leftTime = "00:00"
        if (sec / 10 > 0) {
            leftTime = "$min:$sec"
        } else {
            leftTime = "$min:0$sec"
        }
        return leftTime
    }

    private fun initUI(containerView: View) {
        playPauseButton = containerView.findViewById(R.id.btn_play_pause)
        audioDuration = containerView.findViewById(R.id.txt_clock_audio)
        audioSpeedMenu = containerView.findViewById(R.id.spinner_tts)
        audioProgressBar = containerView.findViewById(R.id.seekbar_tts)
        textInput = containerView.findViewById(R.id.txt_input)
    }

    private fun initListeners() {
        audioSpeedMenu.setOnClickListener {
            if (audioAsset != null && audioLane != null) {
                when (speed) {
                    1f -> {
                        speed = 1.25f;
                        audioSpeedMenu.text = "1.25x"
                    }
                    1.25f -> {
                        speed = 1.5f;
                        audioSpeedMenu.text = "1.5x"
                    }
                    1.5f -> {
                        speed = 1f;
                        audioSpeedMenu.text = "1x"
                    }
                }
                mEditor.pauseTimeLine()
                mEditor.setPlaySpeed(speed.toInt())
                audioLane?.changeAssetSpeed(audioAsset!!.index, speed, audioAsset!!.pitch)
                val continuedLine = progressAudio * mEditor.timeLine.endTime / 100
                mEditor.playTimeLine(continuedLine, mEditor.timeLine.endTime)
            }
        }

        audioProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                mEditor.pauseTimeLine()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val progress = p0?.progress
                val newStartLine = mEditor.timeLine.duration.toInt() * progress!! / 100
                mEditor.playTimeLine(newStartLine.toLong(), mEditor.timeLine.duration)
            }

        })
        playPauseButton.setOnClickListener {
            if (onPause) {
                var starTime = mEditor.timeLine.currentTime
                if (isAudioFinished) {
                    starTime = mEditor.timeLine.startTime
                    isAudioFinished = false
                }
                mEditor.playTimeLine(starTime, mEditor.timeLine.endTime)
                it.setBackgroundResource(R.drawable.pause_button)
                onPause = false
            } else {
                mEditor.pauseTimeLine()
                it.setBackgroundResource(R.drawable.play_button)
                onPause = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mEditor.pauseTimeLine()
        playPauseButton.setBackgroundResource(R.drawable.play_button)
        onPause = true
    }
}