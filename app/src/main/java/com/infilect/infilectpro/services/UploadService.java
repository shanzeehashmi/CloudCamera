package com.infilect.infilectpro.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.infilect.infilectpro.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

public class UploadService extends Service {

    private UploadServiceBinder uploadServiceBinder = new UploadServiceBinder();
    private MutableLiveData<Boolean> isUploading = new MutableLiveData<>() ;
    private MutableLiveData<Boolean> successUploading = new MutableLiveData<>() ;

    public UploadService() {
    }

    public LiveData<Boolean> getIsUploading()
    {
        return isUploading ;
    }

    public LiveData<Boolean> getSuccessUploading()
    {
        return successUploading ;
    }


    public class UploadServiceBinder extends Binder {
        public UploadService getService() {
            return UploadService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return uploadServiceBinder ;
    }


    public void startUpload(File file)
    {

        isUploading.setValue(true);

        Bitmap bitmap ;

        try {

        InputStream inputStream = new FileInputStream(file.getAbsoluteFile());
        bitmap = BitmapFactory.decodeStream(inputStream) ;

        Notification notification = initNotification();
        startForeground(22, notification);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String image_name = UUID.randomUUID() + ".jpeg" ;
        StorageReference imageRef = storageRef.child(image_name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnFailureListener(exception -> {
            successUploading.setValue(false);
            isUploading.setValue(false);
            stopForeground(true);
            stopSelf();

        }).addOnSuccessListener(taskSnapshot -> {
            isUploading.setValue(false);
            successUploading.setValue(true);
            stopForeground(true);
            stopSelf();

        });


        }catch (Exception e)
        {
            isUploading.setValue(false);
            successUploading.setValue(false);
            stopForeground(true);
            stopSelf();
        }

    }



    private Notification initNotification() {

        String notificationTitle ="Uploading";
        String notificationText = "Image is Uploading..";

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(notificationTitle);
        bigTextStyle.bigText(notificationText);

        String channelID = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                createNotificationChannel("CHANNEL_ID", "CHANNEL_NAME")
                : getString(R.string.app_name);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID);
        builder.setStyle(bigTextStyle);
        builder.setContentTitle(notificationTitle);
        builder.setContentText(notificationText);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_LOW) ;

        Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        builder.setLargeIcon(bitmapIcon);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_LOW);
        } else {
            builder.setPriority(Notification.PRIORITY_LOW);
        }

        return builder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelID, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
        return channelID;
    }
    
    
    
    
}
