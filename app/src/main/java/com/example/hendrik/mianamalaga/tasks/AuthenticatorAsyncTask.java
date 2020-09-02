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
import com.owncloud.android.lib.common.network.RedirectionPath;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ExistenceCheckRemoteOperation;
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation;

import java.lang.ref.WeakReference;

public class AuthenticatorAsyncTask  extends AsyncTask<Object, Void, RemoteOperationResult> {

    private static final boolean SUCCESS_IF_ABSENT = false;

    /*
     * Interface to retrieve data from recognition task
     */
    public interface OnAuthenticatorTaskListener{

        void onAuthenticatorTaskCallback(RemoteOperationResult result);
    }

    private WeakReference<Context> mWeakContext;
    private OnAuthenticatorTaskListener mListener;

    public AuthenticatorAsyncTask(Activity activity, OnAuthenticatorTaskListener listener) {
        mWeakContext = new WeakReference<>(activity.getApplicationContext());
        mListener = listener;
    }

    @Override
    protected RemoteOperationResult doInBackground(Object... params) {

        RemoteOperationResult result;
        if (params != null && params.length == 2 && mWeakContext.get() != null) {
            String url = (String)params[0];
            Context context = mWeakContext.get();
            OwnCloudCredentials credentials = (OwnCloudCredentials)params[1];

            // Client
            Uri uri = Uri.parse(url);
            OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(uri, context, true);
            client.setCredentials(credentials);

            // Operation - try credentials
            ExistenceCheckRemoteOperation operation = new ExistenceCheckRemoteOperation(Constants.Root_path, SUCCESS_IF_ABSENT);
            result = operation.execute(client);

            if (operation.wasRedirected()) {
                RedirectionPath redirectionPath = operation.getRedirectionPath();
                String permanentLocation = redirectionPath.getLastPermanentLocation();
                result.setLastPermanentLocation(permanentLocation);
            }

            // Operation - get display name
            if (result.isSuccess()) {
                GetUserInfoRemoteOperation remoteUserNameOperation = new GetUserInfoRemoteOperation();
                result = remoteUserNameOperation.execute(client);
            }

        } else {
            result = new RemoteOperationResult(RemoteOperationResult.ResultCode.UNKNOWN_ERROR);
        }

        return result;
    }

    @Override
    protected void onPostExecute(RemoteOperationResult result) {

        if (result!= null)
        {
            if (mListener != null)
            {
                mListener.onAuthenticatorTaskCallback(result);
            }
        }
    }

}
