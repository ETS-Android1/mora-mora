package com.example.hendrik.mianamalaga.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;


import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.utilities.WiktionaryHelper;

import java.util.List;


/**
 * Background task to handle Wiktionary lookups. This correctly shows and
 * hides the loading animation from the GUI thread before starting a
 * background query to the Wiktionary API. When finished, it transitions
 * back to the GUI thread where it updates with the newly-found entry.
 */
public class WiktionaryLookupTask extends AsyncTask<String, String, List> {

    OnTaskLookUpFinishedListener mOnLookUpFinishedListener;
    Activity mActivity;

    public interface OnTaskLookUpFinishedListener{
        void onTaskLookupFinished(List result);
    }

    public WiktionaryLookupTask(Activity activity, OnTaskLookUpFinishedListener listener){
        mOnLookUpFinishedListener = listener;
        mActivity = activity;
    }


    /**
     * Perform the background query using {@link WiktionaryHelper}, which
     * may return an error message as the result.
     */
    @Override
    protected List doInBackground(String... args) {
        String query = args[0];
        List list = null;

        try {
            if (query != null) {
                list = WiktionaryHelper.getUrlContent( query );
            }
        } catch (WiktionaryHelper.ApiException e) {
            Log.e(Constants.TAG, "Problem making wiktionary request", e);
        }

        return list;
    }


    /**
     * When finished, send the newly-found entry content back
     * to the calling activity
     */
    @Override
    protected void onPostExecute(List list) {

        mOnLookUpFinishedListener.onTaskLookupFinished( list );
    }
}
