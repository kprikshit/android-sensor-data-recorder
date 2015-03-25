package com.example.prikshit.recorder;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private int minUploadFileSizeLimit = Constants.MIN_UPLOAD_SIZE_LIMIT;


    public UploaderService() {
        super(UploaderService.class.getName());
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Logger.i(TAG,"uploader service started");
        // connection info
        HttpURLConnection conn;
        DataOutputStream dos;
        ConnectivityManager connManager;

        // notification info
        NotificationManager mNotifyManager;
        NotificationCompat.Builder mBuilder;

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
        int serverResponseCode = 0;

        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected()) {
            WifiManager  wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            //final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            //if( connectionInfo.getBSSID()=="80:1f:02:16:d1:a0") {
            File sourceFile = new File(sourceFileUri);
            long uploadFileSize = sourceFile.length();

            //if the size of the file is big enough then start uploading
            if (uploadFileSize > minUploadFileSizeLimit) {
                //Logger.i(TAG,"File Size OK");
                boolean isRecordingOn = isRecordingServiceRunning();
                if(isRecordingOn) {
                    Log.i(TAG, "Recording was on. No uploading");
                    //Logger.i(TAG, "Recording was on. No uploading");
                }
                else{
                    try {
                        Log.i(TAG,"Everything is good. Uploading file now");
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL(upLoadServerUri);

                        //this part is for proxy authentication
                        final String authUser = "2011CS1024";
                        final String authPassword = "daburhoney";
                        /**
                        Authenticator.setDefault(
                                new Authenticator() {
                                    public PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(
                                                authUser, authPassword.toCharArray());
                                    }
                                }
                        );
                        */
                        //to here
                        conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
                        conn.setDoInput(true); // AllowInputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", sourceFileUri);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + sourceFileUri + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);

                        bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];
                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        //show notification now
                        mNotifyManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        mBuilder = new NotificationCompat.Builder(getApplicationContext());
                        mBuilder
                                .setContentTitle(Constants.APP_NAME)
                                .setContentText("Uploading data to server")
                                .setSmallIcon(R.drawable.ic_launcher);

                        boolean isWrittenToDos = false;
                        while (bytesRead > 0) {
                            dos.write(buffer, 0, bufferSize);
                            mBuilder.setProgress(100, (int) ((dos.size() / (float) uploadFileSize) * 100), false);
                            mNotifyManager.notify(0, mBuilder.build());

                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }

                        // send multipart form data necesssary after file data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        mBuilder.setContentText("Upload complete").setProgress(0, 0, false);
                        mNotifyManager.cancel(0);
                        mNotifyManager.notify(100, mBuilder.build());

                        // Responses from the server (code and message)
                        serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();

                        Log.i(TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                        //close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();
                    } catch (Exception e) {
                        //dialog.dismiss();
                        e.printStackTrace();
                        Log.e(TAG,"Upload file to server Exception Exception : " + e.getMessage(), e);
                    }
                    //delete the file after uploading
                    sourceFile.delete();
                }
            }
            else{
                Log.i(TAG,"file size not big enough");
                Log.i(TAG,"current size: " + uploadFileSize);
                Log.i(TAG,"minimum size: " + minUploadFileSizeLimit);
            }
        }
        else{
            Log.i(TAG,"Not connected to Wifi");
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
}
