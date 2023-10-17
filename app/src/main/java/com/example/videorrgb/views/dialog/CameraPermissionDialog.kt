package com.example.videorrgb.views.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.videorrgb.databinding.DialogCameraPermissionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CameraPermissionDialog(
    private val listener: ICameraPermissionDialog
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogCameraPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DialogCameraPermissionBinding.inflate(layoutInflater).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOpenSetting.setOnClickListener {
            dismiss()
            listener.onActionOpenSettingApp()
        }
    }
}

interface ICameraPermissionDialog{

    fun onActionOpenSettingApp()
}