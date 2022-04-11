# AIDubbing-AudioEditorKit-Player

![Kotlin](https://img.shields.io/badge/language-kotlin-blue) ![Minimum SDK Version](https://img.shields.io/badge/minSDK-21-orange) ![Android Gradle Version](https://img.shields.io/badge/androidGradleVersion-4.0.0-green) ![Gradle Version](https://img.shields.io/badge/gradleVersion-6.1.1-brown)

## App View
![final](https://user-images.githubusercontent.com/17616681/162732484-095eb025-243a-4fa4-b941-a90694ff1626.gif)

## Screenshots
![1](https://user-images.githubusercontent.com/17616681/162731844-8498a736-7cb0-4923-9e97-62270db7622a.jpg)


## :notebook_with_decorative_cover: Introduction 
AIDubbing-AudioEditorKit-Player; It is created with HMS kits for phones running with the Android-based HMS service an application where you can convert text to the audio file with AIDubbing and play/control audio file with Audio Editor Kit.

## :notebook_with_decorative_cover: About HUAWEI Audio Editor Kit
Audio Editor Kit provides a wide range of audio editing capabilities, including AI dubbing, audio source separation, spatial audio, voice changer, and sound effects. With these capabilities, the kit serves as a one-stop solution for you to develop audio-related functions in your app with ease. In this project AI dubbing feature is used to vocalize news. To discover more, visit: [Huawei Audio Editor Kit](https://developer.huawei.com/consumer/en/hms/huawei-audio-editor/)

## :information_source: Technical Information
* Project Software Language: Kotlin
* Kotlin Version: 1.3.72
* Android Studio Version: 4.0
* Gradle Version: 4.0

## :information_source: What You Will Need

**Hardware Requirements**
- A computer that can run Android Studio.
- An Android phone for debugging.

**Software Requirements**
- Android SDK package
- Android Studio 3.X
- HMS Core (APK) 4.X or later

## :information_source: Getting Started

This project uses HUAWEI services. In order to use them, you have to [create an app](https://developer.huawei.com/consumer/en/doc/distribution/app/agc-create_app) first. Before getting started, please [sign-up](https://id1.cloud.huawei.com/CAS/portal/userRegister/regbyemail.html?service=https%3A%2F%2Foauth-login1.cloud.huawei.com%2Foauth2%2Fv2%2Flogin%3Faccess_type%3Doffline%26client_id%3D6099200%26display%3Dpage%26flowID%3D6d751ab7-28c0-403c-a7a8-6fc07681a45d%26h%3D1603370512.3540%26lang%3Den-us%26redirect_uri%3Dhttps%253A%252F%252Fdeveloper.huawei.com%252Fconsumer%252Fen%252Flogin%252Fhtml%252FhandleLogin.html%26response_type%3Dcode%26scope%3Dopenid%2Bhttps%253A%252F%252Fwww.huawei.com%252Fauth%252Faccount%252Fcountry%2Bhttps%253A%252F%252Fwww.huawei.com%252Fauth%252Faccount%252Fbase.profile%26v%3D9f7b3af3ae56ae58c5cb23a5c1ff5af7d91720cea9a897be58cff23593e8c1ed&loginUrl=https%3A%2F%2Fid1.cloud.huawei.com%3A443%2FCAS%2Fportal%2FloginAuth.html&clientID=6099200&lang=en-us&display=page&loginChannel=89000060&reqClientType=89) for a HUAWEI developer account.

After creating the application, you need to [generate a signing certificate fingerprint](https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#3). Then you have to set this fingerprint to the application you created in AppGallery Connect.
- Go to "My Projects" in AppGallery Connect.
- Find your project from the project list and click the app on the project card.
- On the Project Setting page, set SHA-256 certificate fingerprint to the SHA-256 fingerprint you've generated.
![AGC-Fingerprint](https://communityfile-drcn.op.hicloud.com/FileServer/getFile/cmtyPub/011/111/111/0000000000011111111.20200511174103.08977471998788006824067329965155:50510612082412:2800:6930AD86F3F5AF6B2740EF666A56165E65A37E64FA305A30C5EFB998DA38D409.png?needInitFileName=true?needInitFileName=true?needInitFileName=true?needInitFileName=true)

## :information_source: Using the Application

- Before you run the app, make sure that you have a working internet connection since the application uses Huawei Mobile Services. 

- When the user opens the application, It will ask for storage permission. User needs to accept it to save converted file on local.

- On the first page there is a field that user can enter text, selection for speaker type and a bar to set volume of audio file to be generated.

- After user clicks convert button it leads to player page.

- On player page user can play/pause audio file and change its speed. Also there is left time indicator that show left time of audio.

## :rocket: Features 
* Convert text to the audio file.
* Play converted audio.
* Change speed of audio.
* Change type of speaker(Male/Female).

## :link: Useful Links 
* [Huawei Developers Medium Page EN](https://medium.com/huawei-developers)
* [Huawei Developers Medium Page TR](https://medium.com/huawei-developers-tr) 
* [Huawei Developers Forum](https://forums.developer.huawei.com/forumPortal/en/home)

## :information_source: Licence
Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
