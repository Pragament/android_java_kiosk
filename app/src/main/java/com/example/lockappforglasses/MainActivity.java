package com.example.lockappforglasses;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;


// import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DevicePolicyManager deviceManger;
    ComponentName compName;
    View view;
    Boolean isAppLockEnabledLocal = true;
    Boolean isChromeModeEnabledLocal = true;
    private static final String TAG = "MainActivity";
    Boolean mIsKioskEnabled = false;
    WebView mWebView;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

        Boolean isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_opt_chrome_mode), true);
        Button buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
        Button buttonBack = (Button) findViewById(R.id.buttonBack);
        LinearLayout llTools = findViewById(R.id.llTools);
        if (!isChromeModeEnabled) {
            //AdblockWebView adblockWebView = findViewById(R.id.main_webview);
            mWebView = findViewById(R.id.webView);
            mWebView.setVisibility(View.VISIBLE);
            //mWebView.setWebChromeClient(new WebChromeClient());
            TextView txtView = findViewById(R.id.txtView);
            txtView.setVisibility(View.VISIBLE);

            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            WebViewClientImpl webViewClient = new WebViewClientImpl(progressBar, txtView, this);
            mWebView.setWebViewClient(webViewClient);
            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);

            //webSettings.setPluginState(WebSettings.PluginState.ON);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setSupportZoom(true);
            webSettings.setDefaultTextEncodingName("utf-8");

            mWebView.loadUrl("https://www.freecodecamp.org/learn/2022/responsive-web-design/");
            //mWebView.loadUrl("file:///android_asset/tpl_one/index.html");
            //mWebView.loadUrl("https://codepen.io/benthedev/pen/rjZPRG?editors=1000");


            //buttonRefresh.setVisibility(View.VISIBLE);
            buttonRefresh.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mWebView.reload();
                }
            });
            //buttonBack.setVisibility(View.VISIBLE);
            buttonBack.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mWebView.goBack();
                }
            });
        }else{
            llTools.setOrientation(LinearLayout.VERTICAL);
            llTools.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
            buttonRefresh.setText("Launch web browser");
            buttonRefresh.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "onClick: 12321");
                    try {
                    //String url = "https://www.freecodecamp.org/learn/2022/responsive-web-design/";
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    //Intent browserIntent = new Intent("com.android.chrome");
                    Intent browserIntent = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
                     //  Log.d(TAG, "onClick: "+browserIntent.getPackage());
                        if (browserIntent != null) {
                            startActivity(browserIntent);
                        }
                    } catch (ActivityNotFoundException e) {
                        // TODO Auto-generated catch block
                        Log.d(TAG, "onClick: ActivityNotFoundException ");
                    }
                }
            });
            buttonBack.setText("New chrome tab with freecodecamp");
            buttonBack.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String url = "https://www.freecodecamp.org/learn/2022/responsive-web-design/";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    startActivity(browserIntent);
                }
            });
        }
        Button buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(MainActivity.this);
                //clearCookies(MainActivity.this);
                //mWebView.reload();
            }
        });
        Spinner websiteSpinner = (Spinner) findViewById(R.id.websiteSpinner);
        websiteSpinner.setOnItemSelectedListener(this);
        view = getWindow().getDecorView();

        if (savedInstanceState == null) {
            showClassCodeDialog();
        }
    }



    private void showClassCodeDialog() {
        View dialogLayout = View.inflate(this, R.layout.dialog_input_class_code, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogLayout)
                .setCancelable(false)
                .setTitle("Classroom Code")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Proceed", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText etClassCode = dialogLayout.findViewById(R.id.et_class_code);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String code = etClassCode.getEditableText().toString().trim();
                    if (!code.isEmpty()) {
                        authenticateClassCode(code, dialog, etClassCode);
                    } else {
                        Toast.makeText(this, "Please enter code!", Toast.LENGTH_SHORT).show();
                    }
                });
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener(v -> {
                    finish();
                });
    }

    private void authenticateClassCode(String code, androidx.appcompat.app.AlertDialog dialog, TextInputEditText etClassCode) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("classrooms")
                .whereEqualTo("classCode", code)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()){
                        Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show();
                        ((MyApp) getApplicationContext()).setCurrentClassCode(code);
                        dialog.dismiss();
                    } else {
                        etClassCode.setError("Invalid code");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                });

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String url = "";
        switch (pos) {
            case 0:
                Toast.makeText(parent.getContext(), "Loading freecodecamp!", Toast.LENGTH_LONG).show();
                url = "https://www.freecodecamp.org/learn/2022/responsive-web-design/";
                break;
            case 1:
                Toast.makeText(parent.getContext(), "Loading Personal Webpage!", Toast.LENGTH_LONG).show();
                //Personal Webpage
                url = "https://codepen.io/SquishyAndroid/pen/XjRPVV?editors=1000";
                break;
            case 2:
                Toast.makeText(parent.getContext(), "Loading Portfolio Page!", Toast.LENGTH_LONG).show();
                //Portfolio Page
                url = "https://codepen.io/benthedev/pen/rjZPRG?editors=1000";
                break;
            case 3:
                Toast.makeText(parent.getContext(), "Loading Portfolio Webpage!", Toast.LENGTH_LONG).show();
                //Portfolio Webpage
                url="https://codepen.io/bbrady/pen/WxqPqx?editors=1000";
                break;
            case 4:
                Toast.makeText(parent.getContext(), "Loading Portfolio Webpage!", Toast.LENGTH_LONG).show();
                //Portfolio Webpage
                url="https://codepen.io/mjakal/pen/rLYbLJ?editors=1000";
                break;
            case 5:
                Toast.makeText(parent.getContext(), "Loading Restaurant Webpage!", Toast.LENGTH_LONG).show();
                //Restaurant Webpage
                url="https://codepen.io/AntenaGames/pen/JjKRKmm?editors=1000";
                break;
        }
        if (!url.isEmpty()) {
            Boolean isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(getString(R.string.pref_opt_chrome_mode), true);
            if (!isChromeModeEnabled) {
                mWebView.loadUrl(url);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void showDialog(Context context) {

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.password_prompt, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText etCurrentPassword = (EditText) promptsView
                .findViewById(R.id.etCurrentPassword);
        final EditText etNewPassword = (EditText) promptsView
                .findViewById(R.id.etNewPassword);
        final EditText etUserName = (EditText) promptsView
                .findViewById(R.id.etUserName);
        Boolean isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_opt_chrome_mode), true);
        final Switch switchChromeMode = (Switch) promptsView
                .findViewById(R.id.switchChromeMode);
        switchChromeMode.setChecked(isChromeModeEnabled);
        switchChromeMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isChromeModeEnabledLocal = true;
                } else {
                    isChromeModeEnabledLocal = false;
                }
            }
        });
        Boolean isAppLockEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_opt_app_lock), true);
        final Switch switchKioskMode = (Switch) promptsView
                .findViewById(R.id.switchKioskMode);
        switchKioskMode.setChecked(isAppLockEnabled);
        switchKioskMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isAppLockEnabledLocal = true;
                } else {
                    isAppLockEnabledLocal = false;
                }
            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                /** DO THE METHOD HERE WHEN PROCEED IS CLICKED*/
                                String etCurrentPasswordText = (etCurrentPassword.getText()).toString();
                                String etNewPasswordText = (etNewPassword.getText()).toString();
                                String etUserNameText = (etUserName.getText()).toString();

                                if (!etUserNameText.trim().isEmpty()) {
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                    preferences.edit().putString("userName", etUserNameText).apply();
                                }

                                /** CHECK FOR USER'S INPUT **/
                                SharedPreferences mySharedPreferences;
                                mySharedPreferences = getSharedPreferences("settingspassworddetails", MODE_PRIVATE);
                                String password = mySharedPreferences.getString("password", "");

                                if (etCurrentPasswordText.equals(password)) {
                                    if (!TextUtils.isEmpty(etNewPasswordText)) {
                                        SharedPreferences.Editor myEditor;
                                        myEditor = mySharedPreferences.edit();
                                        myEditor.putString("password", etNewPasswordText);
                                        myEditor.commit();
                                        //dialog.dismiss();
                                        //return;
                                    }
                                    //Log.d(user_text, "HELLO THIS IS THE MESSAGE CAUGHT :)");
                                    //Search_Tips(user_text);
                                    SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                    SharedPreferences.Editor defaultSharedPreferencesEditor = defaultSharedPreferences.edit();
                                    defaultSharedPreferencesEditor.putBoolean(getString(R.string.pref_opt_app_lock), isAppLockEnabledLocal);
                                    defaultSharedPreferencesEditor.putBoolean(getString(R.string.pref_opt_chrome_mode), isChromeModeEnabledLocal);
                                    defaultSharedPreferencesEditor.commit();
                                } else {
                                    //Log.d(user_text,"string is empty");
                                    dialog.dismiss();
                                    finish();
                                }
                            }
                        })
                .setPositiveButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                finish();
                            }

                        }

                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d(TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onBackPressed() {
        // nothing to do here
        // … really
        //showDialog(MainActivity.this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void hideSystemUI() {

        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}