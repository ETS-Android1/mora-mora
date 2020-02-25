package com.example.hendrik.mianamalaga.unused;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

public class TaskUserAccount extends AsyncTask<Void, Void, FullAccount> {

    private DbxClientV2 dbxClient;
    private TaskDelegate  delegate;
    private Exception error;

    public interface TaskDelegate {
        void onAccountReceived(FullAccount account);
        void onError(Exception error);
    }

    public TaskUserAccount(DbxClientV2 dbxClient, TaskDelegate delegate){
        this.dbxClient = dbxClient;
        this.delegate = delegate;
    }

    @Override
    protected FullAccount doInBackground(Void... params) {
        try {
            return dbxClient.users().getCurrentAccount();                   //get the users FullAccount
        } catch (DbxException e) {
            e.printStackTrace();
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);

        if (account != null && error == null){
            delegate.onAccountReceived(account);                            //User Account received successfully
        }
        else {
            delegate.onError(error);                                        // Something went wrong
        }
    }
}

