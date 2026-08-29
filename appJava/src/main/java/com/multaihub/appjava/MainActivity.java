package com.multaihub.appjava;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends AppCompatActivity {
    private final List<Provider> providers = Arrays.asList(
        p("chatgpt", "ChatGPT", "https://chatgpt.com", "Chat"), p("claude", "Claude", "https://claude.ai", "Chat"),
        p("gemini", "Gemini", "https://gemini.google.com", "Chat"), p("grok", "Grok", "https://grok.x.ai", "Chat"),
        p("deepseek", "DeepSeek", "https://chat.deepseek.com", "Chat"), p("qwen", "Qwen", "https://chat.qwen.ai", "Chat"),
        p("mistral", "Mistral", "https://chat.mistral.ai", "Chat"), p("pi", "Pi", "https://pi.ai", "Chat"),
        p("poe", "Poe", "https://poe.com", "Chat"), p("characterai", "Character.AI", "https://character.ai", "Chat"),
        p("perplexity", "Perplexity", "https://www.perplexity.ai", "Search"), p("you", "You.com", "https://you.com", "Search"),
        p("phind", "Phind", "https://www.phind.com", "Coding"), p("copilot", "Microsoft Copilot", "https://copilot.microsoft.com", "Search"),
        p("blackbox", "Blackbox AI", "https://www.blackbox.ai", "Coding"), p("cursor", "Cursor", "https://cursor.com", "Coding"),
        p("replit", "Replit AI", "https://replit.com", "Coding"), p("huggingchat", "HuggingChat", "https://huggingface.co/chat", "Free"),
        p("openrouter", "OpenRouter", "https://openrouter.ai/chat", "Free"), p("lmarena", "LMSYS Arena", "https://chat.lmsys.org", "Free"),
        p("duckduckgo", "DuckDuckGo AI", "https://duckduckgo.com/aichat", "Free"), p("kimi", "Kimi", "https://kimi.moonshot.cn", "Chat"),
        p("metaai", "Meta AI", "https://www.meta.ai", "Chat"), p("groq", "Groq", "https://console.groq.com/playground", "Free"),
        p("together", "Together AI", "https://api.together.xyz/playground", "Free"), p("cohere", "Cohere", "https://coral.cohere.com", "Chat"),
        p("notebooklm", "NotebookLM", "https://notebooklm.google.com", "Writing"), p("gamma", "Gamma", "https://gamma.app", "Writing"),
        p("leonardo", "Leonardo AI", "https://leonardo.ai", "Image"), p("ideogram", "Ideogram", "https://ideogram.ai", "Image"),
        p("flux", "Flux AI", "https://flux.ai", "Image"), p("chatpdf", "ChatPDF", "https://www.chatpdf.com", "Writing")
    );
    private SharedPreferences prefs;
    private LinearLayout root;
    private WebView webView;
    private EditText search;
    private String category = "All";

    private static Provider p(String id, String name, String url, String category) { return new Provider(id, name, url, category); }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("multai", Context.MODE_PRIVATE);
        showHome();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { if (webView != null && webView.canGoBack()) webView.goBack(); else showHome(); }
        });
    }

    private void showHome() {
        webView = null;
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24); root.setBackgroundColor(Color.rgb(15,17,22));
        TextView title = text("MultiAI", 28); root.addView(title);
        TextView subtitle = text("One Java app for your AI tools", 14); subtitle.setTextColor(Color.LTGRAY); root.addView(subtitle);
        search = new EditText(this); search.setHint("Search AI providers..."); search.setSingleLine(true); root.addView(search);
        Spinner spinner = new Spinner(this); String[] cats={"All","Favorites","Chat","Coding","Writing","Image","Search","Free"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats)); root.addView(spinner);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){ public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){category=cats[pos];renderProviders();}});
        search.setOnEditorActionListener((v,a,e)->{renderProviders();return false;});
        LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); root.addView(list,new LinearLayout.LayoutParams(-1,0,1)); root.setTag(list);
        setContentView(root); renderProviders();
    }

    private void renderProviders() {
        if(root==null)return; LinearLayout list=(LinearLayout)root.getTag(); list.removeAllViews();
        String q=search==null?"":search.getText().toString().trim().toLowerCase(Locale.ROOT);
        for(Provider provider:providers){
            boolean cm="All".equals(category)||("Favorites".equals(category)&&isFavorite(provider.id))||provider.category.equalsIgnoreCase(category);
            boolean tm=q.isEmpty()||provider.name.toLowerCase(Locale.ROOT).contains(q)||provider.category.toLowerCase(Locale.ROOT).contains(q);
            if(!cm||!tm)continue; Button b=new Button(this); b.setText(provider.name+"  •  "+provider.category+(isFavorite(provider.id)?"  ★":"")); b.setAllCaps(false); b.setOnClickListener(v->openProvider(provider)); list.addView(b);
        }
    }

    private boolean isFavorite(String id){return prefs.getBoolean("favorite_"+id,false);}

    @SuppressLint("SetJavaScriptEnabled") private void openProvider(Provider provider){
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setBackgroundColor(Color.WHITE);
        LinearLayout bar=new LinearLayout(this); Button back=new Button(this); back.setText("‹ Back"); back.setOnClickListener(v->showHome());
        Button fav=new Button(this); fav.setText(isFavorite(provider.id)?"★":"☆"); fav.setOnClickListener(v->{boolean value=!isFavorite(provider.id);prefs.edit().putBoolean("favorite_"+provider.id,value).apply();fav.setText(value?"★":"☆");});
        TextView name=text(provider.name,18);name.setTextColor(Color.DKGRAY);bar.addView(back);bar.addView(name,new LinearLayout.LayoutParams(0,-2,1));bar.addView(fav);page.addView(bar);
        webView=new WebView(this); webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
                Uri uri=request.getUrl();
                return uri==null||!"https".equalsIgnoreCase(uri.getScheme());
            }
            @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error){
                handler.cancel();
            }
        });
        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            s.setSafeBrowsingEnabled(true);
        }
        s.setSaveFormData(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(provider.url);
        page.addView(webView,new LinearLayout.LayoutParams(-1,0,1));setContentView(page);
    }

    private TextView text(String value,int size){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(Color.WHITE);v.setPadding(0,8,0,8);return v;}
    private static final class Provider{final String id,name,url,category;Provider(String id,String name,String url,String category){this.id=id;this.name=name;this.url=url;this.category=category;}}
}
