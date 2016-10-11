package com.studioussoftware.paperescaper.interfaces;

import android.util.Pair;

import java.util.List;

/**
 * Created by Robbie Wolfe on 10/6/2016.
 */
public interface ILoadedScoresListener {
    void populateTable(List<Pair<String, String>> scores);
}
