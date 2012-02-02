package org.kluesing.android;

import java.net.URLDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class InstallReferrerReceiver extends BroadcastReceiver{
    
    private static final String TAG = "InstallReferrerReceiver";
    
    public static final String INSTALL_REF_PREFS = "INSTALL_REF_PREFS";
    public static final String SKEY_SOURCE = "SKEY_SOURCE";
    public static final String SKEY_CAMPAIGN = "SKEY_CAMPAIGN";
    public static final String SKEY_KEYWORD = "SKEY_KEYWORD";
    
    // set the referal keys into shared preferences for the main activity to
    // read on launch.
    public void setSharedPrefKey(Context context, String key, String value){
        SharedPreferences settings = context.getSharedPreferences(INSTALL_REF_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
        Log.d(TAG, "Stored " + key + ":" + value);
    }
    
    // Extract the referral information and store in shared preferences
    // The first format is the google anayltics format of utm_*
    // The utm_* format applies to:
    // - the android market organic search
    // - admob,  
    // - several third party markets 
    // - ad networks that allow you to provide a custom url (jumptap, inmobi)
    // Example strings:
    //      utm_source=androidmarket&utm_medium=device&utm_campaign=search&utm_term=gun club&rowindex=23&hl=en&correctedQuery=
    //      utm_source=androidmarket&utm_medium=device&utm_campaign=notifications&utm_content=downloadFailed
    //      utm_source=admob&utm_medium=banner&utm_campaign=admob1&gmob_t=ABk73oI_lS1PoBKGBFh-mRyr5V5BTnD3uitZTIFcTTomlXKA-q7lQQ
    // This is the format used by tapjoy, it's just com.company.appname
    
    private void parseAndStoreReferral(Context context, String refString){
        if (refString != null && refString.length() > 0){
            int index = refString.indexOf("referrer=");
            
            if (index > -1){ // we have a referrer              
                if(refString.indexOf("utm_source=")!=-1){ // matches the GA format
                    Uri uri = Uri.parse("?"+refString.substring(index+9));
                    String source = uri.getQueryParameter("utm_source");
                    String campaign = uri.getQueryParameter("utm_campaign");
                    String keyword = uri.getQueryParameter("utm_term");
                    setSharedPrefKey(context, SKEY_SOURCE, source);
                    if(campaign!=null){ setSharedPrefKey(context, SKEY_CAMPAIGN, campaign);}
                    if(keyword!=null){setSharedPrefKey(context, SKEY_KEYWORD, keyword); }
                } else if(refString.indexOf(".")!=-1){ // crude check to see if matches com.company.package
                    setSharedPrefKey(context, SKEY_SOURCE, "tapjoy");
                    setSharedPrefKey(context, SKEY_KEYWORD, refString);
                } else {
                    // unknown referral, store the whole string as the source
                    setSharedPrefKey(context, SKEY_SOURCE, refString);
                }
            }
        }
    }
 
    @Override
    public void onReceive(Context context, Intent intent){
        Bundle extras = intent.getExtras();
        String referrerString = extras.getString("referrer");
        String decodedReferrer = URLDecoder.decode(referrerString);
        parseAndStoreReferral(context,decodedReferrer);
    }
}
