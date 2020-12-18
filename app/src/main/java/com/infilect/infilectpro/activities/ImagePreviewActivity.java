package com.infilect.infilectpro.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.infilect.infilectpro.constants.Constant;
import com.infilect.infilectpro.R;
import com.infilect.infilectpro.services.UploadService;
import com.infilect.infilectpro.utils.ShowToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ImagePreviewActivity extends AppCompatActivity {

    ImageView previewImage ;
    Bitmap bitmap ;
    FloatingActionButton uploadButton ;
    private Boolean isBound = false;
    private UploadServiceConnection uploadServiceConnection ;
    private UploadService uploadService ;

    File file ;
    File f ;

    LiveData<Boolean> isProcessing ;
    LiveData<Boolean> isSuccessUploading ;

    ProgressBar progress_circular ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        file = getExternalFilesDirs(Constant.TEMP_IMAGE_DIR)[0] ;
        f = new File(file,Constant.TEMP_IMAGE) ;

        previewImage = findViewById(R.id.previewImage) ;
        uploadButton = findViewById(R.id.upload) ;
        progress_circular = findViewById(R.id.progress_circular) ;
        progress_circular.setVisibility(View.GONE);

        uploadButton.setOnClickListener(v -> {
          if(isBound)
          {
              uploadService.startUpload(f);
          }
        });


        setPreviewImage();


    }


    @Override
    protected void onResume() {
        super.onResume();

        uploadServiceConnection = new UploadServiceConnection() ;

        Intent intent = new Intent(this , UploadService.class);
        startService(intent);
        bindService(intent , uploadServiceConnection,BIND_AUTO_CREATE);
    }


    private class UploadServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UploadService.UploadServiceBinder binder = (UploadService.UploadServiceBinder) service;
            uploadService = binder.getService();
            isBound = true ;

            isProcessing = uploadService.getIsUploading() ;
            isSuccessUploading = uploadService.getSuccessUploading() ;

            isProcessing.observe(ImagePreviewActivity.this, processing->{

                if(processing)
                {
                    progress_circular.setVisibility(View.VISIBLE);
                }else
                {
                    progress_circular.setVisibility(View.GONE);
                }

            });

            isSuccessUploading.observe(ImagePreviewActivity.this,success->{

                if(success)
                {
                    finish();
                    startActivity(new Intent(ImagePreviewActivity.this,SuccessActivity.class)) ;
                }else
                {
                   ShowToast.showToastMessage("Some Error Occured");
                }

            });

        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isProcessing!=null)
        {
            isProcessing.removeObservers(this);
        }

        if(isSuccessUploading!=null)
        {
            isSuccessUploading.removeObservers(this);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isBound){
            unbindService(uploadServiceConnection);
            isBound = false;

        }
    }


    private void setPreviewImage()
    {
        try {
            InputStream inputStream =  new FileInputStream(new File(f.getAbsolutePath()));

            bitmap = BitmapFactory.decodeStream(inputStream) ;

            Bitmap rotatedBitmap ;
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            previewImage.setImageBitmap(rotatedBitmap);

        } catch (Exception e) {
            e.printStackTrace();
           }

    }

}