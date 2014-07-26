package org.namelessrom.center.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.namelessrom.center.Logger;
import org.namelessrom.center.R;
import org.namelessrom.center.interfaces.OnBackPressedListener;
import org.namelessrom.center.interfaces.OnFragmentLoadedListener;

import static butterknife.ButterKnife.findById;

/**
 * Loads our blog :)
 */
public class HomeFragment extends Fragment implements OnBackPressedListener {
    private static final String ROOT_URL = "blog.nameless-rom.org";

    private WebView     webView;
    private ProgressBar progressBar;

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_main, container, false);

        webView = findById(v, R.id.webview);
        progressBar = findById(v, R.id.webview_progress);
        progressBar.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();
        if (activity != null && activity instanceof OnFragmentLoadedListener) {
            ((OnFragmentLoadedListener) activity).onFragmentLoaded();
        }

        // setup / configure the webview
        setupWebView();

        // load our blog
        webView.loadUrl("https://blog.nameless-rom.org");
    }

    @SuppressLint("SetJavaScriptEnabled") private void setupWebView() {
        // set a webview client
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                // if the url is not empty and our blog...
                if (!TextUtils.isEmpty(url) && url.toLowerCase().contains(ROOT_URL)) {
                    // load it in our webview
                    view.loadUrl(url);
                    // returning false means the webview handles it
                    return false;
                } else {
                    // if the url is not our blog, pass it to applications like Browser, Chrome etc
                    try {
                        final Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        // this may fail in some weird cases, so try - catch it
                        startActivity(i);
                    } catch (Exception exc) {
                        Logger.e(this, String.format("Something went wrong: %s", exc.getMessage()));
                    }
                }
                return true;
            }

            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // when starting loading, show the progressbar
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            }

            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // and hide it again,when we are done
                if (progressBar != null) progressBar.setVisibility(View.INVISIBLE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(final WebView view, final int newProgress) {
                super.onProgressChanged(view, newProgress);
                // when the progress changes, let the progressbar know as well
                if (progressBar != null) progressBar.setProgress(newProgress);
            }
        });

        // our blog uses javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    @Override public boolean onBackPressed() {
        // if we press back and can actually go back, go back, else let it get handled by the parent
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }
}
