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
package com.huawei.audioeditorkitcustomdemo

import android.app.Application
import com.huawei.audioeditorkitcustomdemo.model.TranscriptedWord
import com.huawei.hms.audioeditor.common.agc.HAEApplication
import com.huawei.hms.mlsdk.common.MLApplication

class App : Application() {

    companion object {
        var transcribedFile: List<TranscriptedWord>? = null
    }

    override fun onCreate() {
        super.onCreate()
        val apiKey =
            "DAEDAFADo68XzCZrz1plTfy/W+ET070zjFmv2lYxPzMe+ilmTmWzccnNBexGrYVqxlstPM9LG/HghVkmxXfXzdgGjuToTlUvpEcuGw=="
        HAEApplication.getInstance().apiKey = apiKey
        MLApplication.getInstance().apiKey = apiKey
    }
}