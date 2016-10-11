package com.studioussoftware.paperescaper.database;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Pair;

import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.http.HttpClient;
import com.studioussoftware.paperescaper.http.HttpConstants;
import com.studioussoftware.paperescaper.http.HttpPost;
import com.studioussoftware.paperescaper.interfaces.ILoadedScoresListener;
import com.studioussoftware.paperescaper.views.DialogFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robbie Wolfe on 10/6/2016.
 */
public class LoadOnlineHighScores extends LoadHighScore {

    public LoadOnlineHighScores(Activity parent, ILoadedScoresListener listener) {
        super(parent, listener);
    }

    @Override
    protected String doInBackground(String... strings) {
        List<Pair<String, String>> paramList = new ArrayList<>();
        paramList.add(new Pair<>(DBConstants.user_name, DBConstants.user_value));
        paramList.add(new Pair<>(DBConstants.password_name, DBConstants.password_value));
        paramList.add(new Pair<>(DBConstants.db_name, DBConstants.db_value));

        HttpClient httpClient = new HttpClient();
        Resources r = parentActivity.getResources();

        try {
            HttpPost postRequest = new HttpPost(DBConstants.request_url);
            postRequest.setEntity(paramList);
            httpClient.execute(postRequest);

            if (postRequest.getResponseCode() == HttpConstants.HTTP_OK) {
                String responseString = httpClient.getResponseString();

                // Handle the response
                JSONObject json = new JSONObject(responseString);
                int success = json.getInt(DBConstants.json_success_tag);
                if (success == DBConstants.json_success) {
                    JSONArray scores = json.getJSONArray(DBConstants.json_scores_tag);

                    for (int i = 0; i < scores.length(); ++i) {
                        JSONObject o = scores.getJSONObject(i);

                        String name = o.getString(DBConstants.json_name_tag);
                        String score = o.getString(DBConstants.json_score_tag);

                        scoresList.add(new Pair<>(name, score));
                    }
                } else {
                    // Something bad happened
                    DialogFactory.showAlertDialog(parentActivity,
                            r.getString(R.string.retrieval_error_title), r.getString(R.string.retrieval_error_message));
                }
            }
            else {
                postRequest.disconnect();
                throw new IOException(postRequest.getResponseMessage());
            }

        } catch (IOException e) {

        } catch (JSONException e) {

        }

        return null;
    }
}
