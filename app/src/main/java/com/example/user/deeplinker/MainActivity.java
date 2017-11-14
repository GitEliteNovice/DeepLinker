package com.example.user.deeplinker;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    EditText urledittext;
    Button submit;
    String pacakageName=null;
    String activityname="";
    String urlmatcher="";
    private static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)"
            + // switch on case insensitive matching
            '('
            + // begin group for schema
            "(?:http|https|file)://" + "|(?:inline|data|about|javascript):" + "|(?:.*:.*@)"
            + ')' + "(.*)");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urledittext=(EditText)findViewById(R.id.url);
        submit=(Button)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                urlmatcher=urledittext.getText().toString();
                if (urledittext.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please enter url first...",Toast.LENGTH_LONG).show();
                }else {
                    if (startActivityForUrl(urledittext.getText().toString())) {
                        // If it was a mailto: link, or an intent, or could be launched elsewhere, do that

                    }else {
                        Toast.makeText(getApplicationContext(),"Please install application first",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    public boolean startActivityForUrl(@NonNull String url) {
        Intent intent;
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException ex) {
            Log.w("Browser", "Bad URI " + url + ": " + ex.getMessage());
            return false;
        }
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setComponent(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            intent.setSelector(null);
        }



        Matcher m = ACCEPTED_URI_SCHEMA.matcher(url);
        if (m.matches() && !isSpecializedHandlerAvailable(intent)&&!url.contains("facebook")) {
            return false;
        }
        try {

            if (url.contains("facebook.com")&&m.matches()){

                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                i.setClassName("com.facebook.katana","com.facebook.katana.IntentUriHandler");
                startActivity(i);

            }
            else	if (pacakageName!=null){
                Intent i=new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                i.setClassName(pacakageName,activityname);
              startActivity(i);
                pacakageName="";
                activityname="";
                return true;

            }
        } catch (Exception exception) {
            exception.printStackTrace();
            // TODO: 6/5/17 fix case where this could throw a FileUriExposedException due to file:// urls
        }
        if (url.contains("facebook.com")&&m.matches()){
            return true;
        }else {
            return false;
        }

    }
    private boolean isSpecializedHandlerAvailable(@NonNull Intent intent) {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> handlers = pm.queryIntentActivities(intent,
                PackageManager.GET_RESOLVED_FILTER);
        if (handlers == null || handlers.isEmpty()) {
            return false;
        }
        for (ResolveInfo resolveInfo : handlers) {
            IntentFilter filter = resolveInfo.filter;
            if (filter == null) {
                // No intent filter matches this intent?
                // Error on the side of staying in the browser, ignore
                continue;
            }
            if (filter.matchDataAuthority(Uri.parse(urlmatcher))>0){
                activityname=resolveInfo.activityInfo.name;
                pacakageName=resolveInfo.activityInfo.applicationInfo.packageName;
                Log.d("givemeSchemes:","thi"+resolveInfo.activityInfo.name+"paak " +resolveInfo.activityInfo.applicationInfo.packageName);
            }
            // NOTICE: Use of && instead of || will cause the browser
            // to launch a new intent for every URL, using OR only
            // launches a new one if there is a non-browser app that
            // can handle it.
            // Previously we checked the number of data paths, but it is unnecessary
            // filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0
            if (filter.countDataAuthorities() == 0) {
                // Generic handler, skip
                continue;
            }
            return true;
        }
        return false;
    }
}
