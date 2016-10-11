package com.studioussoftware.paperescaper.http;

import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Robbie Wolfe on 10/10/2016.
 */
public class HttpPost {

    private HttpURLConnection connection;
    private List<Pair<String, String>> paramList;

    public HttpPost(String requestUrl) throws IOException {
        connection = (HttpURLConnection) new URL(requestUrl).openConnection();
        connection.setDoOutput(true);      // Triggers HTTP POST
        connection.setRequestProperty(HttpConstants.ACCEPT_CHARSET_NAME, HttpConstants.HTTP_CHARSET);
        connection.setRequestProperty(HttpConstants.CONTENT_TYPE_NAME, HttpConstants.CONTENT_TYPE_VALUE);
    }

    /**
     * Sets the list of parameters to send in the HttpPost request
     * @param paramList
     */
    public void setEntity(List<Pair<String, String>> paramList) {
        this.paramList = paramList;
    }

    public int getResponseCode() throws IOException {
        return connection.getResponseCode();
    }

    public String getResponseMessage() throws IOException {
        return connection.getResponseMessage();
    }

    /**
     * Executes the HttpPost request and returns the response data
     * @throws IOException
     * @return InputStream
     */
    public InputStream execute() throws IOException {
        OutputStream out = connection.getOutputStream();
        out.write(HttpConstants.getQueryString(paramList).getBytes(HttpConstants.HTTP_CHARSET));
        return connection.getInputStream();     // Executes the request as well
    }

    public void disconnect() {
        connection.disconnect();
    }
}
