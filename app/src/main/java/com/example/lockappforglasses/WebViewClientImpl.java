package com.example.lockappforglasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class WebViewClientImpl extends WebViewClient {

    private Activity activity = null;
    private ProgressBar progressBar;
    private TextView txtView;
    private Context mContext;

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
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mContext.startActivity(browserIntent);
        }
        return false;
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
    }
}
