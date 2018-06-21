package com.aerserv.admob;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.aerserv.sdk.AerServConfig;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Utility file for AerServ Adapter.  To use this file, place the file in .../com/com.com.aerserv/admob
 * of your source folder.  Alternatively, you can place it in any folder of your
 * choice, along with the other AerServ adapter files, and change the package name to match your
 * destination folder.
 */
public class AerServCustomEventUtils {
    private static final String LOG_TAG = AerServCustomEventUtils.class.getSimpleName();

    public static AerServConfig getAerServConfig(Context context, String serverParams,
            Bundle localParams) {
        String params = TextUtils.isEmpty(serverParams.trim()) ? "{}" : serverParams.trim();

        // If server parameter is in old format (PLC string), convert it to new format (JSON string)
        if (TextUtils.isDigitsOnly(params)) {
            params = "{\"placement\":\"" + params + "\"}";
        }

        // Convert server parameters from JSON string to JSONObject
        JSONObject paramsJson = null;
        try {
            paramsJson = new JSONObject(params);
        } catch (JSONException e) {
            Log.d(LOG_TAG, "Error parsing serverParameter JSON: " + e.getMessage());
            return null;
        }

        // Parse PLC and instantiate config object
        if (!paramsJson.has("placement")) {
            Log.d(LOG_TAG, "Required parameter placement is not present in serverParameter");
            return null;
        }
        String plc = paramsJson.optString("placement", "");
        Log.d(LOG_TAG, "AerServ placement is " + plc);
        AerServConfig config = new AerServConfig(context, plc);

        // Read optional userId parameter
        if (localParams != null && !TextUtils.isEmpty(localParams.getString("userId"))) {
            config.setUserId(localParams.getString("userId"));
        }

        return config;
    }
}
