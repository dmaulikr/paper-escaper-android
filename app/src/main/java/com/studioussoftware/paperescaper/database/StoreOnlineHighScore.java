package com.studioussoftware.paperescaper.database;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Pair;

import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.http.HttpClient;
import com.studioussoftware.paperescaper.http.HttpConstants;
import com.studioussoftware.paperescaper.http.HttpPost;
import com.studioussoftware.paperescaper.interfaces.IScoreStoredListener;
import com.studioussoftware.paperescaper.views.DialogFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robbie Wolfe on 10/10/2016.
 */
public class StoreOnlineHighScore extends StoreHighScore {

    public StoreOnlineHighScore(Activity parent, IScoreStoredListener listener, String name, int score) {
        super(parent, listener, name, score, parent.getResources().getString(R.string.storing_online_scores));
    }

    @Override
    protected String doInBackground(String... strings) {
        List<Pair<String, String>> paramList = new ArrayList<>();
        paramList.add(new Pair<>(DBConstants.user_name, DBConstants.user_value));
        paramList.add(new Pair<>(DBConstants.password_name, DBConstants.password_value));
        paramList.add(new Pair<>(DBConstants.name_name, name));
        paramList.add(new Pair<>(DBConstants.score_name, String.valueOf(score)));
        paramList.add(new Pair<>(DBConstants.db_name, DBConstants.db_value));

        HttpClient httpClient = new HttpClient();
        Resources r = parentActivity.getResources();

        try {
            HttpPost postRequest = new HttpPost(DBConstants.post_url);
            postRequest.setEntity(paramList);
            httpClient.execute(postRequest);

            if (postRequest.getResponseCode() == HttpConstants.HTTP_OK) {
                String responseString = httpClient.getResponseString();

                JSONObject json = new JSONObject(responseString);
                int success = json.getInt(DBConstants.json_success_tag);
                if (success == DBConstants.json_success) {
                    finished = true;
                }
                else {
                    // Something bad happened
                    DialogFactory.showAlertDialog(parentActivity,
                        r.getString(R.string.storage_error_title), r.getString(R.string.storage_error_message));
                }
            }
            else {
                postRequest.disconnect();
                throw new IOException(postRequest.getResponseMessage());
            }
        } catch (IOException e) {
            DialogFactory.showAlertDialog(parentActivity,
                    r.getString(R.string.connection_error_title), r.getString(R.string.connection_error_message));
        } catch (JSONException e) {
            finished = true;    // TODO
        }

        return null;
    }
}
