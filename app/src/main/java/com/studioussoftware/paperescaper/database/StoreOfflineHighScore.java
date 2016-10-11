package com.studioussoftware.paperescaper.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.interfaces.IScoreStoredListener;

/**
 * Created by Robbie Wolfe on 10/10/2016.
 */
public class StoreOfflineHighScore extends StoreHighScore {

    public StoreOfflineHighScore(Activity parent, IScoreStoredListener listener, String name, int score) {
        super(parent, listener, name, score, parent.getResources().getString(R.string.storing_offline_scores));
    }

    @Override
    protected String doInBackground(String... strings) {
        // Get the data repo in write mode
        SQLiteDatabase db = new DBHelper(parentActivity).getWritableDatabase();

        // Create a new map of values, where column names are keys
        ContentValues values = new ContentValues();
        values.put(DBConstants.ScoreEntry.COLUMN_NAME_NAME, name);
        values.put(DBConstants.ScoreEntry.COLUMN_NAME_SCORE, score);

        // Insert the new row, returning the primary key value of the new row
        db.insert(DBConstants.ScoreEntry.TABLE_NAME, null, values);

        finished = true;

        return null;
    }
}
