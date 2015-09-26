package com.prikshit.recorder.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.prikshit.recorder.R;
import com.prikshit.recorder.constants.Constants;
import com.prikshit.recorder.main.Logger;
import com.prikshit.recorder.receivers.NotificationReceiver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * Intent service used to upload data file to server
 *
 * Created by Pankaj on 2/6/2015.
 */
public class UploaderService extends IntentService {
    private String TAG = "uploaderService";
    private int maxBufferSize = 1*1024*1024;
    private String uploadScriptName = "roadbump/phoneupload.php";
    private String uploadFileName = Constants.DATA_FILE_NAME;
    private String serverIP = Constants.SERVER_ADDRESS;
    private HttpURLConnection conn;
    private DataOutputStream dos;
    private int minUploadFileSizeLimit = Constants.MIN_UPLOAD_SIZE_LIMIT;


    public UploaderService() {
        super(UploaderService.class.getName());
    }

    @Override
    public void onHandleIntent(Intent intent) {
        //Logger.i(TAG,"uploader service started");
        // connection info
        ConnectivityManager connManager;

        // notification info
        NotificationManager mNotifyManager;
        NotificationCompat.Builder mNotificationBuilder;

        // file info
        File sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DIRECTORY);
        String uploadFilePath = sdDirectory.getPath();

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String sourceFileUri;
        String upLoadServerUri;

        upLoadServerUri = "http://"+serverIP+"/"+uploadScriptName;
        sourceFileUri = uploadFilePath+'/'+uploadFileName;

        connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;

        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected()) {
            File sourceFile = new File(sourceFileUri);
            long uploadFileSize = sourceFile.length();
            //if the size of the file is big enough then start uploading
            if (uploadFileSize > minUploadFileSizeLimit) {
                //Logger.i(TAG,"File Size OK");
                if(isRecordingServiceRunning()) {
                    //Log.i(TAG, "Recording was on. No uploading");
                    //Logger.i(TAG, "Recording was on. No uploading");
                }
                else{
                    try {
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL(upLoadServerUri);

                        // setting up connection parameters
                        conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
                        conn.setDoInput(true); // AllowInputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setChunkedStreamingMode(1024);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", sourceFileUri);
                        conn.setRequestProperty("connection","closed");
                        Log.i(TAG, "properties set");
                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + sourceFileUri + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);

                        // create a buffer of  maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];
                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        //show start upload notification now
                        mNotifyManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext());

                        //showing cancel upload button now
                        int notificationId = new Random().nextInt();
                        Intent cancelIntent = new Intent(getBaseContext(), NotificationReceiver.class);
                        cancelIntent.putExtra("notId", notificationId );
                        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        mNotificationBuilder
                                .setContentTitle(Constants.APP_NAME)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setOngoing(true)
                                .setContentText("Progress");
                        mNotificationBuilder.addAction(0, "Cancel Upload", cancelPendingIntent);

                        // show progress notification
                        while (bytesRead > 0) {
                            mNotificationBuilder.setProgress(100, (int) ((dos.size() / (float) uploadFileSize) * 100), false);
                            String progress = (int) ((dos.size() / (float) uploadFileSize) * 100) + "%";
                            mNotificationBuilder.setContentInfo(progress);
                            mNotifyManager.notify(notificationId, mNotificationBuilder.build());

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }
                        // send multipart form data necessary after file data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        // cancel current notification
                        mNotifyManager.cancel(notificationId);

                        // show upload complete notification NOT WORKING
                        // DON'T KNOW WHY :P
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder2 = new NotificationCompat.Builder(getApplicationContext());
                        builder2
                                .setContentTitle(Constants.APP_NAME)
                                .setContentText("Upload complete");
                        notificationManager.notify(notificationId, builder2.build());

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();
                        Logger.i(TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                        //close the streams
                        fileInputStream.close();
                        dos.flush();
                        dos.close();
                    } catch (Exception e) {
                        Log.e(TAG,"Upload file to server Exception Exception : " + e.getMessage());
                    }
                    // delete the file after uploading
                    sourceFile.delete();
                }
            }
            else{
                //Log.i(TAG,"file size not big enough");
                //Log.i(TAG,"current size: " + uploadFileSize);
                //Log.i(TAG,"minimum size: " + minUploadFileSizeLimit);
            }
        }
        else{
            //Log.i(TAG,"Not connected to Wifi");
        }
    }

    public boolean isRecordingServiceRunning(){
        ActivityManager manager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (Constants.RECORDING_CLASS.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy(){
        // this will throw an error but that will be handled by os.
        // should be checked in next version
        if(conn != null) conn.disconnect();
        if(dos != null){
            try {
                dos.flush();
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

}
