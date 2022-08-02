
package ru.cordova.android.keyboard;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;


import java.util.function.Function;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.os.Build;
import android.content.Context;
import android.content.BroadcastReceiver;

import android.util.Log;

import android.inputmethodservice.Keyboard;

import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;



import android.content.res.Configuration;
import android.content.res.Resources;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

import android.view.WindowInsets;




public class AndroidKeyboard extends CordovaPlugin {
    
    private static final String LOG_TAG = "AndroidKeyboard";

    public AndroidKeyboard() {   };

    private int density = 0;
    private int initHeight = 0;
    private int initHightDiff = 0;
    private int previousHeightBottom = 0;

    private boolean isWatchEvent = false;
    private boolean isInitHeight = false;
    private boolean isOpenKeyboard = false;//Будет работать если isWatchEvent true;


    private CallbackContext callbackContextKeyboard = null;
    BroadcastReceiver receiver;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        this.initKeyboardEvent();

    }

    

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        

     
        final Activity activity = this.cordova.getActivity();
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        final Window window = activity.getWindow();
        View view = window.getDecorView();
      

        if(action.equals("on")){
            isWatchEvent = true;
            this.callbackContextKeyboard = callbackContext;
            // callbackContext.success();
            return true;
        }

        if(action.equals("off")){
            isWatchEvent = false;
            this.callbackContextKeyboard = null;
            this.isOpenKeyboard = false;
            callbackContext.success();
            return true;
        }


        // if (action.equals("show")) {
        //     imm.showSoftInput(view, 0);
        //     callbackContext.success();
        //     return true;
        // }

        // if (action.equals("hide")) {
        //     imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        //     callbackContext.success();
        //     return true;
        // }

        if (action.equals("getHeight")) {
            callbackContext.success(initHeight);
            return true;
        }

        return false;

    }

    public void watchHeight(int heightDiff, int keyboardHeight, boolean isOpen){
        final CordovaWebView appView = this.webView;

        if(!isInitHeight && heightDiff > 200){
            initHeight = keyboardHeight;
            initHightDiff = heightDiff;
            isInitHeight = true;
        }


        if(isWatchEvent){
            JSONObject payloadEvent = new JSONObject();
            try {
                
                if(isOpen){
                    this.isOpenKeyboard = false;
                    payloadEvent.put("height", initHeight);
                    payloadEvent.put("isShow", true);
                }else{
                    payloadEvent.put("isShow", false);
                    payloadEvent.put("height", 0);
                    this.isOpenKeyboard = true;
                }
                

            } catch (Exception e) {
                Log.d("Error", ": " + e.getMessage());
            }
          
        
            this.sendResult(payloadEvent, true);
            
        }
    }


    private void sendResult(JSONObject info, boolean keepCallback) {
        if (this.callbackContextKeyboard != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, info);
            result.setKeepCallback(keepCallback);
            this.callbackContextKeyboard.sendPluginResult(result);
        }
    }

    public void initKeyboardEvent(){
    
        DisplayMetrics dm = new DisplayMetrics();
        this.cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = (int)(dm.density);
        AndroidKeyboard _this = this;

        final View rootView = this.cordova.getActivity().getWindow().getDecorView().findViewById(android.R.id.content).getRootView();

        OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
            	Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);//Наполняет объект r данными Display размерами, но в своей единицы измерения dp
                int rootHeight = rootView.getRootView().getHeight();
                int heightDiff = rootHeight - (r.bottom - r.top);//
                int keyboardHeight = (int)(heightDiff / density);
                boolean isOpen = false;

                if(heightDiff + (r.bottom - r.top) == rootHeight && isWatchEvent){
                    // isOpen = true;
                }

                if(heightDiff + r.bottom - initHightDiff == previousHeightBottom && isWatchEvent){
                    isOpen = true;
                }

                _this.watchHeight(heightDiff, keyboardHeight, isOpen);
                previousHeightBottom = r.bottom;
              
            }
        }; 
        

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(listener);

    }

  
}


