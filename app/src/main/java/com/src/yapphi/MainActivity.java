package com.src.yapphi;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
    private WebView yapphiWebView;
    private String appURL = "";
    AlertDialog alertDialog;
    private BroadcastReceiver networkStateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            if(ni==null) {
                yapphiWebView.loadUrl("");
                alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setTitle("You need to be online to access Yapphi!!");
                alertDialog.setMessage("Please check your internet connection before continuing.");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                });
                alertDialog.show();
            }
            else {
                yapphiWebView.loadUrl(appURL);
                if (alertDialog!=null) {
                    alertDialog.dismiss();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onResume() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onResume();
        yapphiWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = yapphiWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        yapphiWebView.setWebViewClient(new YapphiViewClient());
        yapphiWebView.setBackgroundColor(0xFF000000);
        @SuppressLint("HardwareIds") String androidDeviceId = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        byte[] androidDeviceIdHash;
        String androidDeviceIdHexString = null;
        try {
            if(androidDeviceId!=null) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                //convert the string into bytes (e.g. using text.getBytes(StandardCharsets.UTF_8)) and then hash the bytes.
                androidDeviceIdHash = md.digest(androidDeviceId.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte deviceIdHash : androidDeviceIdHash) {
                    String hex = Integer.toHexString(0xff & deviceIdHash);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                androidDeviceIdHexString = hexString.toString();
            }
        }
        catch(Exception e) {
            Log.e("Yapphi Alpha:","Error in encryption:"+e);
        }
        String hostURL = "https://app.yapphi.com";
        if(androidDeviceIdHexString !=null) {
            appURL = hostURL + "/?para6="+androidDeviceIdHexString;
            Log.i("Yapphi URL","with Encryption:"+ appURL);
        }
        else {
            appURL = hostURL + "/?para6="+androidDeviceId;
            Log.i("Yapphi URL","without Encryption:"+ appURL);
        }
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (alertDialog!=null) {
            alertDialog.dismiss();
        }
    }

    private class YapphiViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Uri uri = Uri.parse(url);
            return handleUri(uri);
        }
        @TargetApi(Build.VERSION_CODES.N)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            return handleUri(uri);
        }
        public boolean handleUri(final Uri uri) {
            final String host = uri.getHost();
            if (host!=null && host.endsWith("app.yapphi.com")) { //change the host url to match your website
                return false;
            } else {
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        }
    }
}

