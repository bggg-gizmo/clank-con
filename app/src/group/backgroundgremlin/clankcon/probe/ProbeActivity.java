package group.backgroundgremlin.clankcon.probe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.webkit.*;
import android.widget.*;
import java.util.ArrayList;

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
        TextView t=new TextView(a); t.setText(value); t.setTextSize(size); t.setTextColor(GREEN); t.setTypeface(Typeface.MONOSPACE); t.setPadding(12,8,12,8); return t;
    }
    static Button button(Activity a,String value,View.OnClickListener click) {
        Button b=new Button(a); b.setText(value); b.setAllCaps(false); b.setTextColor(GREEN); b.setOnClickListener(click); return b;
    }
    static void external(Activity a,String url,boolean duck) {
        Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(url)); i.addCategory(Intent.CATEGORY_BROWSABLE);
        if(duck)i.setPackage("com.duckduckgo.mobile.android");
        try {a.startActivity(i);} catch(android.content.ActivityNotFoundException e) {Toast.makeText(a,duck?"DuckDuckGo is not available.":"No compatible browser available.",Toast.LENGTH_LONG).show();}
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); LinearLayout outer=root(this); ScrollView scroll=new ScrollView(this); LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); scroll.addView(l); outer.addView(scroll,new LinearLayout.LayoutParams(-1,-1)); setContentView(outer);
        l.addView(text(this,"CLANK-CON / CONNECTION TEST 2",23));
        l.addView(text(this,"Login compatibility diagnostic. No conversation extraction. Use a test account first.",15));
        l.addView(text(this,"PROVIDER",14)); Spinner providers=new Spinner(this); providers.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,PROVIDERS)); l.addView(providers);
        l.addView(text(this,"ISOLATED BROWSER PROFILE",14)); Spinner profiles=new Spinner(this); profiles.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"A - persistent","B - persistent"}));l.addView(profiles);
        l.addView(button(this,"Open built-in browser",v->{Intent i=new Intent(this,profiles.getSelectedItemPosition()==0?BrowserA.class:BrowserB.class);i.putExtra("provider",providers.getSelectedItemPosition());startActivity(i);}));
        l.addView(button(this,"Open in DuckDuckGo",v->external(this,URLS[providers.getSelectedItemPosition()],true)));
        l.addView(button(this,"Open in default browser",v->external(this,URLS[providers.getSelectedItemPosition()],false)));
        l.addView(text(this,"External browsers retain their own sessions. No session transfer. Built-in profiles are independent of other apps and of connection test 1. Clear this app's storage to remove both test profiles.",15));
    }
    public static final class BrowserA extends BrowserActivity {}
    public static final class BrowserB extends BrowserActivity {}
    public static class BrowserActivity extends Activity {
        private static final int MAX_WINDOWS=4;
        private static final class Tab {
            WebView view; String origin="Not loaded", status="Waiting for page", pending=""; boolean failed, thirdParty;
        }
        private final ArrayList<Tab> tabs=new ArrayList<>();
        private FrameLayout frame; private TextView address,status,window; private CheckBox cookies; private String initial; private boolean updating;
        private Tab active(){return tabs.isEmpty()?null:tabs.get(tabs.size()-1);}
        @Override public void onCreate(Bundle state) {
            super.onCreate(state); int index=getIntent().getIntExtra("provider",-1); if(index<0||index>=URLS.length){finish();return;} initial=URLS[index];
            LinearLayout l=root(this); setContentView(l); l.addView(text(this,PROVIDERS[index]+" / BUILT-IN BROWSER",17));
            window=text(this,"",12); l.addView(window); address=text(this,"",14); address.setTextIsSelectable(true); l.addView(address); status=text(this,"",12); l.addView(status);
            LinearLayout controls=new LinearLayout(this); controls.addView(button(this,"Close",v->closeActive()),new LinearLayout.LayoutParams(0,-2,1)); controls.addView(button(this,"Back",v->back()),new LinearLayout.LayoutParams(0,-2,1)); controls.addView(button(this,"Reload",v->{Tab t=active();if(t!=null)t.view.reload();}),new LinearLayout.LayoutParams(0,-2,1)); l.addView(controls);
            LinearLayout links=new LinearLayout(this); links.addView(button(this,"DuckDuckGo",v->external(this,initial,true)),new LinearLayout.LayoutParams(0,-2,1)); links.addView(button(this,"Default browser",v->external(this,initial,false)),new LinearLayout.LayoutParams(0,-2,1)); l.addView(links);
            cookies=new CheckBox(this); cookies.setTextColor(GREEN); cookies.setText("Allow third-party cookies in this window (off by default)"); l.addView(cookies);
            cookies.setOnCheckedChangeListener((b,checked)->{Tab t=active();if(updating||t==null)return;t.thirdParty=checked;CookieManager.getInstance().setAcceptThirdPartyCookies(t.view,checked);t.status="Cookie preference changed for this window. Reload manually if needed.";refresh();});
            AutofillManager am=getSystemService(AutofillManager.class); l.addView(text(this,am!=null&&am.isEnabled()?"Android autofill service enabled; use provider form fields.":"No active Android autofill service detected.",12));
            frame=new FrameLayout(this); l.addView(frame,new LinearLayout.LayoutParams(-1,0,1)); WebView.setWebContentsDebuggingEnabled(false);
            Tab first=newTab(false); tabs.add(first); show(); first.view.loadUrl(initial);
        }
        private Tab newTab(boolean thirdParty){
            Tab t=new Tab(); t.thirdParty=thirdParty; WebView w=new WebView(this); t.view=w; w.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            WebSettings s=w.getSettings(); s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);s.setSupportMultipleWindows(true);s.setJavaScriptCanOpenWindowsAutomatically(false);s.setSafeBrowsingEnabled(true);
            CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(w,thirdParty);
            w.setWebViewClient(new WebViewClient(){
                @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){if(!"https".equalsIgnoreCase(r.getUrl().getScheme())){t.status="Blocked non-HTTPS or unsupported navigation.";refresh();return true;}return false;}
                @Override public void onPageStarted(WebView v,String u,android.graphics.Bitmap b){t.failed=false;t.pending=origin(u);t.status="Loading; sign-in is not verified.";refresh();}
                @Override public void onPageCommitVisible(WebView v,String u){if(!t.failed){t.origin=origin(u);t.pending="";refresh();}}
                @Override public void onPageFinished(WebView v,String u){if(!t.failed){t.origin=origin(u);t.pending="";t.status="Page loaded. Confirm account access visually.";refresh();}}
                @Override public void onReceivedSslError(WebView v,SslErrorHandler h,SslError e){h.cancel();t.failed=true;t.pending="";t.status="TLS certificate error. Connection blocked.";refresh();}
                @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame()){t.failed=true;t.pending="";t.status="Page load failed (WebView error "+e.getErrorCode()+").";refresh();}}
                @Override public void onReceivedHttpError(WebView v,WebResourceRequest r,WebResourceResponse response){if(r.isForMainFrame()){t.failed=true;t.origin=origin(r.getUrl().toString());t.pending="";t.status="HTTP "+response.getStatusCode()+". Provider returned an error.";refresh();}}
                @Override public void onSafeBrowsingHit(WebView v,WebResourceRequest r,int threat,SafeBrowsingResponse cb){cb.backToSafety(false);t.failed=true;t.pending="";t.status="Unsafe page blocked.";refresh();}
                @Override public boolean onRenderProcessGone(WebView v,RenderProcessGoneDetail d){if(tabs.contains(t)){int pos=tabs.indexOf(t);while(tabs.size()>pos)destroy(tabs.remove(tabs.size()-1));if(tabs.isEmpty())finish();else{active().status="Browser renderer stopped; child window closed.";show();}}return true;}
            });
            w.setWebChromeClient(new WebChromeClient(){
                @Override public boolean onCreateWindow(WebView v,boolean dialog,boolean gesture,Message result){if(!gesture||active()!=t||tabs.size()>=MAX_WINDOWS||result==null||!(result.obj instanceof WebView.WebViewTransport)){t.status="Popup blocked: requires user gesture and available window.";refresh();return false;}Tab child=newTab(false);tabs.add(child);show();((WebView.WebViewTransport)result.obj).setWebView(child.view);result.sendToTarget();return true;}
                @Override public void onCloseWindow(WebView v){if(active()==t&&tabs.size()>1)closeActive();}
                @Override public void onPermissionRequest(PermissionRequest request){request.deny();}
            });
            return t;
        }
        private void show(){if(frame==null)return;frame.removeAllViews();Tab t=active();if(t!=null){frame.addView(t.view,new FrameLayout.LayoutParams(-1,-1));refresh();}}
        private void refresh(){Tab t=active();if(t==null||address==null)return;window.setText("WINDOW "+tabs.size()+" / "+(tabs.size()==1?"MAIN":"LOGIN POPUP"));address.setText("Origin: "+t.origin+(t.pending.isEmpty()?"":"\nLoading: "+t.pending));status.setText(t.status);updating=true;cookies.setChecked(t.thirdParty);updating=false;}
        private void destroy(Tab t){if(t.view.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)t.view.getParent()).removeView(t.view);t.view.stopLoading();t.view.destroy();}
        private void closeActive(){if(tabs.size()<=1){finish();return;}destroy(tabs.remove(tabs.size()-1));show();}
        private void back(){Tab t=active();if(t!=null&&t.view.canGoBack())t.view.goBack();else closeActive();}
        private static String origin(String u){try{Uri uri=Uri.parse(u);String host=uri.getHost();return host==null?"No HTTPS origin":uri.getScheme()+"://"+host+(uri.getPort()==-1?"":":"+uri.getPort());}catch(Exception e){return "Unknown origin";}}
        @Override protected void onPause(){super.onPause();CookieManager.getInstance().flush();}
        @Override protected void onDestroy(){while(!tabs.isEmpty())destroy(tabs.remove(tabs.size()-1));super.onDestroy();}
        @Override public void onBackPressed(){back();}
    }
}
