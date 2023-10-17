package com.example.videorrgb.views

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.daasuu.gpuv.camerarecorder.CameraRecordListener
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorder
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder
import com.daasuu.gpuv.camerarecorder.LensFacing
import com.example.videorrgb.R
import com.example.videorrgb.databinding.ActivityMainBinding
import com.example.videorrgb.model.RGBFilter
import com.example.videorrgb.utils.generateTargetVideoCreated
import com.example.videorrgb.utils.isCameraPermissionsNotGranted
import com.example.videorrgb.utils.launchPermissions
import com.example.videorrgb.utils.openSettingApp
import com.example.videorrgb.utils.registerOpenAppSettingResult
import com.example.videorrgb.utils.registerPermissionsCamera
import com.example.videorrgb.views.dialog.CameraPermissionDialog
import com.example.videorrgb.views.dialog.ICameraPermissionDialog
import com.example.videorrgb.views.listener.OpenSettingResult
import com.example.videorrgb.views.listener.PermissionsCameraResult
import com.example.videorrgb.widget.SampleCameraGLView
import timber.log.Timber
import java.io.File


class MainActivity : AppCompatActivity(),
    PermissionsCameraResult,
    ICameraPermissionDialog,
    OpenSettingResult,
    IMainActivity {

    private lateinit var binding: ActivityMainBinding

    private lateinit var resultPermissionsCamera: ActivityResultLauncher<Array<String>>
    private lateinit var resulOpenSettingApp: ActivityResultLauncher<Intent>

    private var filepath: String? = null
    private var lensFacing = LensFacing.BACK
    private var toggleClick = false

    private var cameraWidth = 1280
    private var cameraHeight = 720
    private var videoWidth = 720
    private var videoHeight = 1280

    private var sampleGLView: SampleCameraGLView? = null
    private var gpuCameraRecorder: GPUCameraRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureViews()
        initPermissions()

        resultPermissionsCamera.launchPermissions()
    }

    override fun onFinishPermissionCamera(result: Map<String, Boolean>, isAllGranted: Boolean) {
        if(!isAllGranted){
            showDialogPermissionCameraInfo()
            return
        }

        setUpCamera()
    }

    override fun onActionOpenSettingApp() {
        resulOpenSettingApp.openSettingApp(this)
    }

    override fun onFinishOpenSettingApp() {
        if(isCameraPermissionsNotGranted(this)){
            showDialogPermissionCameraInfo()
            return
        }

        setUpCamera()
    }

    override fun onResume() {
        super.onResume()
        setUpCamera()
    }

    override fun onStop() {
        super.onStop()
        releaseCamera()
    }

    override fun onSelectFilterCamera(view: View) {
        view.isSelected = !view.isSelected

        if(!view.isSelected){
            gpuCameraRecorder?.setFilter(RGBFilter.clear())
            return
        }

        gpuCameraRecorder?.setFilter(
            when(view.id){
                R.id.iv_filter_red -> RGBFilter.red()
                R.id.iv_filter_green -> RGBFilter.green()
                R.id.iv_filter_blue -> RGBFilter.blue()
                else-> RGBFilter.clear()
            }
        )

        unselectViews(view.id, listOf(R.id.iv_filter_red, R.id.iv_filter_green, R.id.iv_filter_blue))
    }

    override fun openFileVideo(filePath: String?) {
        if(filePath.isNullOrEmpty()){
            Toast.makeText(
                this@MainActivity,
                "Cannot open the file, file path is empty!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val uri = Uri.parse(filePath)
        startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
            setDataAndType(uri, "video/mp4")
        })
    }

    private fun unselectViews(currentSelectedViewId: Int, views: List<Int>){
        views
            .filter { it != currentSelectedViewId }
            .forEach {
                findViewById<View>(it).isSelected = false
            }
    }

    private fun configureViews() {
        binding.apply {
            viewMainCamera.ivCamera.isSelected = false

            // init listener views
            viewMainCamera.apply {
                ivCamera.setOnClickListener {
                    recordVideo()
                }

                ivFilterRed.setOnClickListener { v -> onSelectFilterCamera(v) }
                ivFilterGreen.setOnClickListener { v -> onSelectFilterCamera(v) }
                ivFilterBlue.setOnClickListener { v -> onSelectFilterCamera(v) }

                ivResultTake.setOnClickListener {
                    openFileVideo(filepath)
                }
            }

            btnCheckPermission.setOnClickListener {
                resultPermissionsCamera.launchPermissions()
            }
        }
    }

    private fun initPermissions() {
        resultPermissionsCamera = registerPermissionsCamera(this, this)
        resulOpenSettingApp = registerOpenAppSettingResult(this, this)
    }

    private fun showDialogPermissionCameraInfo(){
        CameraPermissionDialog(this).show(
            supportFragmentManager,
            CameraPermissionDialog::class.java.simpleName
        )
    }

    private fun recordVideo(){
        if(isCameraPermissionsNotGranted(this)){
            resultPermissionsCamera.launchPermissions()
            return
        }

        if(!binding.viewMainCamera.ivCamera.isSelected){
            val videoPath = generateTargetVideoCreated()

            filepath = videoPath
            gpuCameraRecorder?.start(filepath)

            return
        }

        gpuCameraRecorder?.stop()
    }

    private fun setUpCameraView() {
        runOnUiThread {
            binding.apply {
                if(isCameraPermissionsNotGranted(this@MainActivity)){
                    viewMainCamera.root.visibility = View.GONE
                    viewPrepareCamera.visibility = View.VISIBLE
                }else{
                    viewMainCamera.root.visibility = View.VISIBLE
                    viewPrepareCamera.visibility = View.GONE
                }

                viewMainCamera.viewActionFilter.visibility = View.VISIBLE
            }

            binding.viewMainCamera.wrapView.removeAllViews()
            sampleGLView = null
            sampleGLView = SampleCameraGLView(applicationContext)
            sampleGLView?.setTouchListener(object: SampleCameraGLView.TouchListener{
                override fun onTouch(event: MotionEvent, width: Int, height: Int) {
                    gpuCameraRecorder?.changeManualFocusPoint(event.x, event.y, width, height)
                }
            })
            binding.viewMainCamera.wrapView.addView(sampleGLView)
        }
    }

    private fun setUpCamera() {
        setUpCameraView()

        if(sampleGLView == null) return

        gpuCameraRecorder = GPUCameraRecorderBuilder(this, sampleGLView)
            .cameraRecordListener(object : CameraRecordListener {
                override fun onGetFlashSupport(flashSupport: Boolean) {
                }

                override fun onRecordComplete() {
                    showProgressPreviewVideo(true)

                    runOnUiThread {
                        binding.viewMainCamera.apply {
                            ivCamera.isSelected = false
                            viewActionFilter.visibility = View.VISIBLE
                            viewResultTake.visibility = View.VISIBLE
                        }
                    }

                    val filePathResult = filepath
                    if(filePathResult == null){
                        Toast.makeText(this@MainActivity, "File video not saved", Toast.LENGTH_SHORT).show()
                        return
                    }

                    exportMp4ToGallery(applicationContext, filePathResult)
                }

                override fun onRecordStart() {
                    runOnUiThread {
                        binding.viewMainCamera.apply {
                            ivCamera.isSelected = true
                            viewActionFilter.visibility = View.GONE
                            viewResultTake.visibility = View.GONE
                        }
                    }
                }

                override fun onError(exception: Exception) {
                    Timber.e(exception)
                }

                override fun onCameraThreadFinish() {
                    if (toggleClick) {
                        runOnUiThread { setUpCamera() }
                    }
                    toggleClick = false
                }

                override fun onVideoFileReady() {
                    loadPreviewVideo()
                }
            })
            .videoSize(videoWidth, videoHeight)
            .cameraSize(cameraWidth, cameraHeight)
            .lensFacing(lensFacing)
            .build()
    }

    private fun loadPreviewVideo(){
        showProgressPreviewVideo(false)

        runOnUiThread {
            filepath?.let {
                Glide.with(this@MainActivity)
                    .load(filepath)
                    .into(binding.viewMainCamera.ivResultTake)
            }
        }
    }

    private fun showProgressPreviewVideo(isShow: Boolean){
        runOnUiThread {
            binding.viewMainCamera.apply {
                ivResultTake.visibility = if(isShow) View.GONE else View.VISIBLE
                progressResultTake.visibility = if(isShow) View.VISIBLE else View.GONE
            }
        }
    }

    /// TODO: this process need to check, is needed while page open or not
    private fun exportMp4ToGallery(context: Context, filePath: String) {
        context.contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            ContentValues(2).apply {
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATA, filePath)
            }
        )

        val file = File(filePath)
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.toString()),
            null,
            null
        )
    }

    private fun releaseCamera() {
        sampleGLView?.onPause()

        gpuCameraRecorder?.apply {
            stop()
            release()
        }
        gpuCameraRecorder = null

        sampleGLView?.let {
            binding.viewMainCamera.wrapView.removeView(sampleGLView)
        }
        sampleGLView = null
    }
}

interface IMainActivity{

    fun onSelectFilterCamera(view: View)
    fun openFileVideo(filePath: String?)
}