package io.lhyz.android.webkittemplate;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 主fragment
 */
public class BrowserFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener, Toolbar.OnMenuItemClickListener {

    private static final String HOME = "https://www.baidu.com";
    private static final String EXTRA_URL = "EXTRA_URL";
//    private static final String TAG = BrowserFragment.class.getSimpleName();

    Toolbar mToolbar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    WebView mWebView;
    TextView mAddressView;

    String mURL;

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mURL = url;
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Toast.makeText(getActivity(), "Error to load the page", Toast.LENGTH_SHORT).show();
        }

    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            setAddressView(title);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }
    };

    public BrowserFragment() {

    }

    public static Fragment newInstance(String url) {
        Fragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.menu_main);
        mToolbar.setOnMenuItemClickListener(this);

        mAddressView = (TextView) view.findViewById(R.id.address);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary, R.color.colorPrimaryDark,
                R.color.colorAccent, R.color.colorBackground);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mWebView = (WebView) view.findViewById(R.id.browser);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);

    }

    @Override
    public void onResume() {
        super.onResume();
        mURL = getArguments().getString(EXTRA_URL, HOME);
        mWebView.loadUrl(mURL);
    }

    private void setAddressView(String title) {
        if (mURL.startsWith("https")) {
            mAddressView.setBackgroundResource(R.drawable.background_security);
        }
        mAddressView.setText(title);
    }

    @Override
    public void onRefresh() {
        mWebView.loadUrl(mURL);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy_link:
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("url", mURL);
                clipboardManager.setPrimaryClip(data);
                Toast.makeText(getActivity(), "链接已复制", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_open_browser:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mURL));
                startActivity(intent);
                return true;
            case R.id.action_share_content:
                ShareActionProvider share = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mURL);
                share.setShareIntent(intent);
                return true;
            case R.id.action_back_page:
                mWebView.goBack();
                return true;
        }
        return false;
    }
}
