package com.infilect.infilectpro.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.infilect.infilectpro.constants.Constant;
import com.infilect.infilectpro.R;
import com.infilect.infilectpro.utils.ShowToast;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int STORAGE_PERMISSION_CODE = 198;
    ImageCapture imageCapture ;
    FloatingActionButton takePicture ;
    PreviewView previewView ;
    ProgressBar progress_circular ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        takePicture = findViewById(R.id.takePicture) ;
        progress_circular = findViewById(R.id.progress_circular) ;
        progress_circular.setVisibility(View.GONE);

        setUpCameraAndPreview();

        takePicture.setOnClickListener(this);


    }

    private void setUpCameraAndPreview()
    {
        ListenableFuture cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                CameraSelector cameraSelector = new CameraSelector.Builder()

                        .build();

                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview,
                        imageCapture) ;

                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider());
            } catch (InterruptedException | ExecutionException e) {
                //
            }
        }, ContextCompat.getMainExecutor(this));

    }

    @Override
    protected void onResume() {
        super.onResume();

        checkForPermission() ;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

            case R.id.takePicture : takePicture() ;

        }
    }


    private void takePicture()
    {
        progress_circular.setVisibility(View.VISIBLE);
        takePicture.setEnabled(false);
        takePicture.setClickable(false);

        File file = getExternalFilesDirs(Constant.TEMP_IMAGE_DIR)[0] ;
        String path = file.getAbsolutePath() ;

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(new File(path, Constant.TEMP_IMAGE)).build();

        imageCapture.takePicture(outputFileOptions,  ContextCompat.getMainExecutor(CameraActivity.this),

                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        takePicture.setEnabled(true);
                        takePicture.setClickable(true);
                        progress_circular.setVisibility(View.GONE);
                        startActivity(new Intent(CameraActivity.this,ImagePreviewActivity.class));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {

                        takePicture.setEnabled(true);
                        takePicture.setClickable(true);
                        progress_circular.setVisibility(View.GONE);
                        ShowToast.showToastMessage("Hmmm.. Some Problem Occured Retry");

                    }
                } );
    }





    public  void checkForPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        requestStoragePermission();

    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return;

        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
           ShowToast.showToastMessage("Allow camera permission to use this app");
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == STORAGE_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return ;

            } else {

                ShowToast.showToastMessage("Allow Camera permission to use this app");
                finish();
            }
        }
    }





}