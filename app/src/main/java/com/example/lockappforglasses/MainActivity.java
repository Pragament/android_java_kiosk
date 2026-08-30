package com.example.lockappforglasses;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;


// import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DevicePolicyManager deviceManger;
    ComponentName compName;
    View view;
    Boolean isAppLockEnabledLocal = true;
    Boolean isChromeModeEnabledLocal = true;
    private static final String TAG = "MainActivity";
    private static final String CLASSROOMS_COLLECTION = "classrooms";
    private static final String CLASS_CODE_FIELD = "classCode";
    private static final String WHITELISTED_APPS_COLLECTION = "whitelistedApps";
    private static final String WHITELISTED_WEBSITES_COLLECTION = "whitelistedWebsites";
    Boolean mIsKioskEnabled = false;
    WebView mWebView;
    LinearLayout llWhitelistedApps;
    LinearLayout llWhitelistedWebsites;
    ScrollView whitelistedAppsScrollView;
    ScrollView whitelistedWebsitesScrollView;
    TextView txtWhitelistedAppsTitle;
    TextView txtWhitelistedWebsitesTitle;
    Button buttonRefresh;
    Button buttonBack;
    ProgressBar progressBar;
    TextView txtView;
    LinearLayout llTools;
    Boolean isChromeModeEnabled;
    boolean isWhitelistedAppsExpanded = true;
    boolean isWhitelistedWebsitesExpanded = true;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

        isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_opt_chrome_mode), true);
        buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
        buttonBack = (Button) findViewById(R.id.buttonBack);
        llTools = findViewById(R.id.llTools);
        llWhitelistedApps = findViewById(R.id.llWhitelistedApps);
        llWhitelistedWebsites = findViewById(R.id.llWhitelistedWebsites);
        whitelistedAppsScrollView = findViewById(R.id.whitelistedAppsScrollView);
        whitelistedWebsitesScrollView = findViewById(R.id.whitelistedWebsitesScrollView);
        txtWhitelistedAppsTitle = findViewById(R.id.txtWhitelistedAppsTitle);
        txtWhitelistedWebsitesTitle = findViewById(R.id.txtWhitelistedWebsitesTitle);
        txtWhitelistedAppsTitle.setOnClickListener(v -> toggleWhitelistedApps());
        txtWhitelistedWebsitesTitle.setOnClickListener(v -> toggleWhitelistedWebsites());
        setupWebView();
        if (!isChromeModeEnabled) {
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
                    String url = "http://levelup.technikh.com/";
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

    private void setupWebView() {
        mWebView = findViewById(R.id.webView);
        mWebView.setVisibility(View.VISIBLE);
        txtView = findViewById(R.id.txtView);
        txtView.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        WebViewClientImpl webViewClient = new WebViewClientImpl(progressBar, txtView, this);
        mWebView.setWebViewClient(webViewClient);
        mWebView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
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
        Log.d(TAG, "authenticateClassCode: entered code='" + code + "', length=" + code.length());

        if (!hasNetworkConnection()) {
            Log.w(TAG, "authenticateClassCode: no active network connection");
            showClassCodeError(etClassCode, "No internet connection");
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Log.d(TAG, "authenticateClassCode: checking direct classroom path /" + CLASSROOMS_COLLECTION + "/" + code);
        firestore.collection(CLASSROOMS_COLLECTION)
                .document(code)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "authenticateClassCode: direct document id=" + documentSnapshot.getId()
                            + ", exists=" + documentSnapshot.exists()
                            + ", fromCache=" + documentSnapshot.getMetadata().isFromCache()
                            + ", pendingWrites=" + documentSnapshot.getMetadata().hasPendingWrites()
                            + ", data=" + documentSnapshot.getData());
                    if (documentSnapshot.exists()) {
                        onClassCodeVerified(documentSnapshot.getId(), dialog);
                    } else {
                        authenticateClassCodeField(firestore, code, dialog, etClassCode, false);
                    }
                })
                .addOnFailureListener(e -> {
                    handleClassCodeLookupFailure(etClassCode, e);
                });
    }

    private void authenticateClassCodeField(FirebaseFirestore firestore, String code, androidx.appcompat.app.AlertDialog dialog, TextInputEditText etClassCode, boolean numericQuery) {
        Object classCode = code;
        if (numericQuery) {
            try {
                classCode = Long.parseLong(code);
            } catch (NumberFormatException e) {
                Log.w(TAG, "authenticateClassCodeField: code is not numeric: " + code, e);
                etClassCode.setError("Invalid code");
                return;
            }
        }

        Log.d(TAG, "authenticateClassCodeField: querying /" + CLASSROOMS_COLLECTION
                + " where " + CLASS_CODE_FIELD + " == " + classCode
                + " (" + classCode.getClass().getSimpleName() + ")");
        firestore.collection(CLASSROOMS_COLLECTION)
                .whereEqualTo(CLASS_CODE_FIELD, classCode)
                .get()
                .addOnSuccessListener(querySnapshot -> handleClassCodeQueryResult(firestore, code, dialog, etClassCode, numericQuery, querySnapshot))
                .addOnFailureListener(e -> {
                    handleClassCodeLookupFailure(etClassCode, e);
                });
    }

    private void handleClassCodeQueryResult(FirebaseFirestore firestore, String code, androidx.appcompat.app.AlertDialog dialog, TextInputEditText etClassCode, boolean numericQuery, QuerySnapshot querySnapshot) {
        Log.d(TAG, "handleClassCodeQueryResult: numericQuery=" + numericQuery
                + ", size=" + querySnapshot.size()
                + ", fromCache=" + querySnapshot.getMetadata().isFromCache()
                + ", pendingWrites=" + querySnapshot.getMetadata().hasPendingWrites());
        for (DocumentSnapshot classroom : querySnapshot.getDocuments()) {
            Log.d(TAG, "handleClassCodeQueryResult: matched classroom id=" + classroom.getId()
                    + ", exists=" + classroom.exists()
                    + ", data=" + classroom.getData());
        }

        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot classroom = querySnapshot.getDocuments().get(0);
            onClassCodeVerified(classroom.getId(), dialog);
        } else if (!numericQuery && TextUtils.isDigitsOnly(code)) {
            Log.d(TAG, "handleClassCodeQueryResult: string query empty; retrying as numeric code");
            authenticateClassCodeField(firestore, code, dialog, etClassCode, true);
        } else {
            Log.w(TAG, "handleClassCodeQueryResult: no classroom found for code=" + code);
            etClassCode.setError("Invalid code");
        }
    }

    private void onClassCodeVerified(String classroomId, androidx.appcompat.app.AlertDialog dialog) {
        Log.i(TAG, "onClassCodeVerified: classroomId=" + classroomId);
        Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show();
        ((MyApp) getApplicationContext()).setCurrentClassCode(classroomId);
        dialog.dismiss();
        loadWhitelistedApps(classroomId);
    }

    private void loadWhitelistedApps(String classroomId) {
        showWebViewToolbar();
        showWebViewContent();
        showWhitelistedAppsMessage("Loading apps...");
        showWhitelistedWebsitesMessage("Loading websites...");

        String whitelistPath = "/" + CLASSROOMS_COLLECTION + "/" + classroomId + "/" + WHITELISTED_APPS_COLLECTION;
        Log.i(TAG, "loadWhitelistedApps: querying " + whitelistPath);
        FirebaseFirestore.getInstance()
                .collection(CLASSROOMS_COLLECTION)
                .document(classroomId)
                .collection(WHITELISTED_APPS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> showWhitelistedApps(querySnapshot))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "loadWhitelistedApps: unable to load whitelisted apps from " + whitelistPath, e);
                    showWhitelistedAppsMessage("Unable to load apps. Check internet or DNS.");
                });
        loadWhitelistedWebsites(classroomId);
    }

    private void loadWhitelistedWebsites(String classroomId) {
        String whitelistPath = "/" + CLASSROOMS_COLLECTION + "/" + classroomId + "/" + WHITELISTED_WEBSITES_COLLECTION;
        Log.i(TAG, "loadWhitelistedWebsites: querying " + whitelistPath);
        FirebaseFirestore.getInstance()
                .collection(CLASSROOMS_COLLECTION)
                .document(classroomId)
                .collection(WHITELISTED_WEBSITES_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> showWhitelistedWebsites(querySnapshot))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "loadWhitelistedWebsites: unable to load whitelisted websites from " + whitelistPath, e);
                    showWhitelistedWebsitesMessage("Unable to load websites. Check internet or DNS.");
                });
    }

    private void showWhitelistedApps(QuerySnapshot querySnapshot) {
        Log.i(TAG, "showWhitelistedApps: size=" + querySnapshot.size()
                + ", fromCache=" + querySnapshot.getMetadata().isFromCache()
                + ", pendingWrites=" + querySnapshot.getMetadata().hasPendingWrites());

        llWhitelistedApps.removeAllViews();
        whitelistedAppsScrollView.setVisibility(View.VISIBLE);
        txtWhitelistedAppsTitle.setVisibility(View.VISIBLE);
        updateWhitelistedAppsTitle();

        if (querySnapshot.isEmpty()) {
            Log.w(TAG, "showWhitelistedApps: query returned empty whitelist");
            addWhitelistedAppsMessage("No whitelisted apps found.");
            return;
        }

        for (DocumentSnapshot appDocument : querySnapshot.getDocuments()) {
            Log.i(TAG, "showWhitelistedApps: whitelist package=" + appDocument.getId()
                    + ", exists=" + appDocument.exists()
                    + ", fromCache=" + appDocument.getMetadata().isFromCache()
                    + ", data=" + appDocument.getData());
            addWhitelistedAppButton(appDocument.getId());
        }
    }

    private void showWhitelistedAppsMessage(String message) {
        showWebViewContent();
        whitelistedAppsScrollView.setVisibility(View.VISIBLE);
        txtWhitelistedAppsTitle.setVisibility(View.VISIBLE);
        updateWhitelistedAppsTitle();
        llWhitelistedApps.removeAllViews();
        addWhitelistedAppsMessage(message);
    }

    private void showWhitelistedWebsites(QuerySnapshot querySnapshot) {
        Log.i(TAG, "showWhitelistedWebsites: size=" + querySnapshot.size()
                + ", fromCache=" + querySnapshot.getMetadata().isFromCache()
                + ", pendingWrites=" + querySnapshot.getMetadata().hasPendingWrites());

        llWhitelistedWebsites.removeAllViews();
        whitelistedWebsitesScrollView.setVisibility(View.VISIBLE);
        txtWhitelistedWebsitesTitle.setVisibility(View.VISIBLE);
        updateWhitelistedWebsitesTitle();

        if (querySnapshot.isEmpty()) {
            Log.w(TAG, "showWhitelistedWebsites: query returned empty whitelist");
            addWhitelistedWebsitesMessage("No whitelisted websites found.");
            return;
        }

        for (DocumentSnapshot websiteDocument : querySnapshot.getDocuments()) {
            String url = websiteDocument.getString("url");
            String title = websiteDocument.getString("title");
            String host = websiteDocument.getString("host");
            Log.i(TAG, "showWhitelistedWebsites: website id=" + websiteDocument.getId()
                    + ", url=" + url
                    + ", title=" + title
                    + ", host=" + host
                    + ", fromCache=" + websiteDocument.getMetadata().isFromCache()
                    + ", data=" + websiteDocument.getData());
            addWhitelistedWebsiteButton(websiteDocument.getId(), title, host, url);
        }
    }

    private void showWhitelistedWebsitesMessage(String message) {
        showWebViewContent();
        whitelistedWebsitesScrollView.setVisibility(View.VISIBLE);
        txtWhitelistedWebsitesTitle.setVisibility(View.VISIBLE);
        updateWhitelistedWebsitesTitle();
        llWhitelistedWebsites.removeAllViews();
        addWhitelistedWebsitesMessage(message);
    }

    private void showWebViewContent() {
        if (mWebView == null) {
            return;
        }

        mWebView.setVisibility(View.VISIBLE);
        if (mWebView.getUrl() == null || "about:blank".equals(mWebView.getUrl())) {
            mWebView.loadUrl("http://levelup.technikh.com/");
        }
    }

    private void showWebViewToolbar() {
        llTools.setOrientation(LinearLayout.HORIZONTAL);
        buttonRefresh.setVisibility(View.VISIBLE);
        buttonBack.setVisibility(View.VISIBLE);
        buttonRefresh.setText("Refresh");
        buttonBack.setText("Back");

        buttonRefresh.setOnClickListener(v -> {
            if (mWebView != null) {
                mWebView.reload();
            }
        });
        buttonBack.setOnClickListener(v -> {
            if (mWebView != null && mWebView.canGoBack()) {
                mWebView.goBack();
            }
        });
    }

    private void toggleWhitelistedApps() {
        isWhitelistedAppsExpanded = !isWhitelistedAppsExpanded;
        whitelistedAppsScrollView.setVisibility(isWhitelistedAppsExpanded ? View.VISIBLE : View.GONE);
        updateWhitelistedAppsTitle();
    }

    private void toggleWhitelistedWebsites() {
        isWhitelistedWebsitesExpanded = !isWhitelistedWebsitesExpanded;
        whitelistedWebsitesScrollView.setVisibility(isWhitelistedWebsitesExpanded ? View.VISIBLE : View.GONE);
        updateWhitelistedWebsitesTitle();
    }

    private void updateWhitelistedAppsTitle() {
        txtWhitelistedAppsTitle.setText(isWhitelistedAppsExpanded ? "Whitelisted Apps [-]" : "Whitelisted Apps [+]");
    }

    private void updateWhitelistedWebsitesTitle() {
        txtWhitelistedWebsitesTitle.setText(isWhitelistedWebsitesExpanded ? "Whitelisted Websites [-]" : "Whitelisted Websites [+]");
    }

    private void hideWebViewStatus() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (txtView != null) {
            txtView.setVisibility(View.GONE);
        }
    }

    private void addWhitelistedAppsMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setTextSize(16);
        textView.setPadding(0, 8, 0, 8);
        llWhitelistedApps.addView(textView);
    }

    private void addWhitelistedWebsitesMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setTextSize(16);
        textView.setPadding(0, 8, 0, 8);
        llWhitelistedWebsites.addView(textView);
    }

    private void addWhitelistedAppButton(String packageName) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(getAppDisplayName(packageName));
        button.setOnClickListener(v -> launchWhitelistedApp(packageName));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 8, 0, 8);
        llWhitelistedApps.addView(button, layoutParams);
    }

    private void addWhitelistedWebsiteButton(String documentId, String title, String host, String url) {
        String websiteUrl = getWebsiteUrl(documentId, host, url);
        String buttonText = getWebsiteDisplayName(documentId, title, host, websiteUrl);

        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(buttonText);
        button.setOnClickListener(v -> openWhitelistedWebsite(websiteUrl));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 8, 0, 8);
        llWhitelistedWebsites.addView(button, layoutParams);
    }

    private String getWebsiteUrl(String documentId, String host, String url) {
        if (!TextUtils.isEmpty(url)) {
            return url;
        }

        String websiteHost = !TextUtils.isEmpty(host) ? host : documentId;
        if (websiteHost.startsWith("http://") || websiteHost.startsWith("https://")) {
            return websiteHost;
        }
        return "http://" + websiteHost;
    }

    private String getWebsiteDisplayName(String documentId, String title, String host, String url) {
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        if (!TextUtils.isEmpty(host)) {
            return host;
        }
        if (!TextUtils.isEmpty(url)) {
            return url;
        }
        return documentId;
    }

    private void openWhitelistedWebsite(String url) {
        Log.i(TAG, "openWhitelistedWebsite: url=" + url);
        showWebViewContent();
        mWebView.loadUrl(url);
    }

    private String getAppDisplayName(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            Log.d(TAG, "getAppDisplayName: package=" + packageName + ", label=" + appName);
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "getAppDisplayName: package not installed or not visible: " + packageName, e);
            return packageName;
        }
    }

    private void launchWhitelistedApp(String packageName) {
        Log.d(TAG, "launchWhitelistedApp: package=" + packageName);
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Log.w(TAG, "launchWhitelistedApp: no launch intent for " + packageName);
            Toast.makeText(this, "App not installed: " + packageName, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetwork != null && activeNetwork.isConnected();
        Log.d(TAG, "hasNetworkConnection: connected=" + connected
                + ", activeNetwork=" + (activeNetwork == null ? "null" : activeNetwork.toString()));
        return connected;
    }

    private void handleClassCodeLookupFailure(TextInputEditText etClassCode, Exception e) {
        Log.w(TAG, "Unable to verify classroom code", e);
        showClassCodeError(etClassCode, "Unable to reach server. Check internet or DNS.");
    }

    private void showClassCodeError(TextInputEditText etClassCode, String message) {
        etClassCode.setError(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                whitelistedAppsScrollView.setVisibility(View.GONE);
                txtWhitelistedAppsTitle.setVisibility(View.GONE);
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (txtView != null) {
                    txtView.setVisibility(View.VISIBLE);
                }
                mWebView.setVisibility(View.VISIBLE);
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
