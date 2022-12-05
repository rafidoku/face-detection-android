package com.example.faceverification

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.jar.Manifest

class FaceDetectorFragment : Fragment() {

    lateinit var resultLabel: TextView
    lateinit var testImageView: ImageView
    lateinit var catFace: MaterialButton
    lateinit var humanFace: MaterialButton
    lateinit var cameraButton: MaterialButton

    var scaledBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_face_detector, container, false)
        setupView(view)
        return view
    }

    private fun setupView(v: View) {
        resultLabel = v.findViewById(R.id.totalLabel)
        testImageView = v.findViewById(R.id.imageTest)
        catFace = v.findViewById(R.id.catButton)
        humanFace = v.findViewById(R.id.faceButton)
        cameraButton = v.findViewById(R.id.cameraButton)


        // Face detection property initializer
        val realtimeopts = FaceDetectorOptions.Builder().setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL).build()
        val detector = FaceDetection.getClient(realtimeopts)

        catFace.setOnClickListener {
            val testBitmap = BitmapFactory.decodeResource(resources, R.drawable.cat_face)
            scaledBitmap = Bitmap.createScaledBitmap(testBitmap, 140, 140, true)
            testImageView.setImageBitmap(scaledBitmap)
            if (scaledBitmap != null) {
                predict(detector, scaledBitmap!!)
            }
        }

        humanFace.setOnClickListener {
            val testBitmap = BitmapFactory.decodeResource(resources, R.drawable.face1)
            scaledBitmap = Bitmap.createScaledBitmap(testBitmap, 140, 140, true)
            testImageView.setImageBitmap(scaledBitmap)
            testImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            if (scaledBitmap != null) {
                predict(detector, scaledBitmap!!)
            }
        }

        cameraButton.setOnClickListener {
            checkCameraPermission()
        }

    }

    private fun predict(detector: FaceDetector, bmp: Bitmap) {
        val result = detector.process(bmp!!, 0)
            .addOnSuccessListener {
                    faces ->
                this.resultLabel.setText("Total face "+faces.size.toString())
            }
            .addOnFailureListener{
                    e -> this.resultLabel.setText("Failed detect face")
            }
    }

    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    private fun checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (checkCameraHardware(requireContext())) {
                findNavController().navigate(R.id.action_faceDetectorFragment_to_liveCameraFragment)
            } else {
                Log.d("TAG", "Device not support camera")
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), 44)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            44 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

}