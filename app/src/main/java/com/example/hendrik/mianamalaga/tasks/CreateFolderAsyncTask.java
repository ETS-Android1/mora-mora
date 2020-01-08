package com.example.hendrik.mianamalaga.Tasks;

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
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.FileUtils;


import java.io.File;
import java.lang.ref.WeakReference;

public class CreateFolderAsyncTask extends AsyncTask<Object, Void, RemoteOperationResult> {


    private WeakReference<Context> mWeakContext;
    private WeakReference<CreateFolderAsyncTask.OnTaskCreateFolderListener> mListener;
    private File mFolder;


    public interface OnTaskCreateFolderListener{
        void onTaskCreateFolder( RemoteOperationResult result, File folder );
    }

    public CreateFolderAsyncTask(Activity activity, File folder, CreateFolderAsyncTask.OnTaskCreateFolderListener listener){

        mWeakContext = new WeakReference<>(activity.getApplicationContext());
        mListener = new WeakReference<>( listener );
        mFolder = folder;
    }



    @Override
    protected RemoteOperationResult doInBackground(Object... params){

        RemoteOperationResult result;
        if (params != null && params.length == 2 && mWeakContext.get() != null) {

            Context context = mWeakContext.get();

            String url = (String)params[0];
            OwnCloudCredentials credentials = (OwnCloudCredentials)params[1];
            File folderToCreate = mFolder;

            Uri uri = Uri.parse(url);
            OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(uri, context, true);
            client.setCredentials(credentials);

            String regexString = ".+/" + Constants.MoraMora;
            String remoteFolder = folderToCreate.getAbsolutePath().replaceAll(regexString, "") + FileUtils.PATH_SEPARATOR;

            CreateFolderRemoteOperation createOperation = new CreateFolderRemoteOperation( remoteFolder, true);
            result = createOperation.execute(client);
            Log.e(Constants.TAG,"Result: " + result.toString());

            if( result.isSuccess() || result.getCode().equals( RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS ) ) {
                return result;
            } else {
                return new RemoteOperationResult( RemoteOperationResult.ResultCode.CANNOT_CREATE_FILE );
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
            CreateFolderAsyncTask.OnTaskCreateFolderListener listener = mListener.get();
            if (listener!= null)
            {
                listener.onTaskCreateFolder(result, mFolder );
            }
        }
    }
}
