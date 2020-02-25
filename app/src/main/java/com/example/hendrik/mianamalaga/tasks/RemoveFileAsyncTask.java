package com.example.hendrik.mianamalaga.tasks;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.hendrik.mianamalaga.Constants;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;

import java.io.File;
import java.lang.ref.WeakReference;

public class RemoveFileAsyncTask extends AsyncTask<Object, Void, RemoteOperationResult> {


    private WeakReference<Context> mWeakContext;
    private RemoveFileAsyncTask.OnTaskRemoveListener mListener;


    public interface OnTaskRemoveListener{
        void onTaskRemove(RemoteOperationResult result);
    }

    public RemoveFileAsyncTask(Activity activity, RemoveFileAsyncTask.OnTaskRemoveListener listener){

        mWeakContext = new WeakReference<>(activity.getApplicationContext());
        mListener = listener;
    }



    @Override
    protected RemoteOperationResult doInBackground(Object... params){

        RemoteOperationResult result;
        if (params != null && params.length == 3 && mWeakContext.get() != null) {

            Context context = mWeakContext.get();

            String url = (String)params[0];
            OwnCloudCredentials credentials = (OwnCloudCredentials)params[1];
            File fileToRemove = (File)params[2];

            Uri uri = Uri.parse(url);
            OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(uri, context, true);
            client.setCredentials(credentials);


            String regexString = ".+/" + Constants.MoraMora;
            String remotePath = fileToRemove.getAbsolutePath().replaceAll(regexString, "");

            RemoveFileRemoteOperation operation = new RemoveFileRemoteOperation(remotePath);

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
            if (mListener!= null)
            {
                mListener.onTaskRemove(result);
            }
        }
    }
}
