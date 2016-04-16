#一个内嵌网页浏览器的fragment模板（支持分享链接，复制链接，在浏览器中打开，上一页）

先看看效果
![](/art/device-2016-04-15-202657.png)

功能实现过程：

## 1.分享链接

首先，在menu中定义
```xml
    <item
        android:id="@+id/action_share_content"
        android:title="@string/action_share_content"
        app:actionProviderClass="android.support.v7.widget.ShareActionProvider"
        app:showAsAction="never"/>
```

在`OnMenuItemClickListener`中，

```java
            case R.id.action_share_content:
                ShareActionProvider share = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, mURL);
                share.setShareIntent(intent);
                return true;
```
就可以直接分享当前链接（具体看代码）

## 2.复制链接

使用系统剪切板
```java
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("url", mURL);
                clipboardManager.setPrimaryClip(data);
                Toast.makeText(getActivity(), "链接已复制", Toast.LENGTH_SHORT).show();
```

## 3.在浏览器中打开

普通打开连接即可
```java
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mURL));
                startActivity(intent);
```

## 4.上一页

调用`WebView`的`goBack`即可（而且如果不可以back的话只是没有效果，但是并不会崩溃）



主要使用的webview调用周期分析
==========================

### 1.调用loadUrl

我们调用
```java
mWebView.loadUrl(mURL);
```

### 2.事先设置好`WebViewClient`和`WebChromeClient`

由于webview默认可能会打开系统浏览器,我们给他设置这两个对象就将处理约束在本地

```java
    private WebViewClient mWebViewClient = new WebViewClient() {
        
        //每次在页面内点击链接的时候就会调用此方法重新load新的url（刚点击的url）
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mURL = url;
            view.loadUrl(url);
            return true;
        }
        
        //当页面完成的时候会调用此方法
        //
        @Override
        public void onPageFinished(WebView view, String url) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        
        //当页面出现错误的时候被调用（一般也用不上）
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Toast.makeText(getActivity(), "Error to load the page", Toast.LENGTH_SHORT).show();
        }

    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        
        //可以从这个方法中取得当前页面的title
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            setAddressView(title);
        }
        
        //当页面内JS脚本需要弹出alert的时候被调用，可以在此方法中根据alert信息来生成一个alertDialog
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }
    };
```

上述方法的可能顺序
`mWebView.loadUrl(mURL);` ->
`onReceivedTitle` -> 
`onPageFinished` ->

在页面内点击链接的时候会调用下面的方法
`shouldOverrideUrlLoading` ->
`` ->


重新添加了一个方法：
```java
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.e("TAG", "" + newProgress);
            mProgressBar.setProgress(newProgress);
        }
```
这个方法在页面加载的过程中被调用，无论页面是loadUrl打开的还是goBack打开的。

同样的还有`onPageFinished`方法，页面加载完成的时候被调用，无论页面是loadUrl打开的还是goBack打开的。

`onPageFinished`调用在`onReceivedTitle`之后。

尚没有好的方法解决使用goBack之后标题错乱的问题（可选的方法是在`onPageFinished`方法之后重新解析URL，就是用第三方库重新获取url网页内容，然后用正则解析出title值，不过这样对于网络和其他方面负担比较重，所以这个bug暂时不决定解决了）

另，对于在`onProgressChanged`设置某些progress控件来显示进度的方法，可能会出现不显示的问题（newProgress倒是0-100的），所以设置了之后看不到东西，就算了吧。


总结的不足，之后如果出现其他问题我会再补充的。