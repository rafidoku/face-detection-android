package com.example.faceverification

import android.graphics.*
import android.opengl.Visibility
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.faceverification.common.ImageUtils
import com.example.faceverification.extension.convertImageProxyToBitmap
import com.example.faceverification.extension.setNavigationBar
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias PreviewListener = (data: Double) -> Unit

class LiveCameraFragment : Fragment() {

    lateinit var cameraPreview: PreviewView
    lateinit var topView: ConstraintLayout
    lateinit var detector: FaceDetector
    lateinit var faceStatusLabel: TextView
    lateinit var counterLabel: TextView

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var isDetected: Boolean = false
    private var collectedImages: MutableList<Bitmap> = mutableListOf<Bitmap>()
    private var isFaceCaptureStarted: Boolean = false

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
        counterLabel = v.findViewById(R.id.counterLabel)
        cameraExecutor = Executors.newSingleThreadExecutor()

        val thisActivity = activity as? AppCompatActivity
        thisActivity?.setNavigationBar("Face Verification", Color.WHITE, false, Color.GRAY, androidx.appcompat.R.drawable.abc_ic_ab_back_material, Color.WHITE)

        startCamera()
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

            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
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
                                            this.isDetected = true
                                            counterLabel.visibility = View.VISIBLE
                                            setTimerCountdown()
                                        }
                                    }.addOnFailureListener {
                                        Log.d("TAG", "Detect Face error ${it.message}")
                                    }
                                }
                            } else if (isDetected && isFaceCaptureStarted) {
                                if (cameraPreview.bitmap != null) {
                                    takePhoto()
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

    private fun takePhoto() {
        if (imageCapture == null) {
            Log.d("TAG", "Null ImageCapture")
            return
        }
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    if (collectedImages.size < 15) {
                        collectedImages.add(ImageUtils.shared.getCompressBitmap(image.convertImageProxyToBitmap())!!)
                        Log.d("TAG", "Face Collected ${collectedImages.size}")
                        image.close()
                    } else {
                        isFaceCaptureStarted = false
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("TAG", "Image capture failed ${exception.message}")
                }
            })
    }

    private fun setTimerCountdown() {
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counterLabel.setText("${(millisUntilFinished / 1000)}")
            }
            override fun onFinish() {
                counterLabel.visibility = View.GONE
                isFaceCaptureStarted = true
            }
        }.start()
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