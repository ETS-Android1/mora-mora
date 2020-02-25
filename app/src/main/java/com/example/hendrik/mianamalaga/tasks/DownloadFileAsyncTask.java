package com.example.hendrik.mianamalaga.tasks;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.hendrik.mianamalaga.Constants;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import java.io.File;
import java.lang.ref.WeakReference;

public class DownloadFileAsyncTask extends AsyncTask<Object, Void, RemoteOperationResult> {


    private WeakReference<Context> mWeakContext;
    private DownloadFileAsyncTask.OnTaskDownloadListener mListener;
    private int mFileNumber;
    private long mFileSize;


    public interface OnTaskDownloadListener{
        void onTaskDownload(RemoteOperationResult result, int fileNumber, long fileSize);
    }

    public DownloadFileAsyncTask(Activity activity, int fileNumber,  DownloadFileAsyncTask.OnTaskDownloadListener listener){

        mWeakContext = new WeakReference<>(activity.getApplicationContext());
        mListener = listener;
        mFileNumber = fileNumber;
    }



    @Override
    protected RemoteOperationResult doInBackground(Object... params){

        RemoteOperationResult result;
        if (params != null && params.length == 4 && mWeakContext.get() != null) {
            String url = (String)params[0];
            Context context = mWeakContext.get();
            OwnCloudCredentials credentials = (OwnCloudCredentials)params[1];

            Uri uri = Uri.parse(url);
            OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(uri, context, true);
            client.setCredentials(credentials);

            RemoteFile remoteFilePath = (RemoteFile)params[2];
            mFileSize = remoteFilePath.getSize();

            File destinationFolder = (File)params[3];

            String cloudFolderPath = new File( remoteFilePath.getRemotePath() ).getParent();
            File path = new File( destinationFolder, cloudFolderPath);

            if (!path.exists()) {
                if (!path.mkdirs()) {
                    return new RemoteOperationResult(RemoteOperationResult.ResultCode.UNKNOWN_ERROR);
                }
            } else if (!path.isDirectory()) {
                Log.d(Constants.TAG," Local path is not a directory! ");
                return new RemoteOperationResult(RemoteOperationResult.ResultCode.UNKNOWN_ERROR);
            }

            DownloadFileRemoteOperation operation = new DownloadFileRemoteOperation( remoteFilePath.getRemotePath(), destinationFolder.getAbsolutePath());
            result = operation.execute(client);

            if (result.isSuccess()) {
                return result;
            } else {
                return new RemoteOperationResult(RemoteOperationResult.ResultCode.LOCAL_FILE_NOT_FOUND);
            }

        } else {
            result = new RemoteOperationResult(RemoteOperationResult.ResultCode.UNKNOWN_ERROR);
        }

        return result;
    }

    @Override
    protected void onPostExecute(RemoteOperationResult result){
        if (result!= null)
        {
            DownloadFileAsyncTask.OnTaskDownloadListener listener = mListener;
            if (listener!= null)
            {
                listener.onTaskDownload(result, mFileNumber, mFileSize );
            }
        }
    }
}
