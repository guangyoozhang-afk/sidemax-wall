package cc.sidemax.wall;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private static final String HOME_URL =
            "https://sidemax.cc/?page=wall";

    private WebView webView;
    private final Handler handler = new Handler();
    private boolean pageLoaded = false;

    private final Runnable retryRunnable = new Runnable() {
        @Override
        public void run() {
            if (webView != null && !pageLoaded) {
                webView.loadUrl(HOME_URL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        hideSystemUi();

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        setContentView(webView);

        configureWebView();
        loadHome();
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        String ua = settings.getUserAgentString();
        settings.setUserAgentString(ua + " SIDE-MAX-TV");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                pageLoaded = false;
                handler.removeCallbacks(retryRunnable);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pageLoaded = true;
                handler.removeCallbacks(retryRunnable);
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    int errorCode,
                    String description,
                    String failingUrl) {

                pageLoaded = false;
                handler.removeCallbacks(retryRunnable);
                handler.postDelayed(retryRunnable, 5000);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        // Android 5.1 对硬件加速 WebView 支持较好；如个别电视 GPU 异常可改为 SOFTWARE。
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void loadHome() {
        pageLoaded = false;
        webView.loadUrl(HOME_URL);
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();

        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // 广告电视模式：禁止遥控器误按返回退出。
        // 如需允许返回上一页，可改为 webView.goBack()。
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);

        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
