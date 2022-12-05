package com.example.faceverification

import android.graphics.Camera
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.faceverification.base.CameraPreview
import com.example.faceverification.extension.setNavigationBar

class LiveCameraFragment : Fragment() {

    lateinit var cameraPreview: FrameLayout
    lateinit var topView: ConstraintLayout

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
        cameraPreview.right = 10
        topView = v.findViewById(R.id.topView)

        val thisActivity = activity as? AppCompatActivity
        thisActivity?.setNavigationBar("Face Verification", Color.WHITE, false, Color.GRAY, androidx.appcompat.R.drawable.abc_ic_ab_back_material, Color.WHITE)
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
            android.hardware.Camera.open(1)
        } catch (e: Exception) {
            null
        }
    }
}