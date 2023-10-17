package com.example.videorrgb.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
fun generateFileName(prefix: String): String{
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    return "${prefix}_${timeStamp}_"
}

fun createVideoFile(
    context: Context
): File? {
    val fileName = generateFileName("VIDEO")
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
    return File.createTempFile(
        fileName,  /* prefix */
        ".mp4",  /* suffix */
        storageDir /* directory */
    )
}

fun generateTargetVideoCreated(): String{
    val filePath = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_MOVIES
    ).absolutePath
    val fileName = generateFileName("VIDEO")

    return "${filePath}/${fileName}.mp4"
}