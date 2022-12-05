package com.example.faceverification

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.faceverification.extension.setNavigationBar
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias PreviewListener = (data: Double) -> Unit

class LiveCameraFragment : Fragment() {

    lateinit var cameraPreview: PreviewView
    lateinit var topView: ConstraintLayout
    lateinit var detector: FaceDetector
    lateinit var faceStatusLabel: TextView
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    private var isDetected: Boolean = false

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
        // Face detection property initializer
        val realtimeopts = FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build()
        detector = FaceDetection.getClient(realtimeopts)
        cameraPreview = v.findViewById(R.id.cameraPreview)
        topView = v.findViewById(R.id.topView)
        faceStatusLabel = v.findViewById(R.id.faceStatus)
        cameraExecutor = Executors.newSingleThreadExecutor()

        val thisActivity = activity as? AppCompatActivity
        thisActivity?.setNavigationBar("Face Verification", Color.WHITE, false, Color.GRAY, androidx.appcompat.R.drawable.abc_ic_ab_back_material, Color.WHITE)

        startCamera()
        faceStatusLabel.setOnClickListener{
            this.takePhoto("taken_picture")
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraPreview.surfaceProvider)
                }

            val imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PreviewAnalyzer { bmp ->
                        activity?.runOnUiThread {
                            if (!isDetected) {
                                if (cameraPreview.bitmap != null) {
                                    detector.process(cameraPreview.bitmap!!, 0).addOnSuccessListener { faces ->
                                        if(faces.size > 0) {
                                            this.faceStatusLabel.setText("Face Detected")
                                            this.isDetected = true
                                        }
                                    }.addOnFailureListener {
                                        Log.d("TAG", "Detect Face error ${it.message}")
                                    }
                                }
                            }
                        }

                    })
                }
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto(filenameFormat: String) {
        if (imageCapture == null) {
            Log.d("TAG", "Null ImageCapture")
            return
        }
//        val imageCapture = imageCapture ?:  return
        val name = SimpleDateFormat(filenameFormat, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                activity?.contentResolver!!,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d("TAG", msg)
                }
            }
        )
    }

    private class PreviewAnalyzer(private val listener: PreviewListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()
            val data = ByteArray(remaining())
            get(data)
            return data
        }

        override fun analyze(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val avr = pixels.average()

            listener(avr)

            image.close()
        }
    }
}