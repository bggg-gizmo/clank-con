package group.backgroundgremlin.clankcon.probe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class ProbeActivity extends Activity {
    private static final int BG=0xff06100a, GREEN=0xff89ffac;
    private static final String[] PROVIDERS={"ChatGPT","Kimi","Claude","Gemini","Grok","DeepSeek","Perplexity","Microsoft Copilot"};
    private static final String[] URLS={"https://chatgpt.com/","https://www.kimi.com/","https://claude.ai/","https://gemini.google.com/","https://grok.com/","https://chat.deepseek.com/","https://www.perplexity.ai/","https://copilot.microsoft.com/"};
    static LinearLayout root(Activity a) {
        LinearLayout l=new LinearLayout(a); l.setOrientation(LinearLayout.VERTICAL); l.setBackgroundColor(BG);
        l.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i;});
        return l;
    }
    static TextView text(Activity a,String value,int size) {
        TextView t=new TextView(a); t.setText(value); t.setTextSize(size); t.setTextColor(GREEN); t.setTypeface(Typeface.MONOSPACE); t.setPadding(16,12,16,12); return t;
    }
    static Button button(Activity a,String value,View.OnClickListener click) {
        Button b=new Button(a); b.setText(value); b.setAllCaps(false); b.setTextColor(GREEN); b.setOnClickListener(click); return b;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); LinearLayout outer=root(this); ScrollView scroll=new ScrollView(this); LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); scroll.addView(l); outer.addView(scroll,new LinearLayout.LayoutParams(-1,-1)); setContentView(outer);
        l.addView(text(this,"CLANK-CON / CONNECTION TEST",23));
        l.addView(text(this,"Diagnostic build only. Tests provider website access and two independently stored browser profiles. No chat extraction, account-history access or vault implementation is included. Use a test account first.",15));
        l.addView(text(this,"PROVIDER",14)); Spinner providers=new Spinner(this); providers.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,PROVIDERS)); l.addView(providers);
        l.addView(text(this,"ISOLATED BROWSER PROFILE",14)); Spinner profiles=new Spinner(this); profiles.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"A — persistent","B — persistent"}));l.addView(profiles);
        l.addView(button(this,"Open official provider website",v->{Intent i=new Intent(this,profiles.getSelectedItemPosition()==0?BrowserA.class:BrowserB.class);i.putExtra("provider",providers.getSelectedItemPosition());startActivity(i);}));
        l.addView(text(this,"Profiles use independent WebView data directories. Sign in through the genuine website only. An unsupported sign-in must remain unsupported; no bypass is attempted. To remove both test sessions, clear this diagnostic app's storage in Android Settings.",15));
    }
    public static final class BrowserA extends BrowserActivity {}
    public static final class BrowserB extends BrowserActivity {}
    public static class BrowserActivity extends Activity {
        private WebView web; private TextView address,status; private String initial; private boolean mainFrameFailed;
        @Override public void onCreate(Bundle state) {
            super.onCreate(state); int index=getIntent().getIntExtra("provider",-1); if(index<0||index>=URLS.length){finish();return;} initial=URLS[index];
            LinearLayout l=root(this); setContentView(l); l.addView(text(this,PROVIDERS[index]+" / CONNECTION TEST",17));
            address=text(this,"",12); address.setTextIsSelectable(true); l.addView(address); status=text(this,"Website access does not prove login or conversation retrieval.",12); l.addView(status);
            LinearLayout controls=new LinearLayout(this); controls.addView(button(this,"Close",v->finish()),new LinearLayout.LayoutParams(0,-2,1)); controls.addView(button(this,"Back",v->{if(web.canGoBack())web.goBack();}),new LinearLayout.LayoutParams(0,-2,1)); controls.addView(button(this,"Reload",v->web.reload()),new LinearLayout.LayoutParams(0,-2,1)); l.addView(controls);
            l.addView(button(this,"Open provider in external browser",v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(initial)));}catch(Exception e){status.setText("No compatible external browser available.");}}));
            web=new WebView(this); l.addView(web,new LinearLayout.LayoutParams(-1,0,1)); WebView.setWebContentsDebuggingEnabled(false);
            WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);s.setSupportMultipleWindows(false);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setSafeBrowsingEnabled(true);
            CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
            web.setWebViewClient(new WebViewClient(){
                @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){if(!"https".equalsIgnoreCase(r.getUrl().getScheme())){status.setText("Blocked non-HTTPS or unsupported navigation.");return true;}return false;}
                @Override public void onPageStarted(WebView v,String u,android.graphics.Bitmap b){mainFrameFailed=false;address.setText(origin(u));status.setText("Loading provider page. Authentication is not automatically verified.");}
                @Override public void onPageFinished(WebView v,String u){address.setText(origin(u));if(!mainFrameFailed)status.setText("Page loaded. Confirm account access visually; retrieval is not tested.");}
                @Override public void onReceivedSslError(WebView v,SslErrorHandler h,SslError e){h.cancel();mainFrameFailed=true;status.setText("TLS certificate error. Connection blocked.");}
                @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame()){mainFrameFailed=true;status.setText("Provider page failed to load (WebView error "+e.getErrorCode()+").");}}
            });
            web.loadUrl(initial);
        }
        private static String origin(String u){try{Uri uri=Uri.parse(u);String host=uri.getHost();return host==null?"Unknown origin":uri.getScheme()+"://"+host+(uri.getPort()==-1?"":":"+uri.getPort());}catch(Exception e){return "Unknown origin";}}
        @Override protected void onPause(){super.onPause();CookieManager.getInstance().flush();}
        @Override protected void onDestroy(){if(web!=null){web.stopLoading();((LinearLayout)web.getParent()).removeView(web);web.destroy();web=null;}super.onDestroy();}
        @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
    }
}
