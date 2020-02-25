package com.example.hendrik.mianamalaga.unused;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Task to download a file from DropBox and put it in the Downloads folder
 */
public class TaskDownloadFile extends AsyncTask<FileMetadata, Void, File> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private final File mDestinationDirectory;
    private Exception mException;
    private int mFileNumber;

    public interface Callback {
        void onDownloadComplete(File result, int fileNumber);
        void onError(Exception e);
    }


    public TaskDownloadFile(DbxClientV2 dbxClient, File destinationDirectory, int fileNumber, Callback callback ) {
        mDbxClient = dbxClient;
        mCallback = callback;
        mDestinationDirectory = destinationDirectory;
        mFileNumber = fileNumber;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result, mFileNumber);
        }
    }

    @Override
    protected File doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {

            String cloudFolderPath = new File(metadata.getPathLower()).getParent();
            File path = new File(mDestinationDirectory, cloudFolderPath);
            File file = new File(path, metadata.getName());

            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            try (OutputStream outputStream = new FileOutputStream(file)) {
                mDbxClient.files().download(metadata.getPathLower(), metadata.getRev())
                        .download(outputStream);
            }

            return file;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
