package com.studioussoftware.paperescaper.http;

import android.util.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Robbie Wolfe on 10/10/2016.
 */
public class HttpConstants {
    public static final int HTTP_OK = 200;
    public static final String HTTP_CHARSET = "UTF-8";
    public static final String ACCEPT_CHARSET_NAME = "Accept-Charset";
    public static final String CONTENT_TYPE_NAME = "Content-Type";
    public static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded;charset=" + HTTP_CHARSET;
    public static final String getQueryString(List<Pair<String, String>> paramList) throws UnsupportedEncodingException {
        String formatString = "";
        for (Pair<String, String> pair : paramList) {
            formatString += String.format("%s=%s&", URLEncoder.encode(pair.first, HTTP_CHARSET), URLEncoder.encode(pair.second, HTTP_CHARSET));
        }
        return formatString.substring(0, formatString.length() - 1);    // Remove last '&'
    }
}
