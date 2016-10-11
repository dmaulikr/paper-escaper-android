package com.studioussoftware.paperescaper.database;

import android.provider.BaseColumns;

/**
 * Created by Robbie Wolfe on 10/6/2016.
 */
public class DBConstants {
    public static final int NUM_HIGHSCORES_TO_SHOW = 20;

    // Online DB Constants
    public static final String request_url = "";
    public static final String post_url = "";
    public static final String db_name = "db";
    public static final String db_value = "";
    public static final String user_name = "user";
    public static final String user_value = "";
    public static final String password_name = "pass";
    public static final String password_value = "";
    public static final String name_name = "name";
    public static final String score_name = "score";

    // Offline DB Constants
    public static abstract class ScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "scores";
        public static final String COLUMN_NAME_NAME = name_name;
        public static final String COLUMN_NAME_SCORE = score_name;
    }
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE" + ScoreEntry.TABLE_NAME + " (" +
        ScoreEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP + " (" +
        ScoreEntry.COLUMN_NAME_NAME + " TEXT" + COMMA_SEP +
        ScoreEntry.COLUMN_NAME_SCORE + " INTEGER" +
        " )";
    public static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + ScoreEntry.TABLE_NAME;

    // JSON Response Constants
    public static final String  json_success_tag    = "success";
    public static final int     json_success        = 1;
    public static final String  json_scores_tag     = "scores";
    public static final String  json_name_tag       = "name";
    public static final String  json_score_tag      = "score";
}
