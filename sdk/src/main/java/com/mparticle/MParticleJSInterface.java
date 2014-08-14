package com.mparticle;


import android.webkit.JavascriptInterface;

import com.mparticle.MParticle.EventType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Javascript interface to be used for {@code Webview} analytics.
 *
 */
class MParticleJSInterface {
    private static final String TAG = Constants.LOG_TAG;
    static final String INTERFACE_NAME = "mParticleAndroid";

    //the following keys are sent from the JS library as a part of each event
    private static final String JS_KEY_EVENT_NAME = "EventName";
    private static final String JS_KEY_EVENT_CATEGORY = "EventCategory";
    private static final String JS_KEY_EVENT_ATTRIBUTES = "EventAttributes";
    private static final String JS_KEY_EVENT_DATATYPE = "EventDataType";
    private static final String JS_KEY_OPTOUT = "OptOut";

    private static final int JS_MSG_TYPE_SS = 1;
    private static final  int JS_MSG_TYPE_SE = 2;
    private static final int JS_MSG_TYPE_PV = 3;
    private static final int JS_MSG_TYPE_PE = 4;
    private static final int JS_MSG_TYPE_CR = 5;
    private static final int JS_MSG_TYPE_OO = 6;

    private static final String errorMsg = "Error processing JSON data from Webview: %s";

    MParticleJSInterface() {

    }

    @JavascriptInterface
    public void logEvent(String json) {
        try {
            JSONObject event = new JSONObject(json);

            String name = event.getString(JS_KEY_EVENT_NAME);
            EventType eventType = convertEventType(event.getInt(JS_KEY_EVENT_CATEGORY));
            Map<String, String> eventAttributes = convertToMap(event.optJSONObject(JS_KEY_EVENT_ATTRIBUTES));

            int messageType = event.getInt(JS_KEY_EVENT_DATATYPE);
            switch (messageType){
                case JS_MSG_TYPE_PE:
                    MParticle.getInstance().logEvent(name,
                            eventType,
                            eventAttributes);
                    break;
                case JS_MSG_TYPE_PV:
                    MParticle.getInstance().logScreen(name,
                            eventAttributes,
                            true);
                    break;
                case JS_MSG_TYPE_OO:
                    MParticle.getInstance().setOptOut(event.optBoolean(JS_KEY_OPTOUT));
                    break;
                case JS_MSG_TYPE_CR:
                    MParticle.getInstance().logError(name, eventAttributes);
                    break;
                case JS_MSG_TYPE_SE:
                case JS_MSG_TYPE_SS:
                    //swallow session start and end events, the native SDK will handle those.
                default:

            }

        } catch (JSONException jse) {
            ConfigManager.log(MParticle.LogLevel.WARNING, String.format(errorMsg, jse.getMessage()));
        }
    }

    @JavascriptInterface
    public void setUserTag(String json){
        try{
            JSONObject attribute = new JSONObject(json);
            MParticle.getInstance().setUserTag(attribute.getString("key"));
        }catch (JSONException jse){
            ConfigManager.log(MParticle.LogLevel.WARNING, String.format(errorMsg, jse.getMessage()));
        }
    }

    @JavascriptInterface
    public void removeUserTag(String json){
        try{
            JSONObject attribute = new JSONObject(json);
            MParticle.getInstance().removeUserTag(attribute.getString("key"));
        }catch (JSONException jse){
            ConfigManager.log(MParticle.LogLevel.WARNING, String.format(errorMsg, jse.getMessage()));
        }
    }

    @JavascriptInterface
    public void setUserAttribute(String json){
        try {
            JSONObject attribute = new JSONObject(json);
            MParticle.getInstance().setUserAttribute(attribute.getString("key"), attribute.getString("value"));
        } catch (JSONException jse) {
            ConfigManager.log(MParticle.LogLevel.WARNING, String.format(errorMsg, jse.getMessage()));
        }
    }

    @JavascriptInterface
    public void removeUserAttribute(String json){
        try{
            JSONObject attribute = new JSONObject(json);
            MParticle.getInstance().removeUserAttribute(attribute.getString("key"));
        }catch (JSONException jse){
            ConfigManager.log(MParticle.LogLevel.WARNING, String.format(errorMsg, jse.getMessage()));
        }
    }

    @JavascriptInterface
    public void setSessionAttribute(String json){
        try {
            JSONObject attribute = new JSONObject(json);
            MParticle.getInstance().setSessionAttribute(attribute.getString("key"), attribute.getString("value"));
        } catch (JSONException jse) {
            ConfigManager.log(MParticle.LogLevel.WARNING, String.format(errorMsg, jse.getMessage()));
        }
    }

    @JavascriptInterface
    public void setUserIdentity(String json){
        try {
            JSONObject attribute = new JSONObject(json);
            MParticle.getInstance().setUserIdentity(attribute.getString("Identity"), convertIdentityType(attribute.getInt("Type")));
        } catch (JSONException jse) {
            ConfigManager.log(MParticle.LogLevel.WARNING, String.format(errorMsg, jse.getMessage()));
        }
    }

    @JavascriptInterface
    public void removeUserIdentity(String json){
        try{
            JSONObject attribute = new JSONObject(json);
            MParticle.getInstance().removeUserIdentity(attribute.getString("key"));
        }catch (JSONException jse){
            ConfigManager.log(MParticle.LogLevel.WARNING, String.format(errorMsg, jse.getMessage()));
        }
    }

    private Map<String, String> convertToMap(JSONObject attributes) {
        if (null != attributes) {
            Iterator keys = attributes.keys();

            Map<String, String> parsedAttributes = new HashMap<String, String>();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    parsedAttributes.put(key, attributes.getString(key));
                } catch (JSONException e) {
                    ConfigManager.log(MParticle.LogLevel.WARNING, "Could not parse event attribute value");
                }
            }

            return parsedAttributes;
        }

        return null;
    }

    private EventType convertEventType(int eventType) {
        switch (eventType) {
            case 1:
                return EventType.Navigation;
            case 2:
                return EventType.Location;
            case 3:
                return EventType.Search;
            case 4:
                return EventType.Transaction;
            case 5:
                return EventType.UserContent;
            case 6:
                return EventType.UserPreference;
            case 7:
                return EventType.Social;
            default:
                return EventType.Other;
        }
    }

    private MParticle.IdentityType convertIdentityType(int identityType) {
        switch (identityType) {
            case 0:
                return MParticle.IdentityType.Other;
            case 1:
                return MParticle.IdentityType.CustomerId;
            case 2:
                return MParticle.IdentityType.Facebook;
            case 3:
                return MParticle.IdentityType.Twitter;
            case 4:
                return  MParticle.IdentityType.Google;
            case 5:
                return  MParticle.IdentityType.Microsoft;
            case 6:
                return  MParticle.IdentityType.Yahoo;
            default:
                return  MParticle.IdentityType.Email;
        }
    }
}
