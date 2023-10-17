package com.example.videorrgb.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videorrgb.views.listener.OpenSettingResult
import com.example.videorrgb.views.listener.PermissionsCameraResult

fun registerPermissionsCamera(
    page: AppCompatActivity,
    listener: PermissionsCameraResult
): ActivityResultLauncher<Array<String>> {
    return page.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        listener.onFinishPermissionCamera(
            result,
            !result.containsValue(false)
        )
    }
}

fun registerOpenAppSettingResult(
    page: AppCompatActivity,
    listener: OpenSettingResult
): ActivityResultLauncher<Intent> {
    return page.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        listener.onFinishOpenSettingApp()
    }
}

fun permissionsCamera(): Array<String> = mutableListOf<String>().apply {
    add(Manifest.permission.CAMERA)
    add(Manifest.permission.RECORD_AUDIO)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}.toTypedArray()

fun isCameraPermissionsNotGranted(context: Context): Boolean {
    val checked = mutableListOf<Boolean>()
    for (item in permissionsCamera()) {
        checked.add(
            ContextCompat.checkSelfPermission(
                context, item
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    return checked.contains(false)
}

fun ActivityResultLauncher<Array<String>>.launchPermissions() {
    launch(permissionsCamera())
}

fun ActivityResultLauncher<Intent>.openSettingApp(activity: AppCompatActivity) {
    launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", activity.packageName, null)
    })
}