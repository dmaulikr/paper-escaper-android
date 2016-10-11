package com.studioussoftware.paperescaper.database;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.studioussoftware.paperescaper.interfaces.ILoadedScoresListener;

/**
 * Created by Robbie Wolfe on 10/6/2016.
 */
public class LoadOfflineHighScore extends LoadHighScore {

    public LoadOfflineHighScore(Activity parent, ILoadedScoresListener listener) {
        super(parent, listener);
    }

    @Override
    protected String doInBackground(String... strings) {
        SQLiteDatabase db = new DBHelper(parentActivity).getReadableDatabase();

        // Define a projection that specifies which columns from the db will be used after this query
        String[] projection = { DBConstants.ScoreEntry.COLUMN_NAME_NAME, DBConstants.ScoreEntry.COLUMN_NAME_SCORE };

        // How the results should be sorted in the resulting Cursor
        String sortOrder = DBConstants.ScoreEntry.COLUMN_NAME_SCORE + " DESC";

        Cursor c = db.query(DBConstants.ScoreEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

        int i = 0;
        if (!c.moveToFirst()) {
            return null;
        }
        do {
            String name = c.getString(c.getColumnIndex(DBConstants.ScoreEntry.COLUMN_NAME_NAME));
            String score = c.getString(c.getColumnIndex(DBConstants.ScoreEntry.COLUMN_NAME_SCORE));
            scoresList.add(new Pair<>(name, score));
        }
        while (++i < DBConstants.NUM_HIGHSCORES_TO_SHOW && c.moveToNext());     // Stop when out of entries or displayed max #

        return null;
    }
}
