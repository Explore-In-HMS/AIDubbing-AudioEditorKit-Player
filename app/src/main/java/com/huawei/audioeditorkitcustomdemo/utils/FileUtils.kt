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

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.CursorLoader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.huawei.hms.audioeditor.sdk.util.SmartLog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

object FileUtils {
    private val TAG = "FileUtils"
    fun getFileName(fullPath: String): String {
        if (TextUtils.isEmpty(fullPath)) {
            return fullPath
        }
        val slashIndex = fullPath.lastIndexOf('/')
        return if (slashIndex == -1) {
            fullPath
        } else {
            fullPath.substring(slashIndex + 1)
        }
    }

    fun getRealPath(context: Context, fileUri: Uri): String? {
        val realPath: String?
        realPath = if (Build.VERSION.SDK_INT < 19) {
            getRealPathFromURIBelowAPI19(context, fileUri)
        } else {
            getRealPathFromURIAPI19(context, fileUri)
        }
        return realPath
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURIAPI19(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                return if ("primary".equals(type, ignoreCase = true)) {
                    if (split.size > 1) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        Environment.getExternalStorageDirectory().toString() + "/"
                    }
                } else {
                    "storage" + "/" + docId.replace(":", "/")
                }
            } else if (isDownloadsDocument(uri)) {
                val fileName = getFilePath(context, uri)
                if (fileName != null) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/Download/" + fileName
                }
                var id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    id = id.replaceFirst("raw:".toRegex(), "")
                    val file = File(id)
                    if (file.exists()) return id
                }
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURIBelowAPI19(context: Context?, contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        var result: String? = null
        val cursorLoader = CursorLoader(context, contentUri, proj, null, null, null)
        val cursor = cursorLoader.loadInBackground()
        if (cursor != null) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            cursor.close()
        }
        return result
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * Writes data stream to the file.
     * @param buffer Cache to be written
     * @param strFilePath Destination Save File Address
     * @param append Indicates whether to write in append mode.
     */
    fun writeBufferToFile(buffer: ByteArray?, strFilePath: String?, append: Boolean) {
        val file = File(strFilePath)
        var randomAccessFile: RandomAccessFile? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            if (append) {
                // Appending write mode.
                randomAccessFile = RandomAccessFile(file, "rw")
                randomAccessFile.seek(file.length())
                randomAccessFile.write(buffer)
            } else {
                // Overwrite mode.
                fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(buffer)
                fileOutputStream.flush()
            }
        } catch (e: IOException) {
            SmartLog.e(TAG, e.message)
        } finally {
            try {
                randomAccessFile?.close()
                fileOutputStream?.close()
            } catch (e: IOException) {
                SmartLog.e("Failed to close stream.", e.message)
            }
        }
    }

    /**
     * Create a directory to store the voice files generated by the AiDubbing.
     *
     * @param context context
     */
    fun initFile(context: Context): String {
        val filePath = context.getExternalFilesDir("wav")!!.path
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdirs()
            Log.i(
                "initFile",
                "Create a directory to store the voice files generated by the AIDubbing."
            )
        }
        return filePath
    }

    /**
     * Delete a file.
     * @param filePath File path
     */
    fun deleteFile(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            return
        }
        deleteFile(File(filePath))
    }

    /**
     * Delete all contents of the folder
     * @param file File
     */
    fun deleteFile(file: File?) {
        if (file != null && file.exists()) { // Check whether the file exists.
            if (file.isDirectory) { // Otherwise if it's a directory
                val files = file.listFiles() // Declares all files in the directory. files[];
                if (files != null) {
                    for (childFile in files) { // Traverse all files in the directory.
                        deleteFile(childFile) // Use this method to iterate each file.
                    }
                }
            }

            // Safely delete files
            deleteFileSafely(file)
        }
    }

    /**
     * Safely delete files. Prevents the system from reporting an error when a file is re-created after being deleted. open failed: EBUSY (Device or resource busy)
     * @param file file
     * @return true  false
     */
    fun deleteFileSafely(file: File?): Boolean {
        if (file != null) {
            val tmpPath = file.parent + File.separator + System.currentTimeMillis()
            val tmp = File(tmpPath)
            val renameTo = file.renameTo(tmp)
            if (!renameTo) {
                SmartLog.e(TAG, "deleteFileSafely file.renameTo fail!")
            }
            return tmp.delete()
        }
        return false
    }
}