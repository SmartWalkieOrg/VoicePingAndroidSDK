package com.smartwalkie.voiceping;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private ConnectTask connectTask = null;

    private AutoCompleteTextView usernameText;
    private EditText serverAddressText;
    private View progressView;
    private View connectFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameText = (AutoCompleteTextView) findViewById(R.id.username_text);

        serverAddressText = (EditText) findViewById(R.id.server_address_text);
        serverAddressText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.connect || id == EditorInfo.IME_NULL) {
                    attemptToLogin();
                    return true;
                }
                return false;
            }
        });

        Button connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptToLogin();
            }
        });

        connectFormView = findViewById(R.id.connect_form);
        progressView = findViewById(R.id.connect_progress);
    }

    /**
     * Attempts to connect using details specified by the form.
     * If there are form errors, the errors are presented and no actual login attempt is made.
     */
    private void attemptToLogin() {
        if (connectTask != null) {
            return;
        }

        // Reset errors.
        usernameText.setError(null);
        serverAddressText.setError(null);

        // Store values at the time of the connect attempt.
        String username = usernameText.getText().toString();
        String serverAddress = serverAddressText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid server address, if the user entered one.
        if (TextUtils.isEmpty(serverAddress)) {
            serverAddressText.setError(getString(R.string.error_invalid_server_address));
            focusView = serverAddressText;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            usernameText.setError(getString(R.string.error_field_required));
            focusView = usernameText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt connect and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user connect attempt.
            showProgress(true);
            connectTask = new ConnectTask(username, serverAddress);
            connectTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the connect form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            connectFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            connectFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    connectFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            connectFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String serverAddress;

        ConnectTask(String username, String serverAddress) {
            this.username = username;
            this.serverAddress = serverAddress;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            connectTask = null;
            showProgress(false);

            if (success) {
                Intent activityIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(activityIntent);

                Intent serviceIntent = new Intent(LoginActivity.this, MainService.class);
                startService(serviceIntent);

                finish();
            } else {
                serverAddressText.setError(getString(R.string.error_invalid_server_address));
                serverAddressText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            connectTask = null;
            showProgress(false);
        }
    }
}