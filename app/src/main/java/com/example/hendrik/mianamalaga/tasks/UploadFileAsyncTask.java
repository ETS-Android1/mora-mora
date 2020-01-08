package com.example.hendrik.mianamalaga.Tasks;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.IOUtils;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;


import java.io.File;
import java.lang.ref.WeakReference;

public class UploadFileAsyncTask extends AsyncTask<Object, Void, RemoteOperationResult> {


    private WeakReference<Context> mWeakContext;
    private UploadFileAsyncTask.OnTaskUploadListener mListener;
    private long mFileSize;


    public interface OnTaskUploadListener {
        void onTaskUpload(RemoteOperationResult result, long fileSize);
    }

    public UploadFileAsyncTask(Activity activity, UploadFileAsyncTask.OnTaskUploadListener listener) {

        mWeakContext = new WeakReference<>(activity.getApplicationContext());
        mListener = listener;
    }


    @Override
    protected RemoteOperationResult doInBackground(Object... params) {

        RemoteOperationResult result;
        if (params != null && params.length == 3 && mWeakContext.get() != null) {

            Context context = mWeakContext.get();

            String url = (String) params[0];
            OwnCloudCredentials credentials = (OwnCloudCredentials) params[1];
            File fileToUpload = (File) params[2];

            Uri uri = Uri.parse(url);
            OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(uri, context, true);
            client.setCredentials(credentials);

            mFileSize = fileToUpload.length();

            String regexString = ".+/" + Constants.MoraMora;
            String remotePathWithName = fileToUpload.getAbsolutePath().replaceAll(regexString, "");

            String mimeType = IOUtils.getMimeType(fileToUpload);

            Long timeStampLong = fileToUpload.lastModified() / 1000;
            String timeStamp = timeStampLong.toString();

            UploadFileRemoteOperation operation = new UploadFileRemoteOperation(fileToUpload.getAbsolutePath(), remotePathWithName, mimeType, timeStamp);

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
    protected void onPostExecute(RemoteOperationResult result) {
        if (result != null) {

            if (mListener != null) {
                mListener.onTaskUpload(result, mFileSize);
            }
        }
    }
}

