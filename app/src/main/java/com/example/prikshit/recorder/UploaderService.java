package com.example.prikshit.recorder;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ApplicationErrorReport;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;

/**
 * Created by Pankaj on 2/6/2015.
 */
public class UploaderService extends IntentService {
    private HttpURLConnection conn;
    private DataOutputStream dos;
    private String lineEnd;
    private String twoHyphens;
    private String boundary;
    private String sourceFileUri;
    private String upLoadServerUri;
    private int maxBufferSize;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private ConnectivityManager connManager;
    File sdDirectory;// = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Data_Recorder");
    String uploadFilePath;// = sdDirectory.getPath();
    String uploadFileName; //= "sensorData.csv";
    String serverIP; //= "10.1.4.179";
    int minUploadFileSizeLimit ;


    public UploaderService() {
        super(UploaderService.class.getName());
        sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Data_Recorder");
        uploadFilePath = sdDirectory.getPath();
        uploadFileName = "sensorData.csv";
        serverIP = "10.1.5.251";
        minUploadFileSizeLimit = 100*1024*1024;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        //System.out.print("in intentservice");
        System.out.println("handle ");

        upLoadServerUri = "http://"+serverIP+"/uploads/upload_file.php";
        sourceFileUri = uploadFilePath+'/'+uploadFileName;

        conn = null;
        dos = null;
        lineEnd = "\r\n";
        twoHyphens = "--";
        boundary = "*****";
        maxBufferSize = 1 * 1024 * 1024;

        connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int serverResponseCode = 0;
        //String sourceFileUri = arg[0];
        //String upLoadServerUri = arg[1];
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //80:1f:02:17:15:70
        /*
        List<ScanResult> results = wifiManager.getScanResults();
        String message="",str="";
        if (results != null) {
            final int size = results.size();
            if (size == 0) message = "No access points in range";
            else {
                ScanResult bestSignal = results.get(0);
                str = ""; // etWifiList is EditText
                int count = 1;
                for (ScanResult result : results) {
                    str += (count++ + ". " + result.SSID + " : "
                            + result.level + "\n" + result.BSSID + "\n"
                            + result.capabilities + "\n"
                            + "\n=======================\n");
                    if (WifiManager.compareSignalLevel(bestSignal.level,
                            result.level) < 0) {
                        bestSignal = result;
                    }

                    System.out.println(str);
                }
                message = String.format(
                        "%s networks found. %s is the strongest.", size,
                        bestSignal.SSID + " : " + bestSignal.level);
            }
        }
*/
        if (wifi.isConnected()) {
            System.out.println("Is Connected to Wifi");
            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

            System.out.println(connectionInfo.getBSSID());

            if( connectionInfo.getBSSID()=="80:1f:02:16:d1:a0") {
                System.out.println("Campus Wifi");
                //upload only if connected to wifi
                File sourceFile = new File(sourceFileUri);
                long uploadFileSize = sourceFile.length();

                //if the size of the file is big enough then start uploading
                if (uploadFileSize > minUploadFileSizeLimit) {
                    System.out.println("File size OK");

                    boolean isRecordingOn = false;
                    ActivityManager manager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                        if (DataRecorderService.class.getName().equals(service.service.getClassName())) {
                            isRecordingOn = true;
                        }
                    }
                    if(isRecordingOn) {
                        System.out.println("Recording on. Can't Upload.\n");
                    }
                    else{
                        try {
                            System.out.println("Recording Off. Upload Started.\n");
                            FileInputStream fileInputStream = new FileInputStream(sourceFile);
                            URL url = new URL(upLoadServerUri);

                            //this part is for proxy authentication
                            //skip it from here
                            final String authUser = "2011CS1024";
                            final String authPassword = "daburhoney";
                            Authenticator.setDefault(
                                    new Authenticator() {
                                        public PasswordAuthentication getPasswordAuthentication() {
                                            return new PasswordAuthentication(
                                                    authUser, authPassword.toCharArray());
                                        }
                                    }
                            );
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

                            mNotifyManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                            mBuilder = new NotificationCompat.Builder(getApplicationContext());
                            mBuilder.setContentTitle("Data Recorder").setContentText("Upload in progress").setSmallIcon(R.drawable.ic_launcher);

                            boolean isWrittenToDos = false;
                            //System.out.println("uploadFileSize "+uploadFileSize);
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

                            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                            //close the streams //
                            fileInputStream.close();
                            dos.flush();
                            dos.close();
                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                            //Toast.makeText(UploadImageDemo.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                        } catch (Exception e) {
                            //dialog.dismiss();
                            e.printStackTrace();
                            //Toast.makeText(UploadImageDemo.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
                        }

                        //delete the file after uplaoding
                        sourceFile.delete();
                    }
                }
                else{
                    System.out.println("File size not big enough");
                }
            }
            else{
                System.out.println("Upload Failed. No campus connection");
            }
        }
        else{
            System.out.println("Upload Failed. Not Connected to WiFi");
        }
    }
}
