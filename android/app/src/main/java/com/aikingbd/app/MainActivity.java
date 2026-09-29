package com.aikingbd.app;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
  static final String URL = "SERVER_URL_HERE";
  WebView web;
  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    getWindow().setStatusBarColor(Color.parseColor("#050b16"));
    getWindow().setNavigationBarColor(Color.parseColor("#050b16"));
    web = new WebView(this);
    setContentView(web);
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(false);
    web.setBackgroundColor(Color.parseColor("#050b16"));
    web.setWebViewClient(new WebViewClient());
    web.addJavascriptInterface(new Bridge(), "Android");
    web.loadUrl(URL);
  }
  class Bridge {
    @JavascriptInterface public void shareAudio(final String b64, final String pkg) {
      runOnUiThread(new Runnable() { public void run() {
        try {
          byte[] d = Base64.decode(b64, Base64.DEFAULT);
          File f = new File(getCacheDir(), "voice.mp3");
          FileOutputStream o = new FileOutputStream(f); o.write(d); o.close();
          Uri u = FileProvider.getUriForFile(MainActivity.this, "com.aikingbd.app.fp", f);
          Intent i = new Intent(Intent.ACTION_SEND);
          i.setType("audio/mpeg");
          i.putExtra(Intent.EXTRA_STREAM, u);
          i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          if (pkg != null && !pkg.isEmpty()) i.setPackage(pkg);
          try { startActivity(i); }
          catch (Exception e) { i.setPackage(null); startActivity(Intent.createChooser(i, "Share")); }
        } catch (Exception e) { }
      }});
    }
  }
  @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
