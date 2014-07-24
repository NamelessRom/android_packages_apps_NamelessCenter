package org.namelessrom.center.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.namelessrom.center.Logger;
import org.namelessrom.center.interfaces.OnBackPressedListener;
import org.namelessrom.center.interfaces.OnFragmentLoadedListener;

/**
 * Loads our blog :)
 */
public class HomeFragment extends Fragment implements OnBackPressedListener {
    private static final String ROOT_URL = "blog.nameless-rom.org";

    private WebView webView;

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        webView = new WebView(getActivity());
        return webView;
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
        });

        // our blog uses javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
    }

    @Override public boolean onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }
}
