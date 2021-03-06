package tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.Helpers.RestCall;
import tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.Helpers.SettingsActivity;
import tecnoinf.proyecto.grupo4.usbusdroid3.usbusdroidtrip.R;

public class LoginActivity extends AppCompatActivity {

    private static String loginURL;
    private SharedPreferences sharedPreferences;
    private String saved_username;
    private String saved_password;
    private String saved_tenantId;
    private UserLoginTask mAuthTask = null;
    private View mProgressView;
    private View mLoginFormView;
    private EditText mPasswordView;
    private ImageButton settingsButton;
    private AutoCompleteTextView mEmailView;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginURL = getString(R.string.URLlogin, getString(R.string.URL_REST_API));
        sharedPreferences = getSharedPreferences("USBusData", Context.MODE_PRIVATE);

        String savedServerIP = sharedPreferences.getString("serverIP", "");
        String savedPort = sharedPreferences.getString("port", "");
        saved_tenantId = sharedPreferences.getString("tenantId", "");

        if (!savedServerIP.isEmpty() && !savedPort.isEmpty()) {
            loginURL = loginURL.replace("10.0.2.2", savedServerIP).replace(":8080", ":"+savedPort);
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        loginButton = (Button) findViewById(R.id.email_sign_in_button);
        assert loginButton != null;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        settingsButton = (ImageButton) findViewById(R.id.settingsBtn);
        assert settingsButton != null;
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password) || password.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            mAuthTask = new UserLoginTask(email, password, getApplicationContext(), "usbus");
            mAuthTask.execute((Void) null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        mProgressView.setVisibility(show? View.VISIBLE : View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            settingsButton.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String mPassword;
        private final String mType;
        private String token;
        private Context mCtx;

        UserLoginTask(String user, String password, Context ctx, String type) {
            username = user;
            mPassword = password;
            mCtx = ctx;
            mType = type;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject result;
            JSONObject registerResult;
            try {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                JSONObject credentials = new JSONObject();
                credentials.put("username", username);
                if(saved_tenantId.isEmpty()) {
                    credentials.put("tenantId", mCtx.getString(R.string.tenantId));
                } else {
                    credentials.put("tenantId", saved_tenantId);
                }
                credentials.put("password", mPassword);

                RestCall call = new RestCall(loginURL, "POST", credentials, null);
                result = call.getData();
                //String dummy = result.toString();
                System.out.println(result);
                if(result.get("result").toString().equalsIgnoreCase("OK")) {
                    //login OK
                    System.out.println("LOGIN OK...");
                    JSONObject data = new JSONObject(result.get("data").toString());
                    token = data.getString("token");

                    editor.putString("token", token);
                    editor.putString("username", username);
                    editor.putString("password", mPassword);
                    if(saved_tenantId.isEmpty()) {
                        editor.putString("tenantId", getString(R.string.tenantId));
                    } else {
                        editor.putString("tenantId", saved_tenantId);
                    }
                    editor.putString("loginURL", loginURL);
                    editor.apply();
                } else {
                    //algun error
                    System.out.println("DANGER WILL ROBINSON..." + result.get("result").toString());
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                showProgress(true);
                Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
//                mainIntent.putExtra("token", token);
//                mainIntent.putExtra("username", username);
                startActivity(mainIntent);

                finish();
            } else {
                showProgress(false);
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
                Intent loginIntent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(loginIntent);

                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
