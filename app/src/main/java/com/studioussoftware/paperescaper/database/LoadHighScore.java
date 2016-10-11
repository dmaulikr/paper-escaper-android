package com.studioussoftware.paperescaper.database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Pair;

import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.interfaces.ILoadedScoresListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robbie Wolfe on 10/6/2016.
 * Abstract class which accesses a database in a background thread
 * Subclasses should implement method of accessing database, all this does
 * is set up the progress indicator, closes it and alerts listener that job is done
 */
public abstract class LoadHighScore extends AsyncTask<String, String, String> {
    private ProgressDialog dialog;
    private ILoadedScoresListener onFinishedListener = null;

    protected Activity parentActivity;
    protected List<Pair<String, String>> scoresList = new ArrayList<>();

    public LoadHighScore(Activity parent, ILoadedScoresListener listener) {
        parentActivity = parent;
        onFinishedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(parentActivity);
        dialog.setMessage(parentActivity.getResources().getString(R.string.loading_scores));
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(String s) {
        // Execute on UI Thread
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onFinishedListener.populateTable(scoresList);
                dialog.dismiss();
            }
        });
    }
}
