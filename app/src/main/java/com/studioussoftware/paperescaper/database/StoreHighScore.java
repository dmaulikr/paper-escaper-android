package com.studioussoftware.paperescaper.database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.studioussoftware.paperescaper.interfaces.IScoreStoredListener;

/**
 * Created by Robbie Wolfe on 10/10/2016.
 * Abstract class which accesses a database in a background thread
 * Subclasses should implement method of accessing database, all this does
 * is set up the progress indicator, closes it and alerts listener that job is done
 * Subclasses should set finished to true when the job is done
 */
public abstract class StoreHighScore extends AsyncTask<String, String, String> implements DialogInterface.OnClickListener {
    private ProgressDialog dialog;
    private String storingScoreMessage;

    protected Activity parentActivity;
    protected IScoreStoredListener listener;

    protected String name;
    protected int score;

    protected boolean finished = false;

    public StoreHighScore(Activity parent, IScoreStoredListener listener, String name, int score, String storingScoreMessage) {
        parentActivity = parent;
        this.listener = listener;
        this.name = name;
        this.score = score;
        this.storingScoreMessage = storingScoreMessage;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(parentActivity);
        dialog.setMessage(storingScoreMessage);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(String s) {
        if (finished) {
            onFinish();
        }
    }

    /**
     * Only called once really finished
     */
    private void onFinish() {
        // Execute on UI Thread
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                listener.scoreStored();
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        onFinish();
    }
}
