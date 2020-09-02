package com.example.hendrik.mianamalaga.tasks;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.utilities.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromUrlAsyncTask extends AsyncTask<Object, Integer, String> {

    private ProgressBar mProgressBar = null;
    OnTaskFinishedListener mListener = null;

    public interface OnTaskFinishedListener{
        String onTaskFinished(String fileLocation);
    }


    public DownloadFileFromUrlAsyncTask(ProgressBar progressbar, OnTaskFinishedListener listener){
        this.mProgressBar = progressbar;
        this.mListener = listener;
    }

    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if( mProgressBar != null ){
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(0);
        }
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(Object... params) {
        int count;
        try {
            URL url = new URL((String) params[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            String destinationFileLocation = params[1] + File.separator + Utils.getNameFromPath( (String) params[0] );

            int lengthOfFile = connection.getContentLength();

            InputStream input = new BufferedInputStream( url.openStream(), 8192);
            OutputStream output = new FileOutputStream( destinationFileLocation );

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
//                publishProgress("" + (int) ((total * 100) / lengthOfFile ));
                publishProgress( (int) ((total * 100) / lengthOfFile) );
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            return destinationFileLocation;

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(Integer... progress) {
        // setting progress percentage
        if( mProgressBar != null ){
            mProgressBar.setProgress( progress[0] );
        }
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(String fileLocation) {
        // dismiss the dialog after the file was downloaded
        if( mProgressBar != null ) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressBar.setIndeterminate(true);
        }

        if( mListener != null ){
            mListener.onTaskFinished( fileLocation );
        }

    }

}

