package com.example.hendrik.mianamalaga;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TaskUpLoad extends AsyncTask<Void,Void,File> {
    private DbxClientV2 dbxClient;
    private File mFile;
    private Context context;
    private final Callback mCallback;

    public interface Callback {
        void onDownloadComplete(File result);
    }

    TaskUpLoad(DbxClientV2 dbxClient, File file, Context context, Callback callback) {
        this.dbxClient = dbxClient;
        this.mFile = file;
        this.context = context;
        this.mCallback = callback;
    }

    @Override
    protected File doInBackground(Void... voids) {
        try {
            InputStream inputStream = new FileInputStream(mFile);                                                              // Upload to DropBox
            String filePath = mFile.getParent();
            String regexString = ".+/" + Constants.MoraMora;
            String destinationPath = filePath.replaceAll(regexString, "");

            dbxClient.files().uploadBuilder(destinationPath + "/" + mFile.getName())
                    .withMode(WriteMode.OVERWRITE)                                                                             //always overwrite existing file
                    .uploadAndFinish(inputStream);
            return mFile;
        } catch (DbxException e) {
            e.printStackTrace();
            Log.d(Constants.TAG, "Upload Failure Mist!");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(Constants.TAG, "Upload Failure Mist!");
        }
        return null;
    }



    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        mCallback.onDownloadComplete( file );
        //Toast.makeText(context, "Uploading file :" + file.toString(), Toast.LENGTH_SHORT).show();
    }

}
