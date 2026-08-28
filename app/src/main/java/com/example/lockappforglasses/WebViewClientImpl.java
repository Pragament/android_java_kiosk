package com.example.lockappforglasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class WebViewClientImpl extends WebViewClient {

    private static final String TAG = "WebViewClientImpl";
    private Activity activity = null;
    private ProgressBar progressBar;
    private TextView txtView;
    private Context mContext;
    private String lastLoggedUrl = "";

    public WebViewClientImpl(ProgressBar progressBar, TextView txtView, Context ctx) {
        this.progressBar = progressBar;
        this.txtView = txtView;
        this.mContext = ctx;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String      description, String failingUrl) {
        //Toast.makeText(activity, "Oh no! " + description,      Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO Auto-generated method stub
        Boolean isChromeModeEnabled = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean(mContext.getString(R.string.pref_opt_chrome_mode), true);
        if(!isChromeModeEnabled) {
            view.loadUrl(url);
        }else{
            logWebsiteUsage(url, null);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mContext.startActivity(browserIntent);
        }
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        progressBar.setVisibility(View.VISIBLE);
        txtView.setText("Loading....");
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // TODO Auto-generated method stub
        super.onPageFinished(view, url);
        progressBar.setVisibility(View.GONE);
        txtView.setText(view.getTitle());
        logWebsiteUsage(url, view.getTitle());
    }

    private void logWebsiteUsage(String url, String title) {
        if (TextUtils.isEmpty(url) || "about:blank".equals(url) || url.equals(lastLoggedUrl)) {
            return;
        }

        String classCode = ((MyApp) mContext.getApplicationContext()).getCurrentClassCode();
        if (TextUtils.isEmpty(classCode)) {
            Log.w(TAG, "logWebsiteUsage: skipping because current class code is empty. url=" + url);
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String name = preferences.getString("userName", "Unknown");
        String host = Uri.parse(url).getHost();
        String displayTitle = TextUtils.isEmpty(title) ? url : title;

        Map<String, Object> webUsageLog = new HashMap<>();
        webUsageLog.put("studentName", name);
        webUsageLog.put("studentLog", "Opened " + displayTitle);
        webUsageLog.put("timestamp", System.currentTimeMillis());
        webUsageLog.put("url", url);
        webUsageLog.put("title", displayTitle);
        webUsageLog.put("host", host);

        lastLoggedUrl = url;
        CollectionReference webUsageLogsRef = FirebaseFirestore.getInstance()
                .collection("classrooms")
                .document(classCode)
                .collection("webUsageLogs");
        webUsageLogsRef.add(webUsageLog)
                .addOnSuccessListener(documentReference -> Log.i(TAG, "logWebsiteUsage: saved url=" + url + " to /classrooms/" + classCode + "/webUsageLogs/" + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "logWebsiteUsage: failed url=" + url + " classCode=" + classCode, e));
    }
}
