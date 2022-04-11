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

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.huawei.audioeditorkitcustomdemo.ui.DubbingFragment
import com.huawei.audioeditorkitcustomdemo.utils.ManagePermissions
import com.huawei.hms.audioeditor.common.agc.HAEApplication

class MainActivity : AppCompatActivity() {
    private val dubbingFragment: Fragment = DubbingFragment()
    private val permissionsRequestCode = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragment(dubbingFragment)

        HAEApplication.getInstance().apiKey ="Enter api key here..."
        val list = listOf<String>(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        ManagePermissions(this,list,permissionsRequestCode).checkPermissions()
    }

    private  fun setFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
    }
}