package com.studioussoftware.paperescaper.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.studioussoftware.SplashActivity;
import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.database.LoadOfflineHighScore;
import com.studioussoftware.paperescaper.database.LoadOnlineHighScores;
import com.studioussoftware.paperescaper.interfaces.ILoadedScoresListener;
import com.studioussoftware.paperescaper.views.BorderedTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Robbie Wolfe on 10/11/2016.
 */
public class MenuActivity extends Activity implements ILoadedScoresListener {

    private final String SPLASH_KEY = "ShowedSplash";
    private View currentView, scroller, menuLayout, instructionsLayout, highscoresLayout, creditsLayout, creditsLink;
    private View menuStart, menuInstructions, menuHighScores, menuCredits;  // Menu buttons
    private Map<View, Button> backButtons;
    private int transitionTime = 0;

    private TableLayout highscoresTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null || !savedInstanceState.getBoolean(SPLASH_KEY)) {
            // Launch the splash screen if didn't already
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }

        transitionTime = getResources().getInteger(android.R.integer.config_longAnimTime);

        setContentView(R.layout.main_menu);
        setupFonts();

        menuLayout = findViewById(R.id.menu_layout);
        instructionsLayout = findViewById(R.id.instructions_layout);
        highscoresLayout = findViewById(R.id.highscores_layout);
        creditsLayout = findViewById(R.id.credits_layout);
        currentView = menuLayout;

        menuStart = findViewById(R.id.start);
        menuInstructions = findViewById(R.id.instructions);
        menuHighScores = findViewById(R.id.highscores);
        menuCredits = findViewById(R.id.credits);

        // Need to easily be able to access back buttons to disable their functionality when hidden
        backButtons = new HashMap<View, Button>();
        backButtons.put(menuLayout, null);      // Help know if updating menuLayout
        backButtons.put(instructionsLayout, (Button) findViewById(R.id.instructions_back_button));
        backButtons.put(highscoresLayout, (Button) findViewById(R.id.highscores_back_button));
        backButtons.put(creditsLayout, (Button) findViewById(R.id.credits_back_button));

        scroller = findViewById(R.id.scroller);

        // Enable clickable link in credits page
        TextView creditsLink = (TextView)findViewById(R.id.credits_link);
        creditsLink.setMovementMethod(LinkMovementMethod.getInstance());
        this.creditsLink = creditsLink;

        // Enable ToggleButton's RadioGroup functionality to work properly
        ((RadioGroup)findViewById(R.id.highscores_radio_group)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < group.getChildCount(); ++i) {
                    final ToggleButton b = (ToggleButton)group.getChildAt(i);
                    b.setChecked(b.getId() == checkedId);
                }
            }
        });
        highscoresTable = (TableLayout)findViewById(R.id.highscores_table);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SPLASH_KEY, true);
        super.onSaveInstanceState(outState);
    }

    private void setupFonts() {
        Typeface font = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.menu_font));
        ((TextView)findViewById(R.id.gametitle)).setTypeface(font);
        ((TextView)findViewById(R.id.instructions_title)).setTypeface(font);
        ((TextView)findViewById(R.id.highscores_title)).setTypeface(font);
        ((TextView)findViewById(R.id.credits_title)).setTypeface(font);
    }

    public void onStartGameClick(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    /**
     * Default is fading out menuLayout
     * @param newView
     */
    private void fadeSwapViews(final View newView) {
        fadeSwapViews(menuLayout, newView);
    }

    /**
     * Fades out currentView and fades in newView
     * @param currentView
     * @param newView
     */
    private void fadeSwapViews(final View currentView, final View newView) {
        // Set alpha of newView layout to 0 (setAlpha not available in minSdk), but Visible
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 0.0f);
        alpha.setDuration(0);
        alpha.setFillAfter(true);   // persist after animation ends
        newView.setAnimation(alpha);
        newView.setVisibility(View.VISIBLE);

        // Set otherwise GONE children views to visible (see below)
        Button backButton = backButtons.get(newView);
        showButtons(backButton, newView);

        if (newView == highscoresLayout) {
            scroller.setVisibility(View.VISIBLE);
        }
        else if (newView == creditsLayout) {
            creditsLink.setVisibility(View.VISIBLE);
        }

        // Animate to 100% opacity
        alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(transitionTime);
        alpha.setFillAfter(true);
        newView.setAnimation(alpha);

        // Fade out current view
        alpha = new AlphaAnimation(1.0f, 0.0f);
        alpha.setDuration(transitionTime);
        alpha.setFillAfter(true);
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                currentView.setVisibility(View.GONE);
                // Avoid a strange 'feature' where the parent view is GONE but Buttons/ScrollView still receive touch events
                Button backButton = backButtons.get(currentView);
                hideButtons(backButton, currentView);
                if (currentView == highscoresLayout) {
                    scroller.setVisibility(View.GONE);
                }
                else if (currentView == creditsLayout) {
                    creditsLink.setVisibility(View.GONE);
                }
            }
        });
        currentView.setAnimation(alpha);

        this.currentView = newView;
    }

    private void hideButtons(View backButton, View currentView) {
        updateButtons(backButton, currentView, View.GONE);
    }

    private void showButtons(View backButton, View newView) {
        updateButtons(backButton, newView, View.VISIBLE);
    }

    private void updateButtons(View backButton, View view, int visibility) {
        if (backButton != null) {
            backButton.setVisibility(visibility);
        }
        if (view == menuLayout) {
            menuStart.setVisibility(visibility);
            menuInstructions.setVisibility(visibility);
            menuHighScores.setVisibility(visibility);
            menuCredits.setVisibility(visibility);
        }
    }

    public void onInstructionsClick(View v) {
        fadeSwapViews(instructionsLayout);
    }

    public void onHighScoresClick(View v) {
        fadeSwapViews(highscoresLayout);
        if (((ToggleButton)findViewById(R.id.online_toggle)).isChecked()) {
            new LoadOnlineHighScores(this, this).execute();
        }
        else {
            new LoadOfflineHighScore(this, this).execute();
        }
    }

    @Override
    public void populateTable(List<Pair<String, String>> scores) {
        highscoresTable.removeAllViews();
        TableRow row;
        BorderedTextView t0, t1, t2;
        // Converting to dip unit
        int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 1, getResources().getDisplayMetrics());
        final int textPadding = 5 * dip;

        final float fontSize = getResources().getDimension(R.dimen.highscores_entry_size);
        final int fontColor = ContextCompat.getColor(this, R.color.menuTextColor);

        for (int current = 0; current < scores.size(); ++current) {
            row = new TableRow(this);

            t0 = new BorderedTextView(this);
            t0.setPadding(textPadding, textPadding, textPadding, textPadding);
            t0.setTextColor(fontColor);
            t0.setTextSize(fontSize);
            t0.setGravity(Gravity.RIGHT);

            Pair<String, String> value = scores.get(current);
            t1 = new BorderedTextView(this);
            t1.setPadding(textPadding, textPadding, textPadding, textPadding);
            t1.setTextColor(fontColor);
            t1.setText(value.first);

            t2 = new BorderedTextView(this);
            t2.setPadding(textPadding, textPadding, textPadding, textPadding);
            t2.setTextColor(fontColor);
            t2.setText(value.second);
            t2.setTextSize(fontSize);
            t2.setGravity(Gravity.RIGHT);

            row.addView(t0);
            row.addView(t1);
            row.addView(t2);

            highscoresTable.addView(row, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    public void onCreditsClick(View v) {
        fadeSwapViews(creditsLayout);
    }

    public void onBackButtonClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (currentView == menuLayout) {
            super.onBackPressed();
        }
        else {
            fadeSwapViews(currentView, menuLayout);
        }
    }

    public void onToggle(View v) {
        ((RadioGroup)v.getParent()).check(v.getId());

        if (v.getId() == R.id.online_toggle) {
            new LoadOnlineHighScores(this, this).execute();
        }
        else {
            new LoadOfflineHighScore(this, this).execute();
        }
    }
}
