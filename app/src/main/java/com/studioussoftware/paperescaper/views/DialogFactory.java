package com.studioussoftware.paperescaper.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.studioussoftware.paperescaper.R;

/**
 * Created by Robbie Wolfe on 10/7/2016.
 */
public class DialogFactory {

    /**
     * Convenience function to show an alert dialog with a title/message
     * @param parentActivity
     * @param title
     * @param message
     */
    public static void showAlertDialog(final Activity parentActivity, final String title, final String message) {
        showAlertDialog(parentActivity, title, message, null);
    }

    /**
     * Convenience function to show an alert dialog with a title/message and onclick listener
     * @param parentActivity
     * @param title
     * @param message
     */
    public static void showAlertDialog(final Activity parentActivity, final String title, final String message,
                                       final OnClickListener listener) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(parentActivity)
                        .setTitle(title)
                        .setMessage(message)
                        .setNeutralButton("OK", listener)
                        .create().show();
            }
        });
    }

    /**
     * Convenience function to show a dialog asking the user for their name and if they
     * want to store their highscores to the online database
     * @param parentActivity
     * @param onPositive
     * @param onNegative
     */
    public static void showSaveHighscoreDialog(Activity parentActivity, OnClickListener onPositive, OnClickListener onNegative) {
        // EditText box and CheckBox view
        View view = View.inflate(parentActivity, R.layout.edittext_checkbox, null);
        final CheckBox onlineCheckbox = (CheckBox) view.findViewById(R.id.checkbox_for_dialog);
        onlineCheckbox.setText(R.string.store_online);

        final EditText editText = (EditText) view.findViewById(R.id.edit_text);

        final AlertDialog saveScoreDialog = new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.store_score_title)
                .setMessage(R.string.store_score_message)
                .setView(view)
                .setPositiveButton(R.string.store_submit, onPositive)
                .setNegativeButton(R.string.store_cancel, onNegative).create();

        // To prevent empty names from being submitted, enable the PositiveButton depending
        // on if the EditText box has text in it
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (saveScoreDialog != null) {
                    Button b = saveScoreDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setEnabled(editText.length() > 0);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        saveScoreDialog.show();

        // Disable button until they input their name
        saveScoreDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }
}
