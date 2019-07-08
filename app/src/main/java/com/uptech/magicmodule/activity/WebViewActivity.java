package com.uptech.magicmodule.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.ImageView;

import com.gyf.barlibrary.ImmersionBar;
import com.squareup.picasso.Picasso;
import com.uptech.magicmodule.R;
import com.uptech.magicmodule.utils.PicassoUtils;


public class WebViewActivity extends BaseActivity {


    private String webUrl;
    private Toolbar toolbar;

    private WebView mWebView;

    private ImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        initData();
        initWebView();
    }

    private void initData() {


        Intent intent = this.getIntent();
        webUrl = intent.getStringExtra("_webUrl");
        String webTitle = intent.getStringExtra("_webTitle");
        String picTitle = intent.getStringExtra("_picTitle");

        //设置标题
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImmersionBar.setTitleBar(this, toolbar);
        toolbar.setTitle(webTitle);
        toolbar.inflateMenu(R.menu.menu_news_detail);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_share_sina:
                        break;
                    case R.id.menu_shareweichatCircle:
                        break;
                    case R.id.menu_share_weichatfriend:
                        break;
                    case R.id.menu_shareQQ:
                        break;

                }
                return true;
            }
        });
        mWebView= (WebView) findViewById(R.id.mWebView);
        ivImage= (ImageView) findViewById(R.id.ivImage);
        //如果图片为null，则显示默认的图片
        if (picTitle.isEmpty()){
            PicassoUtils.loadImageViewFromLocal(this,R.mipmap.ic_webview_bg,ivImage);
        }else {
            Picasso.with(this).load(picTitle).error(R.mipmap.ic_webview_bg).into(ivImage);
        }

    }


    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        mWebView = (WebView) findViewById(R.id.mWebView);
        //支持JS
        mWebView.getSettings().setJavaScriptEnabled(true);
        //支持缩放
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        //接口回调
        mWebView.setWebChromeClient(new WebViewClient());
        mWebView.loadUrl(webUrl);
        mWebView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(webUrl);
                //我接受这个事件
                return true;
            }
        });
    }
    public class WebViewClient extends WebChromeClient {

        //进度变化的监听
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {

            }
            super.onProgressChanged(view, newProgress);
        }
    }


}
