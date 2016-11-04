package com.studioussoftware;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.studioussoftware.paperescaper.R;

/**
 * Splash screen that appears at startup of StudiousSoftware apps
 * @author robbie
 *
 */
public class SplashActivity extends Activity {
	private final int SPLASH_DISPLAY_LENGTH = 5000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.splash_screen);
		
		Typeface custom_font = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.splash_font));
		((TextView) findViewById(R.id.splashText)).setTypeface(custom_font);
		
		// New Handler to close this splash screen after some seconds
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				SplashActivity.this.finish();
			}
		}, SPLASH_DISPLAY_LENGTH);
	}
}
