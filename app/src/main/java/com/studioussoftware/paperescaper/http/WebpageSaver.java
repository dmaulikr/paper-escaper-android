package com.studioussoftware.paperescaper.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.studioussoftware.paperescaper.interfaces.IWebpageSavedListener;

/**
 * Created by Robbie Wolfe on 11/18/2016.
 * Loads a given URL into a Bitmap to be used as a Paper page
 */
public class WebpageSaver {
    private WebView view;
    private IWebpageSavedListener listener;

    public WebpageSaver(final Context context) {
        view = new WebView(context);
        view.setVisibility(View.INVISIBLE);
        view.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url) {
                Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                view.draw(c);
                if (listener != null) {
                    listener.onWebpageSaved(b);
                }
            }
        });
    }

    public View getView() {
        return view;
    }

    public void savePage(String url, IWebpageSavedListener listener) {
        this.listener = listener;
        view.loadUrl(url);
    }
}
