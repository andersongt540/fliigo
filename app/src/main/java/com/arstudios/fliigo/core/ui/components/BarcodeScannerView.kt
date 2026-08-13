package com.arstudios.fliigo.core.ui.components

import android.annotation.SuppressLint

import androidx.camera.core.CameraSelector

import androidx.camera.core.ImageAnalysis

import androidx.camera.core.Preview

import androidx.camera.lifecycle.ProcessCameraProvider

import androidx.camera.view.PreviewView

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.platform.LocalLifecycleOwner

import androidx.compose.ui.viewinterop.AndroidView

import androidx.core.content.ContextCompat

import com.google.mlkit.vision.barcode.BarcodeScanning

import com.google.mlkit.vision.common.InputImage

import java.util.concurrent.Executors



@SuppressLint("UnsafeOptInUsageError")

@Composable

fun BarcodeScannerView(

    modifier: Modifier = Modifier,

    onBarcodeDetected: (String) -> Unit

) {

    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }



    // Evitamos disparar múltiples eventos seguidos del mismo código

    var hasScanned by remember { mutableStateOf(false) }



    AndroidView(

        modifier = modifier,

        factory = { ctx ->

            val previewView = PreviewView(ctx)

            val cameraProvider = cameraProviderFuture.get()



            val preview = Preview.Builder().build().also {

                it.setSurfaceProvider(previewView.surfaceProvider)

            }



            val barcodeScanner = BarcodeScanning.getClient()



            val imageAnalysis = ImageAnalysis.Builder()

                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

                .build()

                .also { analysis ->

                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->

                        val mediaImage = imageProxy.image

                        if (mediaImage != null && !hasScanned) {

                            val image = InputImage.fromMediaImage(

                                mediaImage,

                                imageProxy.imageInfo.rotationDegrees

                            )



                            barcodeScanner.process(image)

                                .addOnSuccessListener { barcodes ->

                                    for (barcode in barcodes) {

                                        barcode.rawValue?.let { code ->

                                            hasScanned = true

                                            onBarcodeDetected(code)

                                        }

                                    }

                                }

                                .addOnFailureListener {

                                    // Manejar error de escaneo si es necesario

                                }

                                .addOnCompleteListener {

                                    imageProxy.close()

                                }

                        } else {

                            imageProxy.close()

                        }

                    }

                }



            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(

                    lifecycleOwner,

                    CameraSelector.DEFAULT_BACK_CAMERA,

                    preview,

                    imageAnalysis

                )

            } catch (e: Exception) {

                e.printStackTrace()

            }



            previewView

        }

    )

}