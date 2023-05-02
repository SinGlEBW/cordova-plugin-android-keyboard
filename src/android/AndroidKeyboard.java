package ru.cordova.android.keyboard;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;


import java.util.function.Function;
import java.util.Arrays;

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

import android.widget.LinearLayout;


public class AndroidKeyboard extends CordovaPlugin {
    
    private static final String LOG_TAG = "AndroidKeyboard";

    public AndroidKeyboard() {   };

    // private float density = 0;
    private int initHeight = 0;
    private int initHeightDiff = 0;
    private int previousHeightBottom = 0;

    private boolean isWatchEvent = false;
    private boolean isInitHeight = false;
    private boolean isOpenedKeyboard = false;//

    private int heightOfKeyboard = 0;
    private int previousHeightDiffrence = 0;
    private boolean isKeyBoardVisible = false;
    private CordovaWebView webView;

    private CallbackContext callbackContextKeyboard = null;
    BroadcastReceiver receiver;

    public void initialize(CordovaInterface cordova, CordovaWebView cordovaWebView) {
        super.initialize(cordova, cordovaWebView);
        webView = cordovaWebView;
        this.initKeyboardEvent();


    }

    @Override
    public void onStart() {
        super.onStart();
//        setTimeout(() -> imm.hideSoftInputFromWindow(finalView.getWindowToken(), 0), 5);

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        final Activity activity = this.cordova.getActivity();
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        final Window window = activity.getWindow();

        View view;
        try {
            view = (View)webView.getClass().getMethod("getView").invoke(webView);
        }
        catch (Exception e){
            view = (View)webView;
        }


//        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
////Enjoy your keyboard height
//        initHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;


        if(action.equals("on")){
            isWatchEvent = true;
            this.callbackContextKeyboard = callbackContext;
            // callbackContext.success();
            return true;
        }

        if(action.equals("off")){
            isWatchEvent = false;
            this.callbackContextKeyboard = null;
            callbackContext.success();
            return true;
        }
        
        if (action.equals("show")) {
            imm.showSoftInput(view, 0);
            callbackContext.success();
            return true;
        }

        if (action.equals("hide")) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            callbackContext.success();
            return true;
        }

        if (action.equals("getHeight")) {
            callbackContext.success(initHeight);
            return true;
        }

        return false;
    }


    private void sendResult(JSONObject info, boolean keepCallback) {
        if (this.callbackContextKeyboard != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, info);
            result.setKeepCallback(keepCallback);
            this.callbackContextKeyboard.sendPluginResult(result);
        }
    }
    private static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();
    }

    public int dpToPx(Resources resources, int dp) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return Math.round(dp / displayMetrics.density);      
    }

    public float getDensity(Resources resources) {
        return resources.getDisplayMetrics().density;     
    }

    public int getDensityDPI(Resources resources) {
        return resources.getDisplayMetrics().densityDpi;     
    }

    public void initKeyboardEvent(){
        /*
        * Высота клавиатуры - 280px (770dp)  
        * Оставшееся место клава - 524px (1441dp)
        * Несмотря на то что растягиваем экран на полную, что бы посчитать высоту клавиатуру нам нужно брать расстояние от navBar до верхнего края
        * эту высоту будем получаем через r.bottom т.к. она меняется динамически при открытии клавиатуры.
        * Переводить dp в px можно формулу: (dp / (displayMetrics.densityDpi / 160)) или просто взять (dp / displayMetrics.density) 
        * dp берём
        */

        AndroidKeyboard _this = this;
        final Activity activity = this.cordova.getActivity();
//        final View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content).getRootView();
        final View rootView = activity.getWindow().getDecorView();
        Resources resources = rootView.getResources();


        int statusBarId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int navigationBarId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

        int navigatorBarSize = resources.getDimensionPixelSize(navigationBarId);
        int statusBarSize = resources.getDimensionPixelSize(statusBarId);

        /*
        * INFO: На будущее можно передать высоту statusBar и NavBar
        */

        OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean isOpen = false;

            	Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);//Наполняет объект rect данными Display размерами, но в своей единицы измерения dp

                int heightApp = rootView.getRootView().getHeight();//rootView.getMeasuredHeight();//dp. height можно получить так: rootView.getRootView().getHeight();
                int heightNavBarDP = navigatorBarSize;
                if(resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    heightNavBarDP = 0;
                }
                int htmlDp = (heightApp - heightNavBarDP);//Высота html в DevTools без navBar. Клавиатуру высчитываем без navBar. Статик
                int staticHeightHTML = dpToPx(resources, htmlDp);
                int dynamicHeightHtml =  dpToPx(resources, rect.bottom);



//                int keyBoardHeight = (screenHeight - (r.bottom - r.top));
//                int heightKeyboardPxTest =  dpToPx(resources, keyBoardHeight - heightNavBarDP);
//                int paddingBottom = rootView.getBottom();


                int heightKeyboard = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
                    WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
                    /*Получает целиком высоту клавиатуры + navBar*/
                    int keyboardHeightDp = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

                    if(keyboardHeightDp != 0){
                        if(Build.MODEL.equals("SM-G780G")){
                            /* На samsung SM-G780G значение status_bar вычитаеться при расчёте отступа снизу. Для корректного значения прибавляем */
                            keyboardHeightDp = keyboardHeightDp + statusBarSize;
                        }
                        //Получим только размер клавиатуры за вычетом navBar и преобразуем в px.


                        heightKeyboard =  dpToPx(resources, keyboardHeightDp - heightNavBarDP);
                    }
                }else{
                    heightKeyboard = Math.round(staticHeightHTML - dynamicHeightHtml);
                }

//                heightKeyboard = Math.round(staticHeightHTML - dynamicHeightHtml);
                /*-----------------------------------------------------------------------*/

                if(isWatchEvent){
                    JSONObject payloadEvent = new JSONObject();

                    try {
                        if(!isOpenedKeyboard && heightKeyboard > 0){
                            if(initHeight == 0){
                                initHeight = heightKeyboard;
                            }
                            payloadEvent.put("height", heightKeyboard);
                            payloadEvent.put("isShow", true);
                            isOpenedKeyboard = true;
                            _this.sendResult(payloadEvent, true);
                        }

                        if(isOpenedKeyboard && heightKeyboard <= 0){
                            payloadEvent.put("isShow", false);
                            payloadEvent.put("height", 0);
                            isOpenedKeyboard = false;
                            _this.sendResult(payloadEvent, true);
                        }
                        
        
                    } catch (Exception e) {
                        Log.d("Error", ": " + e.getMessage());
                    }
                  

                }

              
            }
        }; 
        

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(listener);

    }


}


