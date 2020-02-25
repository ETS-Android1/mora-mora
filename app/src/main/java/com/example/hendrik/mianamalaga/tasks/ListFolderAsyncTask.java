package com.example.hendrik.mianamalaga.tasks;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;


import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;


import java.lang.ref.WeakReference;

public class ListFolderAsyncTask extends AsyncTask<Object, Void, RemoteOperationResult> {


    private WeakReference<Context> mWeakContext;
    private OnTaskListFolderListener mListener;


    public interface OnTaskListFolderListener{
        void onTaskListFolder(RemoteOperationResult result);
    }

    public ListFolderAsyncTask(Activity activity, OnTaskListFolderListener listener){

        mWeakContext = new WeakReference<>(activity.getApplicationContext());
        mListener = listener;
    }

    @Override
    protected RemoteOperationResult doInBackground(Object... params){

        RemoteOperationResult result;
        if (params != null && params.length == 3 && mWeakContext.get() != null) {
            String url = (String)params[0];
            Context context = mWeakContext.get();
            OwnCloudCredentials credentials = (OwnCloudCredentials)params[1];

            Uri uri = Uri.parse(url);
            OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(uri, context, true);
            client.setCredentials(credentials);

            String remotePath = (String)params[2];
            ReadFolderRemoteOperation operation = new ReadFolderRemoteOperation(remotePath);
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
            if (mListener != null)
            {
                mListener.onTaskListFolder(result);
            }
        }
    }
}
