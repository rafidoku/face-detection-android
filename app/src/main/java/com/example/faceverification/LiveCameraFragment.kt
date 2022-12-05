package com.example.faceverification

import android.graphics.Camera
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.faceverification.base.CameraPreview

class LiveCameraFragment : Fragment() {

    lateinit var cameraPreview: FrameLayout

    private var faceCamera: android.hardware.Camera? = null
    private var preview: CameraPreview? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_live_camera, container, false)
        setupView(view)
        return view
    }

    private fun setupView(v: View) {
        cameraPreview = v.findViewById(R.id.cameraPreview)

        faceCamera = getCameraInstance()
        preview = faceCamera?.let {
            CameraPreview(requireContext(), it)
        }

        preview?.also {
            cameraPreview.addView(it)
        }

    }

    private fun getCameraInstance(): android.hardware.Camera? {
        return  try {
            android.hardware.Camera.open()
        } catch (e: Exception) {
            null
        }
    }
}