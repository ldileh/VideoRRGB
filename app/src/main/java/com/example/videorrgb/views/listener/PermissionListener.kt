package com.example.videorrgb.views.listener

interface PermissionsCameraResult {

    fun onFinishPermissionCamera(result: Map<String, Boolean>, isAllGranted: Boolean)
}

interface OpenSettingResult {

    fun onFinishOpenSettingApp()
}