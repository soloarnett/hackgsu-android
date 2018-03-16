package com.hackgsu.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.http.*;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.onesignal.OneSignal;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    WebView myWebView;
    public SwipeRefreshLayout swipeRefreshLayout;
    Router router = new Router();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        getSupportActionBar().hide();

        swipeRefreshLayout = findViewById(com.hackgsu.app.R.id.refresh);
        startProgressBar();
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        myWebView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        router = new Router();
        start();



//        myWebView.evaluateJavascript("localStorage.setItem('pushUID', 'pushUID');", new ValueCallback<String>() {
//            @Override
//            public void onReceiveValue(String value) {
//                Log.d("javascript", value);
//            }
//        });

//        Window window = getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//
//        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorWhite));
//        getWindow().setStatusBarColor(Color.WHITE);

    }

    private void start(){
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                try{
                    Log.d("push", "reached");
                    Log.d("pushuid", "registration: " +  userId);
                    //updateUserPushUID(mContext,userId, jobject.getString("id"));
                    OneSignal.setSubscription(true);
                    router.setPushUID(getApplicationContext(), userId);
                    String pushUID = userId;
                    Log.d("pushthis", pushUID);
                    String mimeType = "text/html";
                    String encoding = "utf-8";
                    String injection = "<script type='text/javascript'>localStorage.setItem('pushUID', '"+pushUID.toString()+"');window.location.replace('http://hackgsu.com/');</script>";
                    Log.d("injection", injection);
                    myWebView.loadDataWithBaseURL("http://hackgsu.com/", injection, mimeType, encoding, null);
                    String id = "1";
                    String name = "Android User";
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, pushUID);
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "user");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                    setRefresh();

                }catch (Exception e){
                    Log.d("one signal error", e.toString());
                }
            }
        });
    }

    private void setRefresh(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
//                            offerList.clear();
//                            feedRecyclerView.getRecycledViewPool().clear();
                            myWebView.reload();
//                            getOffers(getApplicationContext());
                        }catch(Exception e){
                            Log.d("refresh", "failed " + e.toString());
                        }
                    }
                });

            }
        });
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d("link", request.getUrl().toString());
            if(request.getUrl().toString().contains("://hackgsu.com")) {
                view.loadUrl(request.getUrl().toString());
                return false;
            } else {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                startActivity(i);
            }
            return true;
        }
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            if (Uri.parse(url).getHost().equals("www.example.com")) {
//                // This is my web site, so do not override; let my WebView load the page
//                return false;
//            }
//            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            startActivity(intent);
//            return true;
//        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
            handler.proceed();
        }
        public void onPageFinished(WebView view, String url) {
            // do your stuff here
            stopProgressBar();
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    public void startProgressBar() {
        swipeRefreshLayout.setRefreshing(true);
    }

    public void stopProgressBar() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
