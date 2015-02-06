package com.example.prikshit.recorder;

import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Pankaj Kumar on 25-01-2015.
 */
public class Uploader extends AsyncTask<String,Integer,Void> {

    private HttpURLConnection conn;
    private DataOutputStream dos;
    private String lineEnd;
    private String twoHyphens;
    private String boundary;
    private int maxBufferSize;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private ConnectivityManager connManager;

    public Uploader(Context context){
        conn = null;
        dos = null;
        lineEnd = "\r\n";
        twoHyphens = "--";
        boundary = "*****";
        maxBufferSize = 1 * 1024 * 1024;
        mNotifyManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Data Recorder").setContentText("Upload in progress").setSmallIcon(R.drawable.ic_launcher);
        connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    //@Override
    protected Void doInBackground(String... arg) {
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int serverResponseCode = 0;
        String sourceFileUri = arg[0];
        String upLoadServerUri = arg[1];
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.isConnected()) {
            //upload only if connected to wifi
            try {
                URL url = new URL(upLoadServerUri);

                conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL

                conn.setDoInput(true); // AllowInputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", sourceFileUri);

                File sourceFile = new File(sourceFileUri);
                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + sourceFileUri + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available(); // create a buffer of  maximum size

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                long uploadFileSize = sourceFile.length();
                //System.out.println("uploadFileSize "+uploadFileSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    publishProgress((int) ((dos.size() / (float) uploadFileSize) * 100));

                    //System.out.println("dos "+(int) ((dos.size() / (float) uploadFileSize) * 100));

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

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
        }
        return null;
    }

    //@Override
    protected void onProgressUpdate(Integer... var) {
        mBuilder.setProgress(100, var[0], false);
        mNotifyManager.notify(0, mBuilder.build());
    }

    //@Override
    protected void onPostExecute(Void arg) {
        mBuilder.setContentText("Upload complete").setProgress(0,0,false);
        mNotifyManager.cancel(0);
        mNotifyManager.notify(100, mBuilder.build());
        //return null;
    }
}
