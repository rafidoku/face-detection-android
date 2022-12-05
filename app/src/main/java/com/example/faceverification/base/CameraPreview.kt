package com.example.faceverification.base

import android.content.Context
import android.graphics.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

class CameraPreview(context: Context, private val camera: android.hardware.Camera): SurfaceView(context), SurfaceHolder.Callback {

    private val myHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraPreview)
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        camera.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                Log.d("TAG", "Error setting camera preview: ${e.message}")
            }
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        if (holder.surface == null) {
            return
        }

        try {
            camera.stopPreview()
        } catch (e: Exception) {

        }

        camera.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: Exception) {
                Log.d("TAG", "Error setting camera preview: ${e.message}")
            }
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {

    }

}